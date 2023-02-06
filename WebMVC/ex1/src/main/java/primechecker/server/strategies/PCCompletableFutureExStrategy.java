package primechecker.server.strategies;

import primechecker.common.Options;
import primechecker.utils.FuturesCollector;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import primechecker.utils.CompletableFutureEx;
import static primechecker.utils.PrimeUtils.isPrime;

/*
 * This strategy uses a customization of the Java completable futures
 * framework that uses virtual threads to check all the elements in a
 * {@link List} for primality.
 */
public class PCCompletableFutureExStrategy
       implements PCAbstractStrategy {
    /**
     * Checks all the elements in the {@code primeCandidates} {@link
     * List} param for primality and return a corresponding {@link
     * List} whose results indicate 0 if an element is prime or the
     * smallest factor if it's not.
     *
     * @param primeCandidates The {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if primality checking should run in
     *                 parallel, else false if it should run
     *                 sequentially
     * @return An {@link List} whose elements are 0 if the
     *         corresponding element in {@code primeCandidate} is
     *         prime or its smallest factor if it's not prime
     */
    @Override
    public List<Integer> checkIfPrimeList(List<Integer> primeCandidates,
                                          Boolean parallel) {
        try (var executor = parallel
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newSingleThreadExecutor()) {
            var results = primeCandidates
                    // Convert the List into a Stream.
                    .stream()

                    // Asynchronously check each primeCandidate for
                    // primality.
                    .map(primeCandidate -> CompletableFutureEx
                            .supplyAsync(() -> isPrime(primeCandidate),
                                         executor))

                    // Trigger intermediate operations and create a
                    // CompletableFuture that triggers when all the
                    // asynchronous calls complete.
                    .collect(FuturesCollector.toFuture())

                    // Block until all the asynchronous processing
                    // completes.
                    .join();

            // Conditionally display the results.
            if (Options.instance().getDebug())
                Options.displayResults(primeCandidates, results);

            // Return the results.
            return results;
        }
    }
}
