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
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * This example showcases and benchmarks the use of a Java
 * object-oriented and functional programming features in the context
 * of a Java ConcurrentHashMap, a Java SynchronizedMap, and a HashMap
 * protected with a Java StampedLock used to compute/cache/retrieve
 * large prime numbers.  This example also demonstrates the Java
 * record data type, several advanced features of StampedLock, and the
 * use of slicing with the Java streams takeWhile() and dropWhile()
 * operations.
 */
public class ex9 {
    /**
     * Count the number of calls to isPrime() as a means to determine
     * the benefits of caching.
     */
    private final AtomicInteger mPrimeCheckCounter =
        new AtomicInteger(0);

    /**
     * Count the number of pending items.
     */
    private final AtomicInteger mPendingItemCount =
        new AtomicInteger(0);

    /**
     * A list of randomly-generated large integers.
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
        mRandomIntegers = generateRandomData();
    }

    /**
     * Generate random data for use by the various hashmaps.
     *
     * @return A {@link List} of random {@link Integer} objects
     */
    private List<Integer> generateRandomData() {
        // Get how many integers we should generate.
        int count = Options.instance().count();

        // Get the max value for the random numbers.
        int maxValue = Options.instance().maxValue();

        // Generate a list of random large integers.
        return new Random()
                // Generate a stream of "count" random large ints.
                .ints(count,
                        // Try to generate duplicates.
                        maxValue - count,
                        maxValue)

                // Convert each primitive int to Integer.
                .boxed()

                // Trigger intermediate operations and collect into list.
                .collect(toList());
    }

    /**
     * Run all the tests and print the results.
     */
    private void run() {
        // Create a StampedLockHashMap.
        Map<Integer, Integer> stampedLockHashMap =
            new StampedLockHashMap<>();

        // Create and time the use of a synchronized hash map.
        Function<Integer, Integer> synchronizedHashMapMemoizer =
            timeTest(new Memoizer<>(this::isPrime,
                                    Collections.synchronizedMap(new HashMap<>())),
                     "synchronizedHashMapMemoizer");

        // Create and time the use of a concurrent hash map.
        Function<Integer, Integer> concurrentHashMapMemoizer =
            timeTest(new Memoizer<>(this::isPrime,
                                    new ConcurrentHashMap<>()),
                     "concurrentHashMapMemoizer");
        
        // Create and time the use of a stamped lock hash map.
        Function<Integer, Integer> stampedLockHashMapMemoizer =
            timeTest(new Memoizer<>(this::isPrime,
                                    stampedLockHashMap),
                     "stampedLockHashMapMemoizer");                

        // Print the results.
        System.out.println(RunTimer.getTimingResults());

        // Demonstrate slicing on the stamped lock memoizer.
        demonstrateSlicing(stampedLockHashMap);
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
     * smallest factor (if they aren't prime) or 0 if they are prime
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
        mPrimeCheckCounter.set(0);

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
            .map(number -> checkIfPrime(number, memoizer))
            
            // Handle the results.
            .forEach(this::handleResult);

        Options.print("Leaving "
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
     * Publish a stream of random large {@link Integer} objects.
     *
     * @param parallel True if the stream should be parallel, else false
     * @return Return a stream containing random large numbers
     */
    private Stream<Integer> publishRandomIntegers(boolean parallel) {
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
     * Check if {@code primeCandidate} is prime or not.
     * 
     * @param primeCandidate The number to check for primality
     * @param memoizer A cache that avoids rechecking if a number is prime
     * @return A {@link PrimeResult} record that contains the original
     * {@code primeCandidate} and either 0 if it's prime or its
     * smallest factor if it's not prime.
     */
    private PrimeResult checkIfPrime(Integer primeCandidate,
                                     Function<Integer, Integer> memoizer) {
        // Return a record containing the prime candidate and the
        // result of checking if it's prime.
        return new PrimeResult(primeCandidate,
                               memoizer.apply(primeCandidate));
    }

    /**
     * Handle the result by printing it if debugging is enabled.
     *
     * @param result The result of checking if a number is prime
     */
    private void handleResult(PrimeResult result) {
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
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.
     *
     * @return 0 if it is prime or the smallest factor if it is not prime
     */
    private Integer isPrime(Integer primeCandidate) {
        // Increment the counter to indicate a prime candidate wasn't
        // already in the cache.
        mPrimeCheckCounter.incrementAndGet();

        int n = primeCandidate;

        if (n > 3)
            // This "brute force" algorithm is intentionally
            // inefficient to burn lots of CPU time!
            for (int factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (Thread.interrupted()) {
                    Options.debug(" Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return factor;

        return 0;
    }

    /**
     * Demonstrate how to slice by applying the Java streams {@code
     * dropWhile()} and {@code takeWhile()} operations to the {@link
     * Map} parameter.
     */
    private void demonstrateSlicing(Map<Integer, Integer> map) {
        // Sort the map by its values.
        var sortedMap = sortMap(map, comparingByValue());

        // Print out the entire contents of the sorted map.
        Options.print("map sorted by value = \n" + sortedMap);

        // Print out the prime numbers using takeWhile().
        printPrimes(sortedMap);

        // Print out the non-prime numbers using dropWhile().
        printNonPrimes(sortedMap);
    }
    
    /**
     * Print out the prime numbers in {@code sortedMap}.
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
        Options.print("primes =\n" + primes);
    }

    /**
     * Print out the non-prime numbers and their factors in {@code
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
        Options.print("non-prime numbers and their factors =\n"
                      + nonPrimes);
    }

    /**
     * Sort {@code map} via the {@code comparator}.
     *
     * @param map The map to sort
     * @param comparator The comparator to compare map entries
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
    
