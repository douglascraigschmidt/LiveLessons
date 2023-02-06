package primechecker.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import primechecker.server.PCServerController;
import primechecker.utils.CompletableFutureEx;
import primechecker.utils.FuturesCollector;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import static primechecker.common.Constants.Strategies.COMPLETABLE_FUTURE;
import static primechecker.common.Constants.Strategies.COMPLETABLE_FUTURE_EX;
import static primechecker.utils.FuturesCollector.toFuture;

/**
 * This client uses Spring WebMVC features to perform asynchronous
 * remote method invocations on the {@link PCServerController} web
 * service to determine the primality of large integers.  These
 * invocations can be made individually or in bulk, as well as be make
 * sequentially or in parallel using a customized implementation of
 * the Java completable future framework that uses virtual threads to
 * execute tasks.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class PCClientCompletableFutureEx {
    /**
     * This auto-wired field connects the {@link
     * PCClientCompletableFuture} to the {@link PCProxy} that performs
     * HTTP requests synchronously.
     */
    @Autowired
    private PCProxy mPCProxy;

    /**
     * Send individual HTTP GET requests to the server to check if a
     * the {@code primeCandidates} {@link List} of {@link Integer}
     * objects are prime or not.
     *
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallelism, else false
     * @return A {@link List} of {@link Integer} objects indicating
     *         the primality of the corresponding {@code primeCandidates}
     *         elements
     */
    public List<Integer> testIndividualCalls(List<Integer> primeCandidates,
                                             boolean parallel)  {
        if (!parallel)
            CompletableFutureEx
                // Switch to a single-threaded executor.
                .setExecutor(Executors.newSingleThreadExecutor());

        return primeCandidates
            // Convert the List into a Stream.
            .stream()

            // Asynchronously perform a remote method invocation
            // to check each primeCandidate for primality.
            .map(primeCandidate -> CompletableFutureEx
                 .supplyAsync(() -> mPCProxy
                              // Forward to the proxy.
                              .checkIfPrime(COMPLETABLE_FUTURE_EX,
                                            primeCandidate)))

            // Trigger intermediate operations and create a
            // CompletableFuture that triggers when all the
            // asynchronous calls complete.
            .collect(FuturesCollector.toFuture())

            // Block until all the asynchronous processing
            // completes.
            .join();
    }

    /**
     * Sends a {@link List} of {@code primeCandidate} {@link Integer}
     * objects in one HTTP GET request to the server to determine
     * which {@link List} elements are prime or not.
     *
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallelism, else false
     * @return A {@link List} of {@link Integer} objects indicating
     *         the primality of the corresponding {@code primeCandidates}
     *         elements
     */
    public List<Integer> testListCall(List<Integer> primeCandidates,
                                      boolean parallel) {
        return mPCProxy
            // Forward to the proxy.
            .checkIfPrimeList(COMPLETABLE_FUTURE_EX,
                              primeCandidates,
                              parallel);
    }
}
