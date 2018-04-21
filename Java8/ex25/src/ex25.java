import utils.*;

import static java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Random;
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
    private static long sDEFAULT_COUNT = 100;

    /**
     * Maximum value of  random numbers.
     */
    private static long sMAX_VALUE = 100000000000000L;

    /**
     * This object checks for primality using a synchronous memoizer.
     */ 
    private Function<Long, SimpleImmutableEntry<Long, Long>> sPrimeChecker =
        new Memoizer<>(PrimeCheckers::efficientChecker);

    /**
     * This object checks for primality using an asynchronous
     * memoizer.
     */
    private Function<Long, CompletableFuture<SimpleImmutableEntry<Long, Long>>> sPrimeCheckerCF =
        new CFMemoizer<>(PrimeCheckers::efficientChecker);

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv)
        throws ExecutionException, InterruptedException {
        System.out.println("Starting ex25 test");

        // Conditionally override the default count.
        long count = argv.length == 0 ? sDEFAULT_COUNT : Long.valueOf(argv[0]);

        ex25 t = new ex25();

        // Concurrently check the primality of count random numbers
        // using an ExecutorService with a fixed-size thread pool.
        t.testCallableMemoizer(count);

        // Asynchronous check the primality of count random numbers
        // using CompletableFuture.supplyAsync() with the common
        // fork-join pool.
        t.testSupplyAsyncMemoizer(count);

        // Asynchronous check the primality of count random numbers
        // using the CFMemoizer.
        t.testCFMemoizer(count);
        System.out.println("Finishing ex25 test");
    }

    /**
     * Concurrently check the primality of {@code count} random
     * numbers using an ExecutorService with a fixed-size thread pool.
     */
    private void testCallableMemoizer(long count)
        throws ExecutionException, InterruptedException {
        System.out.println("Running testCallableMemoizer()");

        // This object manages a thread pool that matches the number
        // of cores.
        ExecutorService executorService =
            Executors.newFixedThreadPool(Runtime.getRuntime()
                                         .availableProcessors());

        // Create a stream that uses the ExecutorService to
        // concurrently check the primality of "count" random numbers.
        new Random()
            // Generate "count" random between sMAX_VALUE - count and
            // sMAX_VALUE.
            .longs(count, sMAX_VALUE - count, sMAX_VALUE)

            // Convert each random number into a Callable.
            .mapToObj(randomNumber ->
                      (Callable<SimpleImmutableEntry<Long, Long>>) () -> 
                      sPrimeChecker.apply(Math.abs(randomNumber)))

            // Submit each Callable to the ExecutorService.
            .map(executorService::submit)

            // Print out the result of each future.
            .forEach(future -> {
                    try {
                        // Synchronously get future value (may block
                        // if the computation isn't complete).
                        SimpleImmutableEntry<Long, Long> result = future.get();

                        System.out.println("Prime candidate "
                                           + result.getKey()
                                           + " has a smallest factor of "
                                           + result.getValue());
                    } catch (Exception ex) {
                        System.out.println("Exception = " + ex.getMessage());
                    }
            });
    }

    /**
     * Asynchronous check the primality of {@code count} random
     * numbers using CompletableFuture.supplyAsync() with the common
     * fork-join pool.
     */
    private void testSupplyAsyncMemoizer(long count)
        throws ExecutionException, InterruptedException {
        System.out.println("Running testSupplyAsyncMemoizer()");

        // Create a stream that uses CompletableFuture.supplyAsync() to
        // asynchronously check the primality of "count" random numbers.
        new Random()
            // Generate "count" random between sMAX_VALUE - count and
            // sMAX_VALUE.
            .longs(count, sMAX_VALUE - count, sMAX_VALUE)

            // Submit each random number to CompletableFuture's
            // supplyAsync() factory method.
            .mapToObj(randomNumber ->
                      CompletableFuture.supplyAsync(() ->
                                                    sPrimeChecker.apply(Math.abs(randomNumber))))

            // Create one completable future that completes when all
            // the other completable futures in the stream complete.
            .collect(StreamOfFuturesCollector.toFuture())

            // Called when all asynchronous operations complete.
            .thenAccept(stream ->
                        // Print each result in the result stream.
                        stream.forEach(result ->
                                       System.out.println("Prime candidate "
                                                          + result.getKey()
                                                          + " has a smallest factor of "
                                                          + result.getValue())))

            // Wait for all asynchronous processing to complete.
            .join();
    }

    /*
     * Asynchronous check the primality of {@code count} random
     * numbers using the CFMemoizer.
     */
    private void testCFMemoizer(long count)
        throws ExecutionException, InterruptedException {
        System.out.println("Running testCFMemoizer()");

        // Create a stream that uses the CFMemoizer to asynchronously
        // check the primality of "count" random numbers.
        new Random()
            // Generate "count" random between sMAX_VALUE - count and
            // sMAX_VALUE.
            .longs(count, sMAX_VALUE - count, sMAX_VALUE)

            // Convert each random number into a completable future.
            .mapToObj(randomNumber ->
                      sPrimeCheckerCF.apply(Math.abs(randomNumber)))

            // Create one completable future that completes when all
            // the other completable futures in the stream complete.
            .collect(StreamOfFuturesCollector.toFuture())

            // Called when all asynchronous operations complete.
            .thenAccept(stream ->
                        // Print each result in the result stream.
                        stream.forEach(result ->
                                       System.out.println("Prime candidate "
                                                          + result.getKey()
                                                          + " has a smallest factor of "
                                                          + result.getValue())))

            // Wait for all asynchronous processing to complete.
            .join();
    }
}
