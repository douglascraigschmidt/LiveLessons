import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.*;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toList;

/**
 * This program applies Project Reactor features to implement various
 * types of backpressure strategies (e.g., ignore, buffer, error,
 * latest, drop, and push/pull) between a publisher and a subscriber
 * that (conditionally) run in different threads/schedulers.  This
 * program also measures the performance of checking random numbers
 * for primality with and without various types of memoizers (e.g.,
 * untimed and timed) based on Java ConcurrentHashMap.  In addition,
 * it demonstrates the use of slicing with the Flux takeWhile() and
 * skipWhile() operations.
 */
public class ex3 {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * Count the # of pending items between publisher and subscriber.
     */
    private final AtomicInteger mPendingItemCount =
        new AtomicInteger(0);

    /**
     * A list of randomly-generated integers.
     */
    private final List<Integer> mRandomIntegers;

    /**
     * The scheduler used to consume random integers by checking if
     * they are prime or not.
     */
    private final Scheduler mSubscriberScheduler;

    /**
     * The scheduler used to publishRandomIntegers random integers.
     */
    private final Scheduler mPublisherScheduler;

    /**
     * A subscriber that applies hybrid push/pull backpressure.
     */
    private final HybridBackpressureSubscriber mSubscriber;

    /**
     * Track all disposables to dispose them all at once.
     */
    private final Disposable.Composite mDisposables;

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Create an instance to run the test.
        new ex3(argv).run();
    }

    /**
     * Constructor initializes the fields.
     */
    ex3(String[] argv) {
        // Parse the command-line arguments.
        Options.instance().parseArgs(argv);

        // Generate a list of 'count' random Integers whose values
        // don't exceed the given maximum.
        mRandomIntegers = generateRandomIntegers(Options.instance().count(),
                                                 Options.instance().maxValue());

        // Run the publisher in a single thread.
        mPublisherScheduler = Schedulers
            .newParallel("publisher", 1);

        // Maybe run the subscriber in a different thread.
        mSubscriberScheduler = Options.instance().parallel()
            // Choose a different scheduler if we're running in parallel.
            ? Schedulers.newParallel("subscriber",
                                     Options.instance().parallelism())

            // Run everything in the publisher scheduler.
            : mPublisherScheduler;

        // A subscriber that implements hybrid push/pull backpressure.
        mSubscriber = new HybridBackpressureSubscriber(mPendingItemCount);

        // Track all disposables to dispose them all at once.
        mDisposables = Disposables
            .composite(mPublisherScheduler,
                       mSubscriberScheduler,
                       mSubscriber);
    }

    /**
     * Run all the tests and print the results.
     */
    private void run() {
        // Create a Memoizer instance.
        Memoizer<Integer, Integer> memoizer =
            Options.makeMemoizer(PrimeUtils::isPrime);

        // Create and time prime checking using a memoizer.
        RunTimer.timeTest(this::runTest,
                          memoizer,
                          "test with memoizer");

        // Get a copy of the memoizer's map.
        Map<Integer, Integer> memoizerCopy = memoizer.getCache();

        // Shutdown the memoizer.
        memoizer.shutdown();

        // Create and time prime checking without using a memoizer.
        RunTimer.timeTest(this::runTest,
                          PrimeUtils::isPrime,
                          "test without memoizer");

        // Dispose of all schedulers and subscribers.
        mDisposables.dispose();

        // Print the results.
        Options.print(RunTimer.getTimingResults());

        if (Options.instance().diagnosticsEnabled())
            // Print the results (which demonstrates slicing on the ConcurrentHashMap).
            printResults(memoizerCopy);
    }

    /**
     * Run the prime number test.
     * 
     * @param primeChecker A function that maps candidate primes to their
     * smallest factor (if they aren't prime) or 0 if they are prime
     * @param testName Name of the test
     * @return The prime checker (which may be updated during the test).
     */
    private Function<Integer, Integer> runTest(Function<Integer, Integer> primeChecker,
                                               String testName) {
        Options.print("Starting "
                      + testName
                      + " with count = "
                      + Options.instance().count());

        // Reset the counters.
        PrimeUtils.sPrimeCheckCounter.set(0);
        mPendingItemCount.set(0);

        // This function asynchronously determines if a random # is prime or not.
        Function<Integer, Mono<PrimeUtils.Result>> determinePrimality = number -> Mono
            .fromCallable(() -> number)

            // Subscriber may run in different thread(s).
            .publishOn(mSubscriberScheduler)

            // Check if the # is prime.
            .map(__ -> PrimeUtils.checkIfPrime(number, primeChecker));

        // Create a publisher that runs on its own scheduler
        // and returns a Flux that emits random Integers.
        publishRandomIntegers(mPublisherScheduler)

            // Conditionally enable logging.
            .transform(ReactorUtils
                       // Conditionally enable logging.
                       .logIf(Options.instance().loggingEnabled()))

            // Use the flatMap() concurrency idiom to concurrently (maybe)
            // check each random # to see if it's prime.
            .flatMap(determinePrimality)

            // The subscriber starts all the wheels in motion!
            .subscribe(mSubscriber);

        Options.debug(TAG, "waiting in the main thread");

        // BLock until all processing is complete.
        mSubscriber.await();

        // Cleverly print out the results.
        Options.print(makeExitString(testName, primeChecker));

        // Return prime checker (which may update during the test).
        return primeChecker;
    }

    /**
     * Publish a stream of random numbers.
     *
     * @param scheduler Scheduler to publishRandomIntegers the numbers on
     * @return Return a {@link Flux} that publishes random numbers
     */
    private Flux<Integer> publishRandomIntegers(Scheduler scheduler) {
        // This consumer emits a flux stream of random integers.
        return Flux
            // Emit a flux stream of random integers.
            .create(Options.instance().backPressureEnabled()
                    // Emit integers using backpressure.
                    ? Emitters.makeBackpressureEmitter(mRandomIntegers.iterator(),
                                                       mPendingItemCount)
                    // Emit integers not using backpressure.
                    : Emitters.makeNonBackpressureEmitter(mRandomIntegers.iterator(),
                                                          mPendingItemCount,
                                                          mRandomIntegers.size()),
                    // Set the overflow strategy.
                    Options.instance().overflowStrategy())

            // Subscribe on the given scheduler.
            .subscribeOn(scheduler);
    }

    /**
     * Demonstrate how to slice by applying the Project Reactor Flux
     * {@code skipWhile()} and {@code takeWhile()} operations to the
     * {@code map} parameter.
     */
    private void printResults(Map<Integer, Integer> map) {
        // Sort the map by its values.
        var sortedMap = ReactorUtils.sortMap(map, comparingByValue());

        // Print out the entire contents of the sorted map.
        Options.print("map with "
                      + sortedMap.size()
                      + " elements sorted by value = \n" + sortedMap);

        // Print out the prime #'s using takeWhile().
        PrimeUtils.printPrimes(sortedMap);

        // Print out the non-prime #'s using skipWhile().
        PrimeUtils.printNonPrimes(sortedMap);
    }
    
    /**
     * Create and return the proper exit string based on various conditions.
     * @return The proper exit string.
     */
    private String makeExitString(String testName,
                                  Function<Integer, Integer> primeChecker) {
        String prefix = "Leaving "
                + testName
                + " with "
                + PrimeUtils.sPrimeCheckCounter.get()
                + " prime checks ";

        if (Options.instance().overflowStrategy() == FluxSink.OverflowStrategy.DROP
                || Options.instance().overflowStrategy() == FluxSink.OverflowStrategy.LATEST)
            return prefix;
        else if (primeChecker instanceof Memoizer)
            return prefix
                    + "(" + (Options.instance().count()
                    - PrimeUtils.sPrimeCheckCounter.get())
                    + " duplicates)";
        else
            return prefix;
    }


    /**
     * Generate and return a {@link List} of random {@link Integer}s.
     *
     * @param count The number of random Integers to generate
     * @param maxValue The maximum value of the random {@link Integer}s
     * @return A {@link List} of random {@link Integer}s
     */
    private List<Integer> generateRandomIntegers(int count, int maxValue) {
        return new Random()
            // Generate "count" random ints.
            .ints(count,
                  // Try to generate duplicates.
                  maxValue - count,
                  maxValue)

            // Convert each primitive int to Integer.
            .boxed()

            // Trigger intermediate operations and collect into list.
            .collect(toList());
    }
}
    
