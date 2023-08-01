import jdk.incubator.concurrent.StructuredTaskScope;
import utils.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

import static java.util.Map.Entry.comparingByValue;
import static utils.ExceptionUtils.rethrowConsumer;
import static utils.MapUtils.sortMap;
import static utils.PrimeUtils.*;
import static utils.RandomUtils.generateRandomData;

/**
 * This example showcases and benchmarks the use of Java
 * object-oriented and functional programming features in the context
 * of a {@link Memoizer} configured with either a Java {@link
 * ConcurrentHashMap}, a Java {@link Collections} {@code
 * SynchronizedMap}, and a {@link HashMap} protected by a Java {@link
 * StampedLock}.  This {@link Memoizer} is used to compute, cache, and
 * retrieve large prime numbers concurrent via Java structured
 * concurrency and virtual Thread objects.  This example also
 * demonstrates the Java {@code record} data type and several advanced
 * features of {@link StampedLock}.
 */
public class ex47 {
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
        new ex47(argv).run();
    }

    /**
     * Constructor initializes the fields.
     */
    ex47(String[] argv) {
        // Parse the command-line arguments.
        Options.instance().parseArgs(argv);

        // Generate random data for use by the various hashmaps.
        mRandomIntegers = generateRandomData
            (Options.instance().count(),
             Options.instance().maxValue());
    }

    /**
     * Run all the tests and print the results.
     */
    private void run() {
        // Create and time the use of a Memoizer configured with a
        // SynchronizedHashMap, which uses a single lock.
        timeTest(new Memoizer<>
                 (PrimeUtils::isPrime,
                  Collections.synchronizedMap(new HashMap<>())),
                 "synchronizedHashMapMemoizer");

        // Create and time the use of a Memoizer configured with a
        // ConcurrentHashMap, which uses a lock per hash table
        // "bucket".
        timeTest(new Memoizer<>
                 (PrimeUtils::isPrime,
                  new ConcurrentHashMap<>()),
                 "concurrentHashMapMemoizer");

        // Create a StampedLockHashMap, which uses various features of
        // a StampedLock.
        Map<Integer, Integer> stampedLockHashMap =
            new StampedLockHashMap<>();

        // Create and time the use of a Memoizer configured with a
        // StampedLockHashMap.
        timeTest(new Memoizer<>
                 (PrimeUtils::isPrime,
                  stampedLockHashMap),
                 "stampedLockHashMapMemoizer");                

        // Print the timing results.
        System.out.println(RunTimer.getTimingResults());

        // Print the results the StampedLockHashMap object.
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
     * Run the prime number test using Java structured concurrency.
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

        // Create a List of Future<PrimeResult> objects of the given
        // size.
        List<Future<PrimeResult>> results =
            new ArrayList<>(mRandomIntegers.size());

        // Create a new scope to execute virtual Thread objects.
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Iterate through all the random Integer objects.
            for (Integer randomInteger : mRandomIntegers) {
                Options
                    .debug("processed item: "
                           + randomInteger
                           + ", publisher pending items: "
                           + mPendingItemCount.incrementAndGet());

                results
                    // Add the Future<PrimeResult> to the List.
                    .add(scope.
                         // Create a virtual Thread to run the computation.
                         fork(() ->
                              // Check each number to see if it's prime.
                              checkIfPrime(randomInteger,
                                           memoizer)));
            }

            // This barrier synchronizer waits for all Thread objects
            // to finish or throw any exception that occurred.
            scope.join().throwIfFailed();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        results
            // Handle each result.
            .forEach(resultFuture -> 
                     handleResult(resultFuture.resultNow()));

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
     * Handle the result by printing it if debugging is enabled.
     *
     * @param result The result of checking if a number is prime
     */
    private void handleResult(PrimeUtils.PrimeResult result) {
        // Print the results.
        if (result.smallestFactor() != 0)
            Options.debug(result.primeCandidate()
                          + " is not prime with smallest factor "
                          + result.smallestFactor());
        else
            Options.debug(result.primeCandidate()
                          + " is prime");

        Options.debug("consumer pending items: "
                      + mPendingItemCount.decrementAndGet());
    }

    /**
     * Print the results in the given {@link Map} object.
     */
    private void printResults(Map<Integer, Integer> map) {
        // Sort the map by its values.
        var sortedMap =
            sortMap(map, comparingByValue());

        // Print out the entire contents of the sorted map.
        Options.print("Map sorted by value = \n" + sortedMap);

        // Print out the prime numbers using takeWhile().
        printPrimes(sortedMap);

        // Print out the non-prime numbers using dropWhile().
        printNonPrimes(sortedMap);
    }
}
    
