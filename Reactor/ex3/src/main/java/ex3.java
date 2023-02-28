import common.Options;
import common.PrimeUtils;
import publisher.Publisher;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import subscriber.BackpressureSubscriber;
import utils.Memoizer;
import utils.RandomUtils;
import utils.ReactorUtils;
import utils.RunTimer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.Map.Entry.comparingByValue;
import static publisher.Publisher.sPendingItemCount;

/**
 * This program applies Project Reactor features to implement various
 * types of backpressure strategies (e.g., ignore, buffer, error,
 * latest, drop, and push/pull) between a publisher and a subscriber
 * that (conditionally) run in different threads/schedulers. 
 *
 * This program also measures the performance of checking random
 * numbers for primality with and without various types of memoizers
 * (e.g., untimed and timed) based on Java {@link ConcurrentHashMap}.
 * In addition, it demonstrates the use of slicing with the Flux
 * takeWhile() and skipWhile() operations.
 */
public class ex3 {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * The scheduler used to consume random integers by checking if
     * they are prime or not.
     */
    private final Scheduler mSubscriberScheduler;

    /**
     * The {@link Scheduler} used to publish random {@link Integer}
     * objects in its own thread.
     */
    private final Scheduler mPublisherScheduler = Schedulers
        .newParallel("publisher", 1);

    /**
     * A subscriber that applies backpressure.
     */
    private final BackpressureSubscriber mSubscriber =
        new BackpressureSubscriber(sPendingItemCount);
    ;

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

        // Conditionally run the subscriber in a different thread.
        mSubscriberScheduler = Options.instance().parallel()
            // Choose a different scheduler if we're running in parallel.
            ? Schedulers.newParallel("subscriber",
                                     Options.instance().parallelism())

            // Otherwise run everything on the publisher's scheduler.
            : mPublisherScheduler;

        // Track all     disposables to dispose them all at once.
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
            // Print the results (which demonstrates slicing on the
            // ConcurrentHashMap).
            printResults(memoizerCopy);
    }

    /**
     * Run the prime number test.
     * 
     * @param primeChecker A {@link Function} that maps candidate
     *                     primes to their smallest factor (if they
     *                     aren't prime) or 0 if they are prime
     * @param testName Name of the test
     * @return The prime checker (which may be updated during the
     *         test)
     */
    private Function<Integer, Integer> runTest
        (Function<Integer, Integer> primeChecker,
         String testName) {
        Options.print("Starting "
                      + testName
                      + " with count = "
                      + Options.instance().count());

        // Reset the counters.
        PrimeUtils.sPrimeCheckCounter.set(0);
        sPendingItemCount.set(0);

        // Generate a list of 'count' random Integers whose values
        // don't exceed the given maximum.
        var randomIntegers = RandomUtils
            .generateRandomIntegers(Options.instance().count(),
                                    Options.instance().maxValue());

        // This Function asynchronously determines if a random # is
        // prime or not.
        Function<Integer, Mono<PrimeUtils.Result>> check4Prime =
            getPrimeCheckFunction(primeChecker);

        // Create a publisher that runs on its own scheduler and
        // returns a Flux that emits random Integers.
        Publisher
            .publishIntegers(mPublisherScheduler,
                             randomIntegers)
            // Conditionally enable logging.
            .transform(ReactorUtils
                       // Conditionally enable logging.
                       .logIf(Options.instance().loggingEnabled()))

            // Concurrently (maybe) check each random # to see if it's
            // prime.  This operation may run on the subscriber's
            // scheduler, depending on the options used to run the
            // program.
            .flatMap(check4Prime)

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
     * Get a {@link Function} that  asynchronously determines if a
     * random # is prime or not.
     *
     * @param primeChecker A {@link Function} that checks a number's
     *                     primality
     * @return A {@link Function} that  asynchronously determines if a
     *         random # is prime or not
     */
    private Function<Integer,
                     Mono<PrimeUtils.Result>> getPrimeCheckFunction
        (Function<Integer, Integer> primeChecker) {
        return number -> Mono
            .fromCallable(() -> number)

            // Subscriber may run in different thread(s).
            .publishOn(mSubscriberScheduler)

            // Check if the # is prime.
            .map(__ -> PrimeUtils
                 .checkIfPrime(number,
                               primeChecker));
    }

    /**
     * Demonstrate how to slice by applying the Project Reactor Flux
     * {@code skipWhile()} and {@code takeWhile()} operations to the
     * {@code map} parameter.
     *
     * @param map A {@link Map} containing results
     */
    private void printResults(Map<Integer, Integer> map) {
        ReactorUtils
            // Sort the map by its values.
            .sortMap(map, comparingByValue())

            // Print the results on success.
            .doOnSuccess(sortedMap -> {
                    // Print the entire contents of the sorted map.
                    Options.print("map with "
                                  + sortedMap.size()
                                  + " elements sorted by value = \n" 
                                  + sortedMap);

                    // Print out the prime #'s using takeWhile().
                    PrimeUtils.printPrimes(sortedMap);

                    // Print out the non-prime #'s using skipWhile().
                    PrimeUtils.printNonPrimes(sortedMap);
                })

            // Block until all processing is done.
            .block();
    }
    
    /**
     * Create and return the formatted exit string based on various
     * conditions.
     *
     * @return The formatted exit string
     */
    private String makeExitString(String testName,
                                  Function<Integer, Integer> primeChecker) {
        String prefix = "Leaving "
            + testName
            + " with "
            + PrimeUtils.sPrimeCheckCounter.get()
            + " prime checks ";

        if (primeChecker instanceof Memoizer)
            return prefix
                + "(" + (Options.instance().count()
                         - PrimeUtils.sPrimeCheckCounter.get())
                + " duplicates)";
        else
            return prefix;
    }
}
    
