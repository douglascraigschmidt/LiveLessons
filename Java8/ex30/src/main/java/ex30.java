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
 * ConcurrentHashMap and various memoizer implementations to
 * compute/cache/retrieve large prime numbers.
 */
public class ex30 {
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
     * Main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Create an instance to test.
        ex30 test = new ex30(argv);

        // Run the tests.
        test.run();
    }

    /**
     * Constructor initializes the fields.
     */
    ex30(String[] argv) {
        // Initialize this count to 0.
        mPrimeCheckCounter = new AtomicInteger(0);

        // Initialize this count to 0.
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
    }

    /**
     * Run all the tests and print the results.
     */
    private void run() {
        // Create and time the use of a concurrent hash map.
        Function<Integer, Integer> concurrentHashMapMemoizer =
            timeTest(Options.makeMemoizer(this::isPrime),
                     "concurrentHashMapMemoizer");

        // Print the results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Time {@code testName} using the given {@code hashMap}.
     *
     * @param memoizer The memoizer used to cache the prime candidates.
     * @param testName The name of the test.
     * @return The memoizer updated during the test.
     */
    private Function<Integer, Integer> timeTest(Function<Integer, Integer> memoizer,
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
    private Function<Integer, Integer> runTest(Function<Integer, Integer> memoizer,
                                               String testName) {
        Options.print("Starting "
                        + testName
                        + " with count = "
                        + Options.instance().count());

        // Reset the counter.
        mPrimeCheckCounter.set(0);

        this
            // Generate random large numbers.
            .publisher(Options.instance().parallel())

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
     * Publish a stream of random large numbers.
     *
     * @param parallel True if the stream should be parallel, else false
     * @return Return a stream containing random large numbers
     */
    private Stream<Integer> publisher(boolean parallel) {
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
            Options.debug(result.mPrimeCandidate
                            + " is not prime with smallest factor "
                            + result.mSmallestFactor);
        } else {
            Options.debug(result.mPrimeCandidate
                    + " is prime");
        }
        Options.debug("consumer pending items: "
                        + mPendingItemCount.decrementAndGet());
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
                    Options.debug(" Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return factor;

        return 0;
    }

    /**
     * The result returned from {@code checkIfPrime()}.
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
    
