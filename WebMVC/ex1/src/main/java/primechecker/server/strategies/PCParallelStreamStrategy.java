package primechecker.server.strategies;

import primechecker.common.Options;
import primechecker.utils.PrimeUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This strategy uses the Java parallel streams framework to check all
 * the elements in a {@link List} for primality.
 */
public class PCParallelStreamStrategy
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
        var stream = primeCandidates
            // Create a (sequential) stream.
            .stream();

        // Conditionally convert the sequential stream to a parallel
        // stream.
        if (parallel)
            stream.parallel();

        var results = stream
            // Call the isPrime() method on each Integer in the
            // stream.
            .map(PrimeUtils::isPrime)

            // Trigger intermediate operations and collect into a List.
            .collect(toList());

        // Conditionally display the results.
        if (Options.instance().getDebug())
            Options.displayResults(primeCandidates, results);

        // Return the results.
        return results;
    }
}
