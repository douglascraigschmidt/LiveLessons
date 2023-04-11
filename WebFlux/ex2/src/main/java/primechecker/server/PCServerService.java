package primechecker.server;

import org.springframework.stereotype.Service;
import primechecker.server.strategies.*;
import reactor.core.publisher.Flux;

/**
 * This class defines implementation methods that are called by the
 * {@link PCServerController}. These implementation methods check the
 * primality of one or more {@link Integer} objects using the Java
 * parallel streams framework, Java completable futures framework, and
 * the Java structured concurrency framework.  Sequential or parallel
 * programming is used based on parameters passed by clients.
 *
 * This class is annotated as a Spring {@code @Service}, which
 * indicates this class implements "business logic" and enables the
 * auto-detection and wiring of dependent implementation classes via
 * classpath scanning.
 */
@Service
public class PCServerService {
    /**
     * This array contains concrete strategies whose methods are
     * implemented to check for primality.  The order in which these
     * objects are assigned to the array slots matters.
     */
    PCAbstractStrategy[] mStrategy = {
        new PCConcurrentFluxStrategy(),
        new PCParallelFluxStrategy()
    };

    /**
     * Checks the {@code primeCandidate} param for primality,
     * returning 0 if it's prime or the smallest factor if it's not.
     *
     * @param strategy Which implementation strategy to forward the
     *                 request to
     * @param primeCandidate The {@link Integer} to check for
     *                       primality
     * @return An {@link Integer} that is 0 if the {@code
     *         primeCandidate} is prime and its smallest factor if
     *         it's not prime
     */
    public Integer checkIfPrime(Integer strategy,
                                Integer primeCandidate) {
        return mStrategy[strategy]
            // Index into the appropriate strategy and check the
            // primality of the primeCandidate.
            .checkIfPrime(primeCandidate);
    }

    /**
     * Checks all the elements in the {@code primeCandidates} {@link
     * Flux} param for primality and return a corresponding {@link
     * Flux} whose results indicate 0 if an element is prime or the
     * smallest factor if it's not.
     *
     * @param strategy Which implementation strategy to forward the
     *                 request to
     * @param primeCandidates The {@link Flux} of {@link Integer}
     *                        objects to check for primality
     * @return An {@link Flux} emitting elements that are 0 if
     *         the corresponding element in {@code primeCandidate}
     *         is prime or its smallest factor if it's not prime
     */
    public Flux<Integer> checkIfPrimeFlux(Integer strategy,
                                          Flux<Integer> primeCandidates) {
        return mStrategy[strategy]
            // Index into the appropriate strategy and check the
            // primality of the primeCandidates.
            .checkIfPrimeFlux(primeCandidates);
    }
}
