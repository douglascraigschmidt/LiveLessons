package tests;

import utils.Memoizer;
import utils.Options;
import utils.PrimeCheckers;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

import static java.util.AbstractMap.SimpleImmutableEntry;
import static java.util.stream.Collectors.toList;
import static utils.ExceptionUtils.rethrowSupplier;

/**
 * Concurrently check the primality of a {@link List} of random
 * numbers using an ExecutorService with a fixed-size thread pool
 * of daemon threads.
 */
public class TestCallableMemoizer
    implements Runnable {
    /**
     * The {@link List} of randomly generated numbers.
     */
    private final List<Long> mRandomNumbers;

    /**
     * This object checks for primality using a synchronous memoizer.
     */ 
    private final Function<Long, SimpleImmutableEntry<Long, Long>>
        sPrimeChecker = new Memoizer<>(PrimeCheckers::bruteForceChecker,
                                       new ConcurrentHashMap<>());

    /**
     * Create a {@link ThreadFactory} that creates a
     * daemon thread.
     */
    private final ThreadFactory mThreadFactory =
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            };

    /**
     * Constructor initializes the field.
     */
    public TestCallableMemoizer(List<Long> randomNumbers) {
        mRandomNumbers = randomNumbers;
    }

    /**
     * Concurrently check the primality of count random numbers using
     * an ExecutorService with a fixed-size thread pool.
     */
    @Override
    public void run() {
        System.out.println("Running TestCallableMemoizer");

        // This object manages a thread pool that matches the number
        // of cores.
        ExecutorService executorService = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                                mThreadFactory);

        // Create a stream that uses the ExecutorService to
        // concurrently check the primality of "count" random numbers.
        mRandomNumbers
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
                     Options.printResult(rethrowSupplier(future::get).get()));
    }
}
