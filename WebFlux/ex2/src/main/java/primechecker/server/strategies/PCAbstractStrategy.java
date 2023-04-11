package primechecker.server.strategies;

import primechecker.common.Options;
import reactor.core.publisher.Flux;

import java.util.List;

import static primechecker.utils.PrimeUtils.isPrime;

/**
 * This interface defines methods that are implemented to check for
 * primality.
 */
public interface PCAbstractStrategy {
    /**
     * Checks the {@code primeCandidate} param for primality,
     * returning 0 if it's prime or the smallest factor if it's not.
     *
     * @param primeCandidate The {@link Integer} to check for
     *                       primality
     * @return An {@link Integer} that is 0 if the {@code
     *         primeCandidate} is prime and its smallest factor if
     *         it's not prime
     */
    default Integer checkIfPrime(Integer primeCandidate) {
        // Determine primality.
        var result = isPrime(primeCandidate);

        // Conditional print the result.
        Options.debug("Result for "
                      + primeCandidate
                      + " = "
                      + result);

        // Return 0 if primeCandidate was prime and its smallest
        // factor if it's not prime.
        return result;
    }

    /**
     * Checks all the elements in the {@code primeCandidates} {@link
     * Flux} param for primality and return a corresponding {@link
     * Flux} that emits results indicating 0 if an element is prime
     * or the smallest factor if it's not.
     *
     * @param primeCandidates The {@link Flux} of {@link Integer}
     *                        objects to check for primality
     * @return An {@link Flux} emitting elements that are 0 if the
     *         corresponding element in {@code primeCandidate} is
     *         prime or its smallest factor if it's not prime
     */
    Flux<Integer> checkIfPrimeFlux(Flux<Integer> primeCandidates);
}
