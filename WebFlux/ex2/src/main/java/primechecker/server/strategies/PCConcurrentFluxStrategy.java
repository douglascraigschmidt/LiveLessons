package primechecker.server.strategies;

import primechecker.utils.PrimeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/*
 * This strategy uses the Project Reactor flatMap() concurrency idiom to
 * check all the elements in a {@link Flux} for primality.
 */
public class PCConcurrentFluxStrategy
       implements PCAbstractStrategy {
    /**
     * Checks all the elements in the {@code primeCandidates} {@link
     * Flux} param for primality and return a corresponding {@link
     * Flux} whose results indicate 0 if an element is prime or the
     * smallest factor if it's not.
     *
     * @param primeCandidates The {@link Flux} of {@link Integer}
     *                        objects to check for primality
     * @return An {@link Flux} emitting elements that are 0 if
     * the corresponding element in {@code primeCandidate} is
     * prime or its smallest factor if it's not prime
     */
    @Override
    public Flux<Integer> checkIfPrimeFlux
    (Flux<Integer> primeCandidates) {
        return primeCandidates
            // Asynchronously check each primeCandidate for
            // primality using the flatMap() concurrency idiom.
            .flatMap(primeCandidate -> Mono
                .fromCallable(() -> PrimeUtils
                    .isPrime(primeCandidate))

                // Run on the parallel thread pool.
                .subscribeOn(Schedulers.parallel()));
    }
}
