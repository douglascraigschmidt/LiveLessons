package primechecker.server;

import org.springframework.stereotype.Service;
import primechecker.common.Options;
import primechecker.server.strategies.PCAbstractStrategy;
import primechecker.server.strategies.PCCompletableFutureStrategy;
import primechecker.server.strategies.PCParallelStreamStrategy;
import primechecker.server.strategies.PCStructuredConcurrencyStrategy;

import java.util.List;

import static primechecker.utils.PrimeUtils.isPrime;

/**
 * This class defines implementation methods that are called by the
 * {@link PCServerController}. These implementation methods check
 * the primality of one or more {@link Integer} objects using the Java
 * Streams framework.  A sequential or parallel stream is used based
 * on parameters passed by clients.
 *
 * This class is annotated as a Spring {@code @Service}, which
 * indicates this class implements "business logic" and enables the
 * auto-detection and wiring of dependent implementation classes via
 * classpath scanning.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Service
public class PCServerService {
    PCAbstractStrategy[] mStrategy = {
        new PCStructuredConcurrencyStrategy(),
        new PCParallelStreamStrategy(),
        new PCCompletableFutureStrategy()
    };

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
    public Integer checkIfPrime(Integer strategy,
                                Integer primeCandidate) {
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
    public List<Integer> checkIfPrimeList(Integer strategy,
                                          List<Integer> primeCandidates,
                                          Boolean parallel) {
        return mStrategy[strategy]
            .checkIfPrimeList(primeCandidates,
                              parallel);
    }
}
