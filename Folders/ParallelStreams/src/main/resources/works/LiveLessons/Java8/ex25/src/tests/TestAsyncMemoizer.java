package tests;

import utils.AsyncMemoizer;
import utils.Options;
import utils.PrimeCheckers;
import utils.StreamOfFuturesCollector;

import static java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Asynchronous check the primality of a {@link List} of random
 * numbers using the {@link AsyncMemoizer}.
 */
public class TestAsyncMemoizer
    implements Runnable {
    /**
     * The {@link List} of randomly generated numbers.
     */
    private final List<Long> mRandomNumbers;

    /**
     * Checks for primality using an asynchronous memoizer.
     */
    private final Function<Long,
                            CompletableFuture<SimpleImmutableEntry<Long, Long>>>
        mPrimeCheckerCF = new AsyncMemoizer<>(PrimeCheckers::bruteForceChecker);

    /**
     * Constructor initializes the field.
     */
    public TestAsyncMemoizer(List<Long> randomNumbers) {
        mRandomNumbers = randomNumbers;
    }

    /*
     * Asynchronous check the primality of count random numbers using
     * the AsyncMemoizer.
     */
    @Override
    public void run() {
        System.out.println("Running TestAsyncMemoizer");

        // Create a stream that uses the AsyncMemoizer to
        // asynchronously check the primality of "count" random
        // numbers.
        mRandomNumbers
            // Convert the list into a stream.
            .stream()

            // Convert each random number into a completable future.
            .map(randomNumber ->
                 mPrimeCheckerCF.apply(Math.abs(randomNumber)))

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
