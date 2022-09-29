import utils.*;

import static java.util.AbstractMap.SimpleImmutableEntry;
import static java.util.stream.Collectors.toList;
import static utils.ExceptionUtils.rethrowConsumer;
import static utils.ExceptionUtils.rethrowSupplier;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * This example shows various ways to implement and apply synchronous
 * and asynchronous memoizers using Java ExecutorService, functional
 * interfaces, streams, and completable futures.  Memoization is
 * described at https://en.wikipedia.org/wiki/Memoization.
 */
public class ex25 {
    /**
     * Default number of random numbers to generate.
     */
    private static int sDEFAULT_COUNT = 500;

    /**
     * Maximum value of random numbers.
     */
    private static long sMAX_VALUE = 1000000000L;

    /**
     * Number of threads to use.
     */
    private static int sNumberOfThreads = 10;

    /**
     * The list of random numbers.
     */
    private static List<Long> sRandomNumbers;

    /**
     * The unique set of random numbers, i.e., non-duplicate.
     */
    private static HashSet<Long> mUniqueRandomNumbers;


    /**
     * This object checks for primality using a synchronous memoizer.
     */ 
    private Function<Long, SimpleImmutableEntry<Long, Long>> sPrimeChecker =
        new Memoizer<>(PrimeCheckers::bruteForceChecker,
                       new ConcurrentHashMap());

    /**
     * This object checks for primality using an asynchronous
     * memoizer.
     */
    private Function<Long, CompletableFuture<SimpleImmutableEntry<Long, Long>>> sPrimeCheckerCF =
        new AsyncMemoizer<>(PrimeCheckers::bruteForceChecker);

