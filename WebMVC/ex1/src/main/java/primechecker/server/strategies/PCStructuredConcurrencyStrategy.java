package primechecker.server.strategies;

import primechecker.common.Options;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static primechecker.utils.PrimeUtils.isPrime;
import static primechecker.utils.FutureUtils.futures2Objects;

/**
 * This strategy uses the Java structured concurrency framework to
 * check all the elements in a {@link List} for primality.
 */
public class PCStructuredConcurrencyStrategy
       implements PCAbstractStrategy {
    /**
     * Use Java structured concurrency to check all the elements in
     * the {@code primeCandidates} {@link List} param for primality
     * and return a corresponding {@link List} whose results indicate
     * 0 if an element is prime or the smallest factor if it's not.
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
        // Create a List of Future<Integer> to hold the results.
        var results = new ArrayList<Future<Integer>>();

        // Use the try-with-resources block to create an Executor
        // that's either the virtual thread-per-task Executor or a
        // single-threaded Executor.
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

            // This scope will not be exited until all the tasks complete.
        } catch (Exception exception) {
            System.out.println("Exception: "
                               + exception.getMessage());
        }

        // Convert the List<Future<Integer>> to a List<Integer>.
        var response = futures2Objects(results);

        // Conditionally display the results.
        if (Options.instance().getDebug())
            Options.displayResults(primeCandidates, response);
            
        // Return the response.
        return response;
    }
}
