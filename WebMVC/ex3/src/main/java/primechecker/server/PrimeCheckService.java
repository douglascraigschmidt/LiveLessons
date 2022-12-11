package primechecker.server;

import org.springframework.stereotype.Service;
import primechecker.common.Options;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static primechecker.utils.WebUtils.futuresToIntegers;

/**
 * This class defines implementation methods that are called by the
 * {@link PrimeCheckController}. These implementation methods check
 * the primality of one or more {@link Integer} objects using the Java
 * Streams framework.  A {@code newSingleThreadedExecutor} or a {@code
 * newVirtualThreadPerTaskExecutor} is used based on parameters passed
 * by clients.
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
    public List<Integer> checkIfPrimeList(List<Integer> primeCandidates,
                                          Boolean parallel) {
        // Create a List of Future<Integer> to hold the results.
        var results = new ArrayList<Future<Integer>>();

        try (var scope = parallel
             ? Executors.newVirtualThreadPerTaskExecutor()
             : Executors.newSingleThreadExecutor()) {
            // Iterate through all the random BigFraction objects.
            for (var primeCandidate : primeCandidates)
                results
                    // Add the Future<Integer> to the ist.
                    .add(scope
                         // Fork a new virtual thread to check the
                         // primeCandidate for primality.
                         .submit(() -> isPrime(primeCandidate)));

        } catch (Exception ignored) {}

        // Convert the List<Future<Integer>> to a List<Integer>.
        var response = futuresToIntegers(results);

        // Conditionally display the results.
        if (Options.instance().getDebug())
            Options.displayResults(primeCandidates, response);
            
        return response;
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
