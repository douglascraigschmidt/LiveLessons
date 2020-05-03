import utils.Memoizer;
import utils.Options;
import utils.RunTimer;
import utils.StampedLockHashMap;

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
 * ConcurrentHashMap, a Java SynchronizedMap, and a HashMap protected
 * with a Java StampedLock are used to compute/cache/retrieve large
 * prime numbers.  This example also demonstrates several advanced
 * features of StampedLock, as well as the use of slicing with the
 * Java streams takeWhile() and dropWhile() operations.
 */
public class ex9 {
    /**
     * Count the number of calls to isPrime() as a means to determine
     * the benefits of caching.
     */
    private final AtomicInteger mPrimeCheckCounter;

    /**
     * A list of randomly-generated large integers.
     */
    private final List<Integer> mRandomIntegers;

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Create an instance to test.
        ex9 test = new ex9(argv);

        // Run the tests.
        test.run();
    }

    /**
     * Constructor initializes the fields.
     */
    ex9(String[] argv) {
        // Initialize this count to 0.
        mPrimeCheckCounter = new AtomicInteger(0);

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
            timeTest(new Memoizer<>
                     (this::isPrime,
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
        System.out.println("Starting " 
                           + testName
                           + " with count = "
                           + Options.instance().count());

        // Reset the counter.
        mPrimeCheckCounter.set(0);

        this
            // Generate random large numbers.
            .emitter(true)
            
            // Check each random number to see if it's prime.
            .map(number -> checkIfPrime(number, memoizer))
            
            // Handle the results.
            .forEach(this::handleResult);

        System.out.println("Leaving "
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
     * Emit a stream of random large numbers.
     *
     * @param parallel True if the stream should be parallel, else false
     * @return Return a stream containing random large numbers
     */
    private Stream<Integer> emitter(boolean parallel) {
        Stream<Integer> intStream = mRandomIntegers
            // Conver the list into a stream.
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
     * Handle the result by printing it if debugging is enabled.
     *
     * @param result The result of checking if a number is prime.
     */
    private void handleResult(Result result) {
        // Print the results.
        if (result.mSmallestFactor != 0) {
            Options.display(""
                            + Thread.currentThread()
                            + ": "
                            + result.mPrimeCandidate
                            + " is not prime with smallest factor "
                            + result.mSmallestFactor);
        } else {
            Options.display(""
                            + Thread.currentThread()
                            + ": "
                            + result.mPrimeCandidate
                            + " is prime");
        }
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
                    System.out.println(""
                                       + Thread.currentThread()
                                       + " Prime checker thread interrupted");
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
        System.out.println("map sorted by value = \n" + sortedMap);

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
        System.out.println("primes =\n" + primes);
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
        System.out.println("non-prime numbers and their factors =\n"
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

    /**
     * The result returned from {@code transform()}.
     */
    private static class Result {
        /**
         * Value that was evaluated for primality.
         */
        int mPrimeCandidate;

        /**
         * Result of the isPrime() method.
         */
        int mSmallestFactor;

        /**
         * Constructor initializes the fields.
         */
        public Result(int primeCandidate, int smallestFactor) {
            mPrimeCandidate = primeCandidate;
            mSmallestFactor = smallestFactor;
        }
    }
}
    
