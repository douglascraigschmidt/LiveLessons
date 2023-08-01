import utils.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static utils.MapUtils.sortMap;
import static utils.PrimeUtils.printNonPrimes;
import static utils.PrimeUtils.printPrimes;

/**
 * This example showcases and benchmarks the use of a Java Streams
 * functional programming features in the context of a {@link
 * Memoizer} configured with either a Java {@link ConcurrentHashMap},
 * a Java {@link Collections} {@code SynchronizedMap}, and a {@link
 * HashMap} protected with a Java {@link StampedLock}.  This {@link
 * Memoizer} is used to compute/cache/retrieve large prime numbers
 * concurrently via Java sequential or parallel streams.  This example
 * also demonstrates the Java record data type, several advanced
 * features of {@link StampedLock}, and the use of slicing with the
 * Java streams {@code takeWhile()} and {@code dropWhile()}
 * operations.
 */
public class ex9 {
    /**
     * Count the number of pending items.
     */
    private final AtomicInteger mPendingItemCount =
        new AtomicInteger(0);

    /**
     * A list of randomly-generated large {@link Integer} objects.
     */
    private final List<Integer> mRandomIntegers;

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Create and run the tests.
        new ex9(argv).run();
    }

    /**
     * Constructor initializes the fields.
     */
    ex9(String[] argv) {
        // Parse the command-line arguments.
        Options.instance().parseArgs(argv);

        // Generate random data for use by the various hashmaps.
        mRandomIntegers = RandomUtils.generateRandomData
            (Options.instance().count(),
             Options.instance().maxValue());
    }

    /**
     * Run all the tests and print the results.
     */
    private void run() {
        // Create a StampedLockHashMap.
        Map<Integer, Integer> stampedLockHashMap =
            new StampedLockHashMap<>();

        // Create and time the use of a SynchronizedHashMap.
        Function<Integer, Integer> synchronizedHashMapMemoizer =
            timeTest(new Memoizer<>
                     (PrimeUtils::isPrime,
                      Collections.synchronizedMap(new HashMap<>())),
                     "synchronizedHashMapMemoizer");

        // Create and time the use of a ConcurrentHashMap.
        Function<Integer, Integer> concurrentHashMapMemoizer =
            timeTest(new Memoizer<>
                     (PrimeUtils::isPrime,
                      new ConcurrentHashMap<>()),
                     "concurrentHashMapMemoizer");
        
        // Create and time the use of a StampedLockHashMap.
        Function<Integer, Integer> stampedLockHashMapMemoizer =
            timeTest(new Memoizer<>
                     (PrimeUtils::isPrime,
                      stampedLockHashMap),
                     "stampedLockHashMapMemoizer");                

        // Print the timing results.
        System.out.println(RunTimer.getTimingResults());

        // Print the results in the StampedLockHashMap.
        printResults(stampedLockHashMap);
    }

    /**
     * Time {@code testName} using the given {@code memoizer}.
     *
     * @param memoizer The memoizer used to cache the prime candidates
     * @param testName The name of the test
     * @return The memoizer updated during the test
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
     *                 smallest factor (if they aren't prime) or 0 if
     *                 they are prime
     * @param testName Name of the test
     * @return The memoizer updated during the test
     */
    private Function<Integer, Integer> runTest
        (Function<Integer, Integer> memoizer,
         String testName) {
        Options.print("Starting "
                      + testName
                      + " with count = "
                      + Options.instance().count());

        // Reset the counter.
        Options.instance().primeCheckCounter().set(0);

        this
            // Generate a stream of random large numbers.
            .publishRandomIntegers(Options.instance().parallel())

            // Print stats if we're debugging.
            .peek(item -> Options
                  .debug("processed item: "
                         + item
                         + ", publisher pending items: "
                         + mPendingItemCount.incrementAndGet()))

            // Check each random number to see if it's prime.
            .map(number ->
                 PrimeUtils.checkIfPrime(number,
                                         memoizer))
            
            // Handle the results.
            .forEach(this::handleResult);

        Options.print("Leaving "
                      + testName
                      + " with "
                      + Options.instance().primeCheckCounter().get()
                      + " prime checks ("
                      + (Options.instance().count()
                         - Options.instance().primeCheckCounter().get())
                      + " duplicates)");

        // Return the memoizer updated during the test.
        return memoizer;
    }

    /**
     * Publish a sequential or parallel stream of random large {@link
     * Integer} objects.
     *
     * @param parallel True if the stream should be parallel, else
     *                 false
     * @return Return a stream containing random large numbers
     */
    private Stream<Integer> publishRandomIntegers
        (boolean parallel) {
        Stream<Integer> intStream = mRandomIntegers
            // Convert the list into a stream.
            .stream();

        // Conditionally convert the stream to a parallel stream.
        if (parallel)
            intStream.parallel();

        // Return the stream.
        return intStream;
    }

    /**
     * Handle the result by printing it if debugging is enabled.
     *
     * @param result The result of checking if a number is prime
     */
    private void handleResult(PrimeUtils.PrimeResult result) {
        // Print the results.
        if (result.smallestFactor() != 0) {
            Options.debug(result.primeCandidate()
                          + " is not prime with smallest factor "
                          + result.smallestFactor());
        } else {
            Options.debug(result.primeCandidate()
                          + " is prime");
        }

        Options.debug("consumer pending items: "
                      + mPendingItemCount.decrementAndGet());
    }

    /**
     * Print the results in the {@link Map}.
     */
    private void printResults(Map<Integer, Integer> map) {
        // Sort the map by its values.
        var sortedMap = sortMap(map, comparingByValue());

        // Print out the entire contents of the sorted map.
        Options.print("map sorted by value = \n" + sortedMap);

        // Print out the prime numbers using takeWhile().
        printPrimes(sortedMap);

        // Print out the non-prime numbers using dropWhile().
        printNonPrimes(sortedMap);
    }
}
    
