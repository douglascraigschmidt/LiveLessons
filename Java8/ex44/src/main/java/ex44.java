import utils.Memoizer;
import utils.Options;
import utils.RunTimer;
import utils.StreamOfFuturesCollector;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * This example demonstrates benefits of combining object-oriented and
 * functional programming in modern Java.  It uses the Java parallel
 * streams and completable futures frameworks to compose a pipeline of
 * functions (i.e., aggregate operations) that check {@code
 * sMAX_COUNT} positive odd random numbers in parallel to determine
 * which are prime and which aren't.
 *
 * An object-oriented {@link Memoizer} cache is used to optimize
 * performance by returning results from previous (duplicate)
 * primality checks rather than recomputing them again.  Although this
 * cache is mutable state shared between multiple threads, it works
 * correctly and efficiently since the Java {@link ConcurrentHashMap}
 * concurrent collection is used in the {@link Memoizer}
 * implementation.
 *
 * This example also shows how to use the Java {@code record} type to
 * store immutable data fields.
 */
@SuppressWarnings("SameParameterValue")
public class ex44 {
    /**
     * The number of positive odd random numbers to check for
     * primality.
     */
    private static final int sMAX_COUNT = 2_000_000;

    /**
     * Records the number of calls to {@code isPrime()} to demonstrate
     * the benefits of using a {@link Memoizer} cache.
     */
    private static final AtomicInteger sNonDuplicateCount =
        new AtomicInteger();

    /**
     * Records the total count of prime numbers found during the
     * checks.
     */
    private static final AtomicInteger sPrimeCount =
        new AtomicInteger();

    /**
     * A {@link List} of {@code sMAX_COUNT} odd random numbers.
     */
    private static final List<Integer> sRandomNumbers =
        generateOddRandomNumbers(new Random(), sMAX_COUNT);

    /**
     * A "memoizing" cache that demonstrates how shared mutable state
     * can optimize the performance of a concurrent Java program.
     */
    private static final Memoizer<Integer, Integer> sMemoizer =
        new Memoizer<>(ex44::isPrime);

    /**
     * A {@link Function} that uses a {@link Memoizer} to check an
     * {@link Integer} for primality.
     */
    private static final Function<Integer,
        PrimeResult> sMemoized = primeCandidate ->
        checkIfPrime(primeCandidate, sMemoizer);

    /**
     * A {@link Function} that does not use a {@link Memoizer} to
     * check an {@link Integer} for primality.
     */
    private static final Function<Integer,
        PrimeResult> sNotMemoized = primeCandidate ->
        checkIfPrime(primeCandidate, null);

