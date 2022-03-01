package tests;

import utils.*;

import static java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Asynchronous check the primality of random numbers using the {@code
 * supplyAsync()} method in {@link CompletableFuture} with the common
 * fork-join pool.
 */
public class TestSupplyAsyncMemoizer
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
     * Constructor sets the field.
     */
    public TestSupplyAsyncMemoizer(List<Long> randomNumbers) {
        mRandomNumbers = randomNumbers;
    }

    /**
     * Asynchronous check the primality of random numbers using {@code
     * supplyAsync()} method in {@link CompletableFuture} with the
     * common fork-join pool.
     */
    @Override
    public void run() {
        System.out.println("Running TestSupplyAsyncMemoizer");

        // Create a stream that uses CompletableFuture.supplyAsync()
        // to asynchronously check the primality of random numbers.
        mRandomNumbers
            // Convert the list into a stream.
            .stream()

            // Submit each random number to CompletableFuture's
            // supplyAsync() factory method.
            .map(randomNumber -> CompletableFuture
                 .supplyAsync(() ->
                              sPrimeChecker.apply(Math.abs(randomNumber))))

            // Create one completable future that completes when all
            // the other completable futures in the stream complete.
            .collect(StreamOfFuturesCollector.toFuture())

            // Called when all asynchronous operations complete.
            .thenAccept(stream ->
                        // Print each result in the result stream.
                        stream.forEach(Options::printResult))

            // Wait for all asynchronous processing to complete.
            .join();
    }
}
