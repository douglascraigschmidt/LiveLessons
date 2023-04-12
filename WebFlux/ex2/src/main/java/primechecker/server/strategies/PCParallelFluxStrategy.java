package primechecker.server.strategies;

import primechecker.common.Options;
import primechecker.utils.PrimeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * This strategy uses the Project Reactor {@link ParallelFlux} framework to
 * check all the elements in a {@link Flux} for primality.
 */
public class PCParallelFluxStrategy
       implements PCAbstractStrategy {
    /**
     * Checks all the elements in the {@code primeCandidates} {@link
     * Flux} param for primality and return a corresponding {@link
     * Flux} whose results indicate 0 if an element is prime or the
     * smallest factor if it's not.
     *
     * @param primeCandidates The {@link List} of {@link Integer}
     *                        objects to check for primality
     * @return An {@link Flux} emitting elements that are 0 if
     *         the corresponding element in {@code primeCandidate}
     *         isprime or its smallest factor if it's not prime
     */
    @Override
    public Flux<Integer> checkIfPrimeFlux
        (Flux<Integer> primeCandidates) {
        return primeCandidates

            // Convert Flux to ParallelFlux.
            .parallel()

            // Run computations on the parallel Scheduler.
            .runOn(Schedulers.parallel())

            // Call the isPrime() method on each Integer in the
            // stream.
            .map(PrimeUtils::isPrime)

            // Convert ParallelFlux to Flux.
            .sequential();
    }
}
