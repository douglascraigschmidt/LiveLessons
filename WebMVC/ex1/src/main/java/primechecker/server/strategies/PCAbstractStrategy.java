package primechecker.server.strategies;

import primechecker.common.Options;

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
    List<Integer> checkIfPrimeList(List<Integer> primeCandidates,
                                   Boolean parallel);
}
