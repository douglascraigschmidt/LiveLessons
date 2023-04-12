package primechecker.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import primechecker.server.PCServerController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static primechecker.common.Constants.Strategies.CONCURRENT_FLUX;

/**
 * This client uses Spring WebFlux features to perform asynchronous
 * remote method invocations on the {@link PCServerController} web
 * service to determine the primality of large integers.  These
 * invocations can be made individually or in bulk, as well as be run
 * in parallel using Project Reactor {@code flatMap()} concurrency
 * idiom.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class PCClientConcurrentFlux {
    /**
     * This auto-wired field connects the {@link
     * PCClientConcurrentFlux} to the {@link PCProxyAPI} that performs
     * HTTP requests synchronously.
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
            // Apply the flatMap() concurrency idiom.
            .flatMap(primeCandidate -> Mono
                     // Forward each prime candidate to the proxy.
                     .fromCallable(() -> mPCProxy
                                   .checkIfPrime(CONCURRENT_FLUX,
                                                 primeCandidate))

                     // Run on the BoundedElastic Scheduler.
                     .subscribeOn(Schedulers.boundedElastic()));
    }

    /**
     * Sends a {@link Flux} of {@code primeCandidate} {@link Integer}
     * objects in one HTTP POST request to the server to determine
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
            .checkIfPrimeFlux(CONCURRENT_FLUX,
                              primeCandidates);
    }
}
