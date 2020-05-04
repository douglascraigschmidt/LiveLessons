import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * This example showcases and benchmarks the use of a Java
 * ConcurrentHashMap, a Java SynchronizedMap, and a HashMap protected
 * with a Java StampedLock are used to compute/cache/retrieve large
 * prime numbers.  This example also demonstrates several advanced
 * features of StampedLock, as well as the use of slicing with the
 * Java streams takeWhile() and dropWhile() operations.
 */
public class ex3 {
    /**
     * Count the number of calls to isPrime() as a means to determine
     * the benefits of caching.
     */
    private final AtomicInteger mPrimeCheckCounter;

    /**
     * Count the number of pending items.
     */
    private final AtomicInteger mPendingItemCount;

    /**
     * A list of randomly-generated large integers.
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
     * A subscriber that applies adaptive backpressure.
     */
    private final AdaptiveBackpressureSubscriber mSubscriber;

    /**
     * Keeps track of all disposables so they can be disposed in one
     * fell swoop.
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

        // Generate a list of random large integers.
        mRandomIntegers = new Random()
            // Generate "count" random large ints
            .ints(count,
                  // Try to generate duplicates.
                  maxValue - count, 
                  maxValue)

            // Convert each primitive int to Integer.
            .boxed()    
                   
            // Trigger intermediate operations and collect into list.
            .collect(toList());

        // Run the subscriber in a single thread.
        mSubscriberScheduler = Schedulers
            .newParallel("subscriber", 1);

        // Run the publisher in a different single thread.
        mPublisherScheduler = Schedulers
            .newParallel("publisher", 1);

        // Create a subscriber that handles backpressure.
        mSubscriber =
            new AdaptiveBackpressureSubscriber(mPendingItemCount);

        // Create a composite disposable that disposes of everything
        // in one fell swoop.
        mDisposables = Disposables
            .composite(mPublisherScheduler,
                       mSubscriberScheduler,
                       mSubscriber);
    }

    /**
     * Run all the tests and print the results.
     */
    private void run() {
        // Create a concurrent hash map.
        ConcurrentHashMap<Integer, Integer> concurrentHashMap =
            new ConcurrentHashMap<>();

        // Create and time the use of a concurrent hash map.
        Function<Integer, Integer> concurrentHashMapMemoizer =
            timeTest(new Memoizer<>(this::isPrime,
                                    concurrentHashMap),
                     "concurrentHashMapMemoizer");

        // Dispose of all schedulers and subscribers.
        mDisposables.dispose();

        // Print the results.
        System.out.println(RunTimer.getTimingResults());

        // Demonstrate slicing on the concurrent hash map.
        demonstrateSlicing(concurrentHashMap);
    }

    /**
     * Time {@code testName} using the given {@code hashMap}.
     *
     * @param memoizer The memoizer used to cache the prime candidates.
     * @param testName The name of the test.
     * @return The memoizer updated during the test.
     */
    private Function<Integer, Integer> timeTest
        (Function<Integer, Integer> memoizer,
         String testName) {
        // Return the memoizer updated during the test.
        return RunTimer
            // Time how long this test takes to run.
            .timeRun(() ->
                     // Run the test using the given memoizer.
                     runTest(memoizer, testName),
                     testName);
    }

    /**
     * Run the prime number test.
     * 
     * @param memoizer A cache that maps candidate primes to their
     * smallest factor (if they aren't prime) or 0 if they are prime
     * @param testName Name of the test
     * @return The memoizer updated during the test.
     */
    private Function<Integer, Integer> runTest
        (Function<Integer, Integer> memoizer,
         String testName) {
        Options.display("Starting "
                        + testName
                        + " with count = "
                        + Options.instance().count());

        // Reset the counters.
        mPrimeCheckCounter.set(0);
        mPendingItemCount.set(0);

        // Create a countdown latch that causes the main thread to
        // block until all the processing is done.
        CountDownLatch latch = new CountDownLatch(1);

        // Create a publisher that runs on its own scheduler.
        Flux<Integer> publisher = publisher(mPublisherScheduler);

        publisher
            // Arrange to release the latch when the subscriber is done.
            .doOnTerminate(latch::countDown)

            // Run the subscriber in a different thread.
            .publishOn(mSubscriberScheduler, mSubscriber.nextRequestSize())

            // Check each random number to see if it's prime.
            .map(number -> checkIfPrime(number, memoizer))

            // Start the wheels in motion.
            .subscribe(mSubscriber);

        Options.display("waiting in the main thread");

        // Wait for all processing to complete.
        ExceptionUtils.rethrowRunnable(latch::await);

        Options.display("Leaving "
                        + testName
                        + " with "
                        + mPrimeCheckCounter.get()
                        + " prime checks ("
                        + (Options.instance().count()
                              - mPrimeCheckCounter.get())
                        + ") duplicates");

        // Return the memoizer updated during the test.
        return memoizer;
    }

