import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
     * Count # of calls to isPrime() to determine caching benefits.
     */
    private final AtomicInteger mPrimeCheckCounter;

    /**
     * Count the # of pending items between publisher and subscriber.
     */
    private final AtomicInteger mPendingItemCount;

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
     * The scheduler used to publish random integers.
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
        // Create an instance to test.
        ex3 test = new ex3(argv);

        // Run the tests.
        test.run();
    }

    /**
     * Constructor initializes the fields.
     */
    ex3(String[] argv) {
        // Initialize this count to 0.
        mPrimeCheckCounter = new AtomicInteger(0);

        // The count initially starts at 0.
        mPendingItemCount = new AtomicInteger(0);

        // Parse the command-line arguments.
        Options.instance().parseArgs(argv);

        // Get how many integers we should generate.
        int count = Options.instance().count();

        // Get the max value for the random numbers.
        int maxValue = Options.instance().maxValue();

        // Generate a list of random integers.
        mRandomIntegers = new Random()
            // Generate "count" random ints.
            .ints(count,
                  // Try to generate duplicates.
                  maxValue - count, 
                  maxValue)

            // Convert each primitive int to Integer.
            .boxed()    
                   
            // Trigger intermediate operations and collect into list.
            .collect(toList());

        // Run the publisher in a single thread.
        mPublisherScheduler = Schedulers
            .newParallel("publisher", 1);

        // Maybe run the subscriber in a different single thread.
        mSubscriberScheduler = Options.instance().parallel()
            // Choose a different scheduler if we're running in parallel.
            ? Schedulers.newParallel("subscriber",
                                     Options.instance().parallelism())

            // Run everything in the publisher scheduler.
            : mPublisherScheduler;

        // This subscriber implements hybrid push/pull backpressure.
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
        Memoizer<Integer, Integer> memoizer =
            Options.makeMemoizer(this::isPrime);

        // Create and time prime checking with a memoizer.
        timeTest(memoizer,
                 "test with memoizer");

        // Get a copy of the memoizer's map.
        Map<Integer, Integer> memoizerCopy = memoizer.getCache();

        // Shutdown the memoizer.
        memoizer.shutdown();

        // Create and time prime checking without a memoizer.
        timeTest(this::isPrime,
        "test without memoizer");

        // Dispose of all schedulers and subscribers.
        mDisposables.dispose();

        // Print the results.
        Options.print(RunTimer.getTimingResults());

        // Demonstrate slicing on the concurrent hash map.
        demonstrateSlicing(memoizerCopy);
    }

    /**
     * Time {@code testName} using the given {@code hashMap}.
     *
     * @param primeChecker The prime checker used evaluate prime candidates.
     * @param testName The name of the test.
     */
    private void timeTest(Function<Integer, Integer> primeChecker,
                          String testName) {
        RunTimer
            // Time how long this test takes to run.
            .timeRun(() ->
                     // Run the test using the given prime checker.
                     runTest(primeChecker, testName),
                     testName);
    }

    /**
     * Run the prime number test.
     * 
     * @param primeChecker A function that maps candidate primes to their
     * smallest factor (if they aren't prime) or 0 if they are prime
     * @param testName Name of the test
     * @return The prime checker (which may be updated during the test).
     */
    private Function<Integer, Integer> runTest
        (Function<Integer, Integer> primeChecker,
         String testName) {
        Options.print("Starting "
                      + testName
                      + " with count = "
                      + Options.instance().count());

        // Reset the counters.
        mPrimeCheckCounter.set(0);
        mPendingItemCount.set(0);

        // Create a publisher that runs on its own scheduler.
        Flux<Integer> publisher = publish(mPublisherScheduler);

        // This function determines if a random # is prime or not.
        Function<Integer, Flux<Result>> determinePrimality = number -> Flux
            .just(number)

            // Subscriber may run in different thread(s).
            .publishOn(mSubscriberScheduler,
                       // The initial request size.
                       mSubscriber.nextRequestSize())

            // Check if the # is prime.
            .map(__ -> checkIfPrime(number, primeChecker));

        // Run the main flux pipeline.
        publisher
            // Enable transformation at instantiation time.
            .transform(ReactorUtils
                       // Conditionally enable logging.
                       .logIf(Options.instance().loggingEnabled()))

            // Use the flatMap() idiom to concurrently (maybe) check
            // each random # to see if it's prime.
            .flatMap(determinePrimality)

            // The subscriber starts all the wheels in motion!
            .subscribe(mSubscriber);

        Options.debug(TAG, "waiting in the main thread");

        // Wait for all processing to complete.
        mSubscriber.await();

        // Cleverly print out the results.
        Options.print(makeExitString(testName, primeChecker));

        // Return prime checker (which may update during the test).
        return primeChecker;
    }

    /**
     * Publish a stream of random numbers.
     *
     * @param scheduler Scheduler to publish the numbers on.
     * @return Return a flux that publishes random numbers
     */
    private Flux<Integer> publish(Scheduler scheduler) {
        // This consumer emits a flux stream of random integers.
        return Flux
            // Emit a flux stream of random integers.
            .create(Options.instance().backPressureEnabled()
                    // Emit integers using backpressure.
                    ? makeBackpressureEmitter(mRandomIntegers.iterator())
                    // Emit integers not using backpressure.
                    : makeNonBackpressureEmitter(mRandomIntegers.iterator()),
                    // Set the overflow strategy.
                    Options.instance().overflowStrategy())

            // Subscribe on the given scheduler.
            .subscribeOn(scheduler);
    }

    /**
     * A factory method that's used to emit a flux stream of random
     * integers using a hybrid push/pull backpressure model.
     *
     * @param iterator Iterator containing the random integers
     * @return A consumer to a flux sink that emits a flux stream of
     *         random integers using a hybrid push/pull backpressure
     *         model
     */
    private Consumer<FluxSink<Integer>>
        makeBackpressureEmitter(Iterator<Integer> iterator) {
        // Create an emitter that uses the hybrid push/pull
        // backpressure model.
        return sink -> sink
            // Hook method called when request is made to sink.
            .onRequest(size -> {
                    Options.debug(TAG, "Request size = " + size);

                    // Try to publish size # of items.
                    for (int i = 0;
                         i < size;
                         ++i) {
                        // Keep going if iterator's not done.
                        if (iterator.hasNext()) {
                            // Get the next item.
                            Integer item = iterator.next();

                            // Store current pending item count.
                            int pendingItems =
                                mPendingItemCount.incrementAndGet();

                            Options.debug(TAG,
                                          "published item: "
                                          + item
                                          + ", pending items = "
                                          + pendingItems);

                            // Publish the next item.
                            sink.next(item);
                        } else {
                            // We're done publishing.
                            sink.complete();
                            break;
                        }
                    }
                });
    }

    /**
     * A factory method that's used to emit a flux stream of random
     * integers without concern for backpressure.
     *
     * @param iterator Iterator containing the random integers
     * @return A consumer to a flux sink that emits a flux stream
     *         of random integers without concern for backpressure
     */
    private Consumer<FluxSink<Integer>>
        makeNonBackpressureEmitter(Iterator<Integer> iterator) {
        // Create an emitter that just blasts out random integers.
        return sink -> {
            Options.debug(TAG, "Request size = "
                          + mRandomIntegers.size());

            // Keep going if iterator's not done.
            while (iterator.hasNext()) {
                // Get the next item.
                Integer item = iterator.next();

                // Store current pending item count.
                int pendingItems =
                    mPendingItemCount.incrementAndGet();

                Options.debug(TAG,
                              "published item: "
                              + item
                              + ", pending items = "
                              + pendingItems);

                // Only publish an item if the sink hasn't been cancelled.
                if (!sink.isCancelled())
                    // Publish the next item.
                    sink.next(item);
            }

            // We're done publishing.
            sink.complete();
        };
    }

    /**
     * Check if {@code primeCandidate} is prime or not.
     *
     * @param primeCandidate The number to check if it's prime
     * @param primeChecker A function that checks if number is prime
     * @return A {@code Result} object that contains the original
     * {@code primeCandidate} and either 0 if it's prime or its
     * smallest factor if it's not prime.
     */
    private Result checkIfPrime(Integer primeCandidate,
                                Function<Integer, Integer> primeChecker) {
        // Return a tuple containing the prime candidate and the
        // result of checking if it's prime.
        return new Result(primeCandidate,
                          primeChecker.apply(primeCandidate));
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns 0 if it is
     * prime, or the smallest factor if it is not prime.
     */
    private Integer isPrime(Integer primeCandidate) {
        // Increment the counter to indicate a prime candidate wasn't
        // already in the cache.
        mPrimeCheckCounter.incrementAndGet();

        int n = primeCandidate;

        if (n > 3)
            // This algorithm is intentionally inefficient to burn
            // lots of CPU time!
            for (int factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (Thread.interrupted()) {
                    // Options.debug(" Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return factor;

        return 0;
    }

    /**
     * Demonstrate how to slice by applying the Project Reactor Flux
     * {@code skipWhile()} and {@code takeWhile()} operations to the
     * {@code map} parameter.
     */
    private void demonstrateSlicing(Map<Integer, Integer> map) {
        // Sort the map by its values.
        var sortedMap = sortMap(map, comparingByValue());

        // Print out the entire contents of the sorted map.
        Options.print("map with "
                      + sortedMap.size()
                      + " elements sorted by value = \n" + sortedMap);

        // Print out the prime #'s using takeWhile().
        printPrimes(sortedMap);

        // Print out the non-prime #'s using skipWhile().
        printNonPrimes(sortedMap);
    }
    
    /**
     * Print out the prime numbers in the {@code sortedMap}.
     */
    private void printPrimes(Map<Integer, Integer> sortedMap) {
        // Create a list of prime integers.
        var primes = Flux
            // Convert EntrySet of the map into a flux stream.
            .fromIterable(sortedMap.entrySet())
            
            // Slice the stream using a predicate that stops after a
            // non-prime # (i.e., getValue() != 0) is reached.
            .takeWhile(entry -> entry.getValue() == 0)

            // Map the EntrySet into just the key.
            .map(Map.Entry::getKey)

            // Collect the results into a list.
            .collect(toList())

            // Block until processing is done.
            .block();

        // Print out the list of primes.
        Options.print("primes =\n" + primes);
    }

    /**
     * Print out the non-prime numbers and their factors in the {@code
     * sortedMap}.
     */
    private void printNonPrimes(Map<Integer, Integer> sortedMap) {
        // Create a list of non-prime integers and their factors.
        var nonPrimes = Flux
            // Convert EntrySet of the map into a flux stream.
            .fromIterable(sortedMap.entrySet())

            // Slice the stream using a predicate that skips over the
            // non-prime #'s (i.e., getValue() == 0);
            .skipWhile(entry -> entry.getValue() == 0)

            // Collect the results into a list.
            .collect(toList())
            
            // Block until processing is done.
            .block();

        // Print out the list of primes.
        Options.print("non-prime numbers and their factors =\n"
                      + nonPrimes);
    }

    /**
     * Sort {@code map} via the {@code comparator} and {@code LinkedHashMap}
     * @param map The map to sort
     * @param comparator The comparator to compare map entries.
     * @return The sorted map
     */
    private Map<Integer, Integer> sortMap
        (Map<Integer, Integer> map,
         Comparator<Map.Entry<Integer, Integer>> comparator) {
        // Create a map that's sorted by the value in map.
        return Flux
            // Convert EntrySet of the map into a flux stream.
            .fromIterable(map.entrySet())

            // Sort the elements in the stream using the comparator.
            .sort(comparator)

            // Trigger intermediate processing and collect key/value
            // pairs in the stream into a LinkedHashMap, which
            // preserves the sorted order.
            .collect(toMap(Map.Entry::getKey,
                           Map.Entry::getValue,
                           (e1, e2) -> e2,
                           LinkedHashMap::new))

            // Block until processing is done.
            .block();
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
                + mPrimeCheckCounter.get()
                + " prime checks ";

        if (Options.instance().overflowStrategy() == FluxSink.OverflowStrategy.DROP
                || Options.instance().overflowStrategy() == FluxSink.OverflowStrategy.LATEST)
            return prefix;
        else if (primeChecker instanceof Memoizer)
            return prefix
                    + "(" + (Options.instance().count()
                    - mPrimeCheckCounter.get())
                    + " duplicates)";
        else
            return prefix;
    }
}
    
