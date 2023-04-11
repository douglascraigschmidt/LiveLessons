package primechecker.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import primechecker.server.PCServerController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static primechecker.common.Constants.Strategies.PARALLEL_FLUX;

/**
 * This client uses Spring WebFlux features to perform asynchronous
 * remote method invocations on the {@link PCServerController} web
 * service to determine the primality of large integers.  These
 * invocations can be made individually or in bulk, as well as be made
 * in parallel using Project Reactor {@link ParallelFlux}.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class PCClientParallelFlux {
    /**
     * This auto-wired field connects the {@link PCClientParallelFlux}
     * to the {@link PCProxyAPI} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private PCProxyAPI mPCProxy;

    /**
     * Send individual HTTP GET requests to the server to check if a
     * the {@code primeCandidates} {@link Flux} of {@link Integer}
     * objects are prime or not.
     *
     * @param primeCandidates A {@link Flux} that emits {@link
     *                        Integer} objects to check for primality
     * @return A {@link Flux} that emits {@link Integer} objects
     *         indicating the primality of the corresponding {@code
     *         primeCandidates} elements
     */
    public Flux<Integer> testIndividualCalls
        (Flux<Integer> primeCandidates) {
            return primeCandidates
                // Convert Flux to ParallelFlux.
                .parallel()

                // Run on the BoundedElastic Scheduler.
                .runOn(Schedulers.boundedElastic())

                // Forward each prime candidate to the proxy.
                .map(primeCandidate -> mPCProxy
                     .checkIfPrime(PARALLEL_FLUX,
                                   primeCandidate))

                // Convert ParallelFlux to Flux.
                .sequential();
    }

    /**
     * Sends a {@link Flux} of {@code primeCandidate} {@link Integer}
     * objects in one HTTP GET request to the server to determine
     * which {@link Flux} elements are prime or not.
     *
     * @param primeCandidates A {@link Flux} of {@link Integer}
     *                        objects to check for primality
     * @return A {@link Flux} that emits {@link Integer} objects
     *         indicating the primality of the corresponding {@code
     *         primeCandidates} elements
     */
    public Flux<Integer> testFluxCall
        (Flux<Integer> primeCandidates) {
        return mPCProxy
            // Forward to the proxy.
            .checkIfPrimeFlux(PARALLEL_FLUX,
                              primeCandidates);
    }
}