    /**
     * The main entry point into this program.
     */
    static public void main(String[] argv) {
        ex44.display("Entering test for primality of " + sMAX_COUNT + " numbers");

        Options.instance().parseArgs(argv);

        // Warmup the cache, but does not record the time taken to
        // perform the computations.
        checkForPrimes(sRandomNumbers, sNotMemoized);

        RunTimer
            // Record the time needed to perform the computations.
            .timeRun(() ->
                     // Do not use a memoizer to check odd random
                     // numbers in parallel to determine which are
                     // prime and which aren't.
                     checkForPrimes(sRandomNumbers,
                                    sNotMemoized),
                     "checkForPrimes() not-memoized");

        // Print the statistics for the non-memoized version.
        printStatistics("checkForPrimes() not-memoized");

        RunTimer
            // Record the time needed to perform the computations.
            .timeRun(() ->
                     // Use a memoizer to check odd random numbers in
                     // parallel to determine which are prime and
                     // which aren't.
                     checkForPrimes(sRandomNumbers,
                                    sMemoized),
                     "checkForPrimes() memoized");

        // Print the statistics for the memoized version.
        printStatistics("checkForPrimes() memoized");

        RunTimer
            // Record the time needed to perform the computations.
            .timeRun(() ->
                     // Use a memoizer to check odd random numbers in
                     // parallel to determine which are prime and
                     // which aren't.
                     checkForPrimes(sRandomNumbers,
                                    sMemoized),
                     "checkForPrimes() all memoized");

        // Print the statistics for the all memoized version.
        printStatistics("checkForPrimes() all memoized");

        // Clear the cache for the async test.
        sMemoizer.getCache().clear();

        RunTimer
            // Record the time needed to perform the computations.
            .timeRun(() ->
                     // Use a memoizer to check odd random numbers in
                     // parallel to determine which are prime and
                     // which aren't.
                     checkForPrimesAsync(sRandomNumbers,
                                         sMemoized),
                     "checkForPrimesAsync() memoized");

        // Print the statistics for the async memoized version.
        printStatistics("checkForPrimesAsync() memoized");

        // Print the timing results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Check a {@link List} of positive odd random numbers in parallel
     * and print which are prime and which aren't.
     *
     * @param randomNumbers The {@link List} of positive odd random
     *                      numbers to check for primality
     * @param primeChecker The {@link Function} that checks for
     *                     primality
     */
    static void checkForPrimes(List<Integer> randomNumbers,
                               Function<Integer, PrimeResult> primeChecker) {
        // Reset the counters.
        sNonDuplicateCount.set(0);
        sPrimeCount.set(0);

        randomNumbers
            // Convert List into a parallel stream.
            .parallelStream()

            // Check each odd number to see if it's prime.
            .map(primeChecker)

            // Trigger intermediate operations and handle the results.
            .forEach(ex44::handleResults);
    }

    /**
     * Check a {@link List} of positive odd random numbers in parallel
     * and print which are prime and which aren't.
     *
     * @param randomNumbers The {@link List} of positive odd random
     *                      numbers to check for primality
     * @param primeChecker The {@link Function} that checks for
     *                     primality
     */
    static void checkForPrimesAsync(List<Integer> randomNumbers,
                                    Function<Integer, PrimeResult> primeChecker) {
        // Reset the counters.
        sNonDuplicateCount.set(0);
        sPrimeCount.set(0);

        randomNumbers
            // Convert List into a parallel stream.
            .stream()

            // Asynchronously check each odd number to see if it's prime.
            .map(oddNumber -> CompletableFuture
                 .supplyAsync(() -> primeChecker.apply(oddNumber)))

            // Trigger intermediate operations and return a single
            // CompletableFuture that completes when all the other
            // CompletableFuture objects complete.
            .collect(StreamOfFuturesCollector.toFuture())

            // Handle the results after all asynchronous processing completes.
            .thenAccept(stream -> stream
                        .forEach(ex44::handleResults))

            // Wait for all the async computations to complete.
            .join();
    }

    /**
     * This predicate returns true if the {@code integer} param is an
     * odd number, else false.
     *
     * @param integer The parameter to check for oddness
     * @return true if the {@code integer} param is an odd number,
     *         else false
     */
    private static boolean isOdd(int integer) {
        // Use the bit-wise and operator, which returns 'true' if
        // 'integer' is odd and 'false' otherwise.
        return (integer & 1) == 1;
    }

    /**
     * Check if {@code primeCandidate} is prime or not.
     *
     * @param primeCandidate The number to check for primality
     * @param memoizer A "memoizing" cache that optimizes performance for
     *        primality checking
     * @return A {@link PrimeResult} record that contains the original
     *         {@code primeCandidate} and either 0 if it's prime or
     *         its smallest factor if it's not prime.
     */
    private static PrimeResult checkIfPrime(int primeCandidate,
                                            Memoizer<Integer, Integer> memoizer) {
        // Return a record containing the prime candidate and the
        // result of checking if it's prime.
        return new PrimeResult(primeCandidate,
                               memoizer != null
                               // Get the result from memoizer to
                               // accelerate primality determination.
                               ? memoizer.get(primeCandidate)

                               // Call the isPrime() method directly.
                               : isPrime(primeCandidate));
    }

    /**
     * Define a Java record that holds the "plain old data" (POD) for
     * the result of a primality check.  Unlike a Java Object, a Java
     * record contains no "hidden" fields, e.g., a vptr, intrinsic
     * lock, intrinsic condition, etc.
     */
    record PrimeResult(/*
                        * Value that was evaluated for primality.
                        */
                       int primeCandidate,

                       /*
                        * Result of calling {@code isPrime()} on
                        * {@code primeCandidate}.
                        */
                       int smallestFactor) {}

    /**
     * Determine whether {@code primeCandidate} is prime and
     * return the result.
     *
     * @param primeCandidate The number to check for primality
     * @return 0 if prime or the smallest factor if not prime
     */
    private static Integer isPrime(int primeCandidate) {
        // Increment the count of non-duplicates, i.e., integers that
        // weren't found in the Memoizer cache.
        sNonDuplicateCount.incrementAndGet();

        // Check if primeCandidate is a multiple of 2.
        if (primeCandidate % 2 == 0)
            // Return smallest factor for non-prime number.
            return 2;

        // If not, then just check the odds for primality.
        for (int factor = 3;
             factor * factor <= primeCandidate;
             // Skip over even numbers.
             factor += 2)
            if (primeCandidate % factor == 0)
                // primeCandidate was not prime.
                return factor;

        // primeCandidate was prime.
        return 0;
    }

    /**
     * Returns a {@link List} of {@code count} odd random numbers
     * containing duplicates.
     *
     * @param random The {@link Random} number generator
     * @param count The number of odd random numbers to generate
     * @return A {@link List} of {@code count} odd random numbers
     *         containing duplicates
     */
    static List<Integer> generateOddRandomNumbers(Random random,
                                                  int count) {
        return random
            // Generate an infinite stream of random positive ints in
            // a range that tries to ensure duplicates.
            .ints( Integer.MAX_VALUE - count,
                   Integer.MAX_VALUE)

            // Eliminate even numbers.
            .filter(ex44::isOdd)

            // Limit to 'count' odd random numbers.
            .limit(count)

            // Convert the ints into Integers.
            .boxed()

            // Trigger intermediate operations and collect the results
            // into a List.
            .toList();
    }
    /**
     * Print the {@code result}.
     *
     * @param result The result of checking if a number is prime
     */
    private static void handleResults(PrimeResult result) {
        // Check if number was not prime.
        if (result.smallestFactor() != 0) {
            Options.display(result.primeCandidate()
                            + " is not prime with smallest factor "
                            + result.smallestFactor());

        } else {
            // Increment the count of prime numbers.
            sPrimeCount.getAndIncrement();

            Options.display(result.primeCandidate()
                            + " is prime");
        }
    }

    /**
     * Display the {@code output} after prepending the current {@link
     * Thread} id.

     * @param output The {@code output} to display
     */
    private static void display(String output) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + output);
    }

    /**
     * Print statistics about the test run.
     *
     * @param testName Name of the test
     */
    private static void printStatistics(String testName) {
        ex44.display(sPrimeCount
                     + " prime #'s and "
                     + sNonDuplicateCount.get()
                     + " isPrime() calls in "
                     + testName
                     + " test");
    }
}