    /**
     * Number of random numbers generated.
     */
    private static int sRandomNumberCount;

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv) throws InterruptedException {
        System.out.println("Starting ex25 test");

        // Conditionally override the default count.
        sRandomNumberCount = argv.length == 0 
            ? sDEFAULT_COUNT 
            : Integer.valueOf(argv[0]);

        // Generate a list of random numbers used by all the tests.
        sRandomNumbers = new Random()
            // Generate "mRandomNumberCount" random between sMAX_VALUE
            // - mRandomNumberCount and sMAX_VALUE.
            .longs(sRandomNumberCount,
                   sMAX_VALUE - sRandomNumberCount,
                   sMAX_VALUE)

            // Box longs into Longs.
            .boxed()

            // Collect the random numbers into a list.
            .collect(toList());

        // The set of unique random numbers.
        mUniqueRandomNumbers = new HashSet<>(sRandomNumbers);

        int totalDuplicates =
            sRandomNumbers.size() - mUniqueRandomNumbers.size();

        System.out.println("Total random numbers = "
                           + sRandomNumberCount
                           + "\nTotal duplicate random numbers = "
                           + totalDuplicates
                           + "\nTotal distinct random numbers = "
                           + (sRandomNumberCount - totalDuplicates));

        ex25 t = new ex25();

        // Stress-test multiple synchronous Memoizer implementations
        // to see how they perform when run concurrently.
        t.stressTestMemoizers();

        /*
        // Synchronously check the primality of count random numbers
        // using an ExecutorService with a fixed-size thread pool.
        t.testCallableMemoizer();

        // Asynchronously check the primality of count random numbers
        // using CompletableFuture.supplyAsync() with the common
        // fork-join pool.
        t.testSupplyAsyncMemoizer();

        // Asynchronously check the primality of count random numbers
        // using the AsyncMemoizer.
        t.testAsyncMemoizer();
        */

        System.out.println("Finishing ex25 test");
    }

    /**
     * Stress-test multiple synchronous Memoizer implementations to
     * see how they perform when run concurrently.
     */
    private void stressTestMemoizers() throws InterruptedException {
        System.out.println("Running stressTestMemoizers()");

        // Run with a non-synchronized HashMap.
        stressTestMemoizer(new Memoizer<>(PrimeCheckers::bruteForceChecker,
                                          new HashMap<>()),
                           "HashMap");

        // Run with a ConcurrentHashMap. 
        stressTestMemoizer(new Memoizer<>(PrimeCheckers::bruteForceChecker,
                                          new ConcurrentHashMap<>()),
                           "ConcurrentHashMap");

        // Run with a synchronized HashMap.
        stressTestMemoizer(new Memoizer<>(PrimeCheckers::bruteForceChecker,
                                          Collections.synchronizedMap(new HashMap<>())),
                           "Synchronized HashMap");
    }

    /**
     * Stress-test a synchronous Memoizer implementation to see how it
     * performs when run concurrently.
     */
    private void stressTestMemoizer(Memoizer<Long, SimpleImmutableEntry<Long, Long>> primeChecker,
                                    String hashMapName)
            throws InterruptedException {
        System.out.println("Running stressTestMemoizer() for "
                           + hashMapName);

        // The range of random numbers handled by each thread.
        final int range = sRandomNumberCount / sNumberOfThreads;

        // The list of threads that will perform the stress test.
        List<Thread> threads = new ArrayList<>();

        // Ensure all the threads start at the same moment.
        CyclicBarrier cyclicBarrier =
            new CyclicBarrier(sNumberOfThreads);

        // Keep track of the statistics for this hash map.
        StatCollector statCollector = new StatCollector();

        // Iterate through all the threads.
        for (int i = 0; i < sNumberOfThreads; i++) {
              // Determine the lower and upper bound of the
            // range of random numbers.
            int lowerBound = i * range;
            int upperBound = (i + 1) * range;

            // Create a new thread to perform primality checking for a
            // range of random numbers and add it to the list.
            threads.add(new Thread(makeRunnable(lowerBound, 
                                                upperBound,
                                                primeChecker,
                                                cyclicBarrier,
                                                statCollector)));
        }

        // Start all the threads running.
        for (Thread t : threads)
            t.start();

        // Wait for all the threads to finish.
        for (Thread t : threads)
            t.join();

        // Print the statistics for this type of hash map.
        statCollector.print(hashMapName,
                            mUniqueRandomNumbers.size());

        for (Long l : mUniqueRandomNumbers)
            if (primeChecker.remove(l) == null)
                System.out.println("remove failed for " 
                                   + l);
    }

    /**
     * This factory method creates a runnable that checks for the
     * primality of random numbers from {@code lowerBound} to {@code
     * upperBound}.
     */
    private Runnable makeRunnable(int lowerBound,
                                  int upperBound,
                                  Memoizer<Long, SimpleImmutableEntry<Long, Long>> primeChecker,
                                  CyclicBarrier cyclicBarrier,
                                  StatCollector statCollector) {
        // Return a runnable lambda.
        return () -> {
            try {
                // Wait for all threads to start running.
                cyclicBarrier.await();

                // Record the start time.
                long startTime = System.nanoTime();

                // Iterate through the random numbers in the range and
                // test them for primality.
                for (int j = lowerBound;
                     j < upperBound;
                     j++) {
                    // Get the 'jth' random number.
                    Long randomNumber = sRandomNumbers.get(j);

                    // Determine whether this number is prime or not.
                    SimpleImmutableEntry<Long, Long> result =
                        primeChecker.apply(Math.abs(randomNumber));

                    // Print the results.
                    // printResult(result);
                }

                // Record the stop time.
                long stopTime = (System.nanoTime() - startTime) / 1_000_000;

                // Update the statistics.
                statCollector.add(stopTime);

                // Run the garbage collector after each test.
                System.gc();
            } catch (Exception e) {
            }
        };
    }

    /**
     * Concurrently check the primality of count random numbers using
     * an ExecutorService with a fixed-size thread pool.
     */
    private void testCallableMemoizer() {
        System.out.println("Running testCallableMemoizer()");

        // This object manages a thread pool that matches the number
        // of cores.
        ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime
                                         .getRuntime()
                                         .availableProcessors());

        // Create a stream that uses the ExecutorService to
        // concurrently check the primality of "count" random numbers.
        sRandomNumbers
            // Convert the list into a stream.
            .stream()

            // Convert each random number into a Callable.
            .map(randomNumber ->
                      (Callable<SimpleImmutableEntry<Long, Long>>) () ->
                      sPrimeChecker.apply(Math.abs(randomNumber)))

            // Submit each Callable to the ExecutorService.
            .map(executorService::submit)

            // Trigger intermediate operation processing and create a
            // list of futures.
            .collect(toList())

            // Print out the result of each future.
            .forEach(future ->
                     // Synchronously get future value (may block if
                     // computation isn't complete) and print results.
                      printResult(rethrowSupplier(future::get).get()));
    }

    /**
     * Asynchronous check the primality of random numbers using
     * CompletableFuture.supplyAsync() with the common fork-join pool.
     */
    private void testSupplyAsyncMemoizer() {
        System.out.println("Running testSupplyAsyncMemoizer()");

        // Create a stream that uses CompletableFuture.supplyAsync() to
        // asynchronously check the primality of "count" random numbers.
        sRandomNumbers
            // Convert the list into a stream.
            .stream()

            // Submit each random number to CompletableFuture's
            // supplyAsync() factory method.
            .map(randomNumber ->
                      CompletableFuture.supplyAsync(() ->
                                                    sPrimeChecker.apply(Math.abs(randomNumber))))

            // Create one completable future that completes when all
            // the other completable futures in the stream complete.
            .collect(StreamOfFuturesCollector.toFuture())

            // Called when all asynchronous operations complete.
            .thenAccept(stream ->
                        // Print each result in the result stream.
                        stream.forEach(ex25::printResult))

            // Wait for all asynchronous processing to complete.
            .join();
    }

    /*
     * Asynchronous check the primality of count random numbers using
     * the AsyncMemoizer.
     */
    private void testAsyncMemoizer() {
        System.out.println("Running testAsyncMemoizer()");

        // Create a stream that uses the AsyncMemoizer to asynchronously
        // check the primality of "count" random numbers.
        sRandomNumbers
            // Convert the list into a stream.
            .stream()

            // Convert each random number into a completable future.
            .map(randomNumber ->
                      sPrimeCheckerCF.apply(Math.abs(randomNumber)))

            // Create one completable future that completes when all
            // the other completable futures in the stream complete.
            .collect(StreamOfFuturesCollector.toFuture())

            // Called when all asynchronous operations complete.
            .thenAccept(stream ->
                        // Print each result in the result stream.
                        stream.forEach(ex25::printResult))

            // Wait for all asynchronous processing to complete.
            .join();
    }

    /**
     * Print the result.
     * @param result The result to print.
     */
    private static void printResult(SimpleImmutableEntry<Long, Long> result) {
        System.out.println("Prime candidate "
                           + result.getKey()
                           + " has a smallest factor of "
                           + result.getValue());
    }
}
