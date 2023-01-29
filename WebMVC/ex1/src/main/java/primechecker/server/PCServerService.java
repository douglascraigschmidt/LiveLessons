package primechecker.server;

import org.springframework.stereotype.Service;
import primechecker.server.strategies.PCAbstractStrategy;
import primechecker.server.strategies.PCCompletableFutureStrategy;
import primechecker.server.strategies.PCParallelStreamStrategy;
import primechecker.server.strategies.PCStructuredConcurrencyStrategy;

import java.util.List;

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
     * This array contains concrete strategies whose methods
     * are implemented to check for primality.
     */
    PCAbstractStrategy[] mStrategy = {
        new PCStructuredConcurrencyStrategy(),
        new PCParallelStreamStrategy(),
        new PCCompletableFutureStrategy()
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
     * List} param for primality and return a corresponding {@link
     * List} whose results indicate 0 if an element is prime or the
     * smallest factor if it's not.
     *
     * @param strategy Which implementation strategy to forward the
     *                 request to
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
            // Index into the appropriate strategy and check the
            // primality of the primeCandidates.
            .checkIfPrimeList(primeCandidates,
                              parallel);
    }
}