    /**
     * Publish a stream of random large numbers.
     *
     * @param scheduler Scheduler to publish the numbers on.
     * @return Return a flux that publishes random large numbers
     */
    private Flux<Integer> publisher(Scheduler scheduler) {
        // Iterate through all the random numbers.
        final Iterator<Integer> iterator =
            mRandomIntegers.iterator();

        return Flux
            // Generate a flux of random integers.
            .<Integer>create(sink -> sink.onRequest(size -> {
                        Options.display("Request size = " + size);

                        // Try to publish size items.
                        for (int i = 0;
                             i < size;
                             ++i) {
                            // Keep going if there is an item remaining
                            // in the iterator.
                            if (iterator.hasNext()) {
                                // Get the next item.
                                Integer item = iterator.next();

                                Options.display("published item: "
                                        + item
                                        + ", pending items = "
                                        + mPendingItemCount.incrementAndGet());

                                // Publish the next item.
                                sink.next(item);
                            } else {
                                // We're done publishing all the items.
                                sink.complete();
                                break;
                            }
                        }
                    }))

            // Subscribe on the given scheduler.
            .subscribeOn(scheduler);
    }

    /**
     * Check if {@code primeCandidate} is prime or not.
     * 
     * @param primeCandidate The number to check if it's prime
     * @param memoizer A cache that avoids rechecking if a # is prime
     * @return A {@code Result} object that contains the original
     * {@code primeCandidate} and either 0 if it's prime or its
     * smallest factor if it's not prime.
     */
    private Result checkIfPrime(Integer primeCandidate,
                                Function<Integer, Integer> memoizer) {
        // Return a tuple containing the prime candidate and the
        // result of checking if it's prime.
        return new Result(primeCandidate,
                          memoizer.apply(primeCandidate));
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
                    Options.display(" Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return factor;

        return 0;
    }

    /**
     * Demonstrate how to slice by applying the Java streams {@code
     * dropWhile()} and {@code takeWhile()} operations to the {@code
     * map} parameter.
     */
    private void demonstrateSlicing(Map<Integer, Integer> map) {
        // Sort the map by its values.
        var sortedMap = sortMap(map, comparingByValue());

        // Print out the entire contents of the sorted map.
        Options.display("map sorted by value = \n" + sortedMap);

        // Print out the prime numbers using takeWhile().
        printPrimes(sortedMap);

        // Print out the non-prime numbers using dropWhile().
        printNonPrimes(sortedMap);
    }
    
    /**
     * Print out the prime numbers in the {@code sortedMap}.
     */
    private void printPrimes(Map<Integer, Integer> sortedMap) {
        // Create a list of prime integers.
        List<Integer> primes = sortedMap
            // Get the EntrySet of the map.
            .entrySet()
            
            // Convert the EntrySet into a stream.
            .stream()

            // Slice the stream using a predicate that stops after a
            // non-prime number (i.e., getValue() != 0) is reached.
            .takeWhile(entry -> entry.getValue() == 0)

            // Map the EntrySet into just the key.
            .map(Map.Entry::getKey)

            // Collect the results into a list.
            .collect(toList());

        // Print out the list of primes.
        Options.display("primes =\n" + primes);
    }

    /**
     * Print out the non-prime numbers and their factors in the {@code
     * sortedMap}.
     */
    private void printNonPrimes(Map<Integer, Integer> sortedMap) {
        // Create a list of non-prime integers and their factors.
        List<Map.Entry<Integer, Integer>> nonPrimes = sortedMap
            // Get the EntrySet of the map.
            .entrySet()
            
            // Convert the EntrySet into a stream.
            .stream()

            // Slice the stream using a predicate that skips over the
            // non-prime numbers (i.e., getValue() == 0);
            .dropWhile(entry -> entry.getValue() == 0)

            // Collect the results into a list.
            .collect(toList());

        // Print out the list of primes.
        Options.display("non-prime numbers and their factors =\n"
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
        return map
            // Get the EntrySet of the map.
            .entrySet()
            
            // Convert the EntrySet into a stream.
            .stream()

            // Sort the elements in the stream using the comparator.
            .sorted(comparator)

            // Trigger intermediate processing and collect key/value
            // pairs in the stream into a LinkedHashMap, which
            // preserves the sorted order.
            .collect(toMap(Map.Entry::getKey,
                           Map.Entry::getValue,
                           (e1, e2) -> e2,
                           LinkedHashMap::new));
    }
}
    
