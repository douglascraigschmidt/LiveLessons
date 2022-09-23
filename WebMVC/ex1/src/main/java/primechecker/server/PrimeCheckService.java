package primechecker.server;

import org.springframework.stereotype.Service;
import primechecker.utils.Options;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This class defines implementation methods that are called by the
 * {@link PrimeCheckController}. These implementation methods check
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
public class PrimeCheckService {
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
    public Integer checkIfPrime(Integer primeCandidate) {
        System.out.println("checkIfPrime()");

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

            // Trigger intermediate operations and collect into a List.
            .collect(toList());

        // Conditionally display the results.
        if (Options.instance().diagnosticsEnabled())
            Options.displayResults(primeCandidates, results);

        // Return the results.
        return results;
    }

    /**
     * This method determines whether the {@code primeCandidate} param
     * is prime.
     *
     * @param primeCandidate The {@link Integer} to check for primality
     * @return Returns 0 if {@code primeCandidate} is prime, or the
     *         smallest factor if it is not prime
     */
    private Integer isPrime(Integer primeCandidate) {
        int n = primeCandidate;

        // Check if n is a multiple of 2 and return
        // immediately if it is.
        if (n % 2 == 0) 
            return 2;

        // If not, then just check the odds.
        for (int factor = 3;
             factor * factor <= n;
             // Skip over even numbers.
             factor += 2)
            // Check for interrupts every 1,000 iterations.
            if ((factor % (n / 1_000)) == 0
                && Thread.interrupted()) {
                    System.out.println("Prime checker thread interrupted "
                                       + Thread.currentThread());
                    break;
            } else if (n % factor == 0)
                // The primeCandidate number is not a prime.
                return factor;

        // The primeCandidate number is a prime.
        return 0;
    }
}
