package server;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static common.Constants.EndPoint.CHECK_IF_PRIME;
import static common.Constants.EndPoint.CHECK_IF_PRIME_LIST;
import static java.util.stream.Collectors.toList;

/**
 * This class defines implementation methods that are called by the
 * {@link PrimeCheckController}, which serves as an entry point for
 * remote clients that to check the primality of one or more {@link
 * Integer} objects.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the auto-detection and wiring of dependent implementation classes
 * via classpath scanning.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Service
public class PrimeCheckService {
    /**
     * Checks the {@code primeCandidate} param for primality,
     * returning 0 if it's prime or the smallest factor if it's not.
     *
     * WebFlux maps HTTP GET requests sent to the {@code
     * CHECK_IF_PRIME} endpoint to this method.
     *
     * @param primeCandidate The {@link Integer} to check for
     *                       primality
     * @return An {@link Integer} that is 0 if the {@code
     *         primeCandidate} is prime and its smallest factor if
     *         it's not prime
     */
    public Integer checkIfPrime(Integer primeCandidate) {
        return isPrime(primeCandidate);
    }

    /**
     * Checks all the elements in the {@code primeCandidates} {@link
     * List} param for primality and return a corresponding {@link
     * List} whose results indicate 0 if an element is prime or the
     * smallest factor if it's not.
     *
     * WebFlux maps HTTP GET requests sent to the {@code
     * CHECK_IF_PRIME_LIST} endpoint to this method.
     *
     * @param primeCandidates The {@link Integer} to check for
     *                       primality
     * @param parallel True if primality checking should run in
     *                 parallel, else false if it should run sequentially
     * @return An {@link List} whose elements are 0 if the
     *         corresponding element in {@code primeCandidate} is
     *         prime or its smallest factor if it's not prime
     */
    public List<Integer> checkIfPrimeList(List<Integer> primeCandidates,
                                          Boolean parallel) {
        System.out.println("checkIfPrimeList()");

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
            .map(this::isPrime)

            // Trigger the intermediate operations and collect into a
            // List.
            .collect(toList());

        /*
        results
            .forEach(System.out::println);
         */

        // Return the results.
        return results;
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns 0 if it is
     * prime, or the smallest factor if it is not prime.
     */
    private Integer isPrime(Integer primeCandidate) {
        int n = primeCandidate;

        if (n > 3)
            // This algorithm is intentionally inefficient to burn
            // lots of CPU time!
            for (int factor = 2;
                 factor <= n / 2;
                 ++factor)
                if (Thread.interrupted()) {
                    // Options.debug(" Prime checker thread interrupted");
                    break;
                } else if (n / factor * factor == n)
                    return factor;

        return 0;
    }
}
