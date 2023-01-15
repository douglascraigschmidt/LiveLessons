package primechecker.client;

import jdk.incubator.concurrent.StructuredTaskScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import primechecker.server.PCServerController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import static primechecker.common.Constants.Strategies.STRUCTURED_CONCURRENCY;
import static primechecker.utils.FutureUtils.futures2Objects;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@link PCServerController} web
 * service to determine the primality of large integers.  These
 * invocations can be made individually or in bulk, as well as be make
 * sequentially or in parallel using Java structured concurrency.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Component
public class PCClientStructuredConcurrency {
    /**
     * This auto-wired field connects the {@link PCClientStructuredConcurrency} to
     * the {@link PCProxy} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private PCProxy mPCProxy;

    /**
     * Send individual HTTP GET requests to the server to check if a
     * the {@code primeCandidates} {@link List} of {@link Integer}
     * objects are prime or not.
     *
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallelism, else false
     * @return A {@link List} of {@link Integer} objects indicating
     *         the primality of the corresponding {@code primeCandidates}
     *         elements
     */
    public List<Integer> testIndividualCalls(List<Integer> primeCandidates,
                                             boolean parallel) {
        if (parallel)
            return testIndividualCallsParallel(primeCandidates);
        else 
            return testIndividualCallsSequential(primeCandidates);
    }

    /**
     * Send individual HTTP GET requests to the server sequentially to
     * check if a the {@code primeCandidates} {@link List} of {@link
     * Integer} objects are prime or not.
     *
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @return A {@link List} of {@link Integer} objects indicating
     *         the primality of the corresponding {@code primeCandidates}
     *         elements
     */
    private List<Integer> testIndividualCallsSequential
        (List<Integer> primeCandidates) {
        try {
            // Create a List of Integer to hold the results.
            var results = new ArrayList<Integer>();

            // Iterate through all the random BigFraction objects.
            for (var primeCandidate : primeCandidates)
                results
                    // Add the Integer to the List.
                    .add(mPCProxy
                         .checkIfPrime(STRUCTURED_CONCURRENCY,
                                       primeCandidate));

            // Return the results.
            return results;
        } catch (Exception exception) {
            // Return an empty List if an exception occurs.
            return Collections.emptyList();
        }
    }

    /**
     * Send individual HTTP GET requests to the server in parallel to
     * check if a the {@code primeCandidates} {@link List} of {@link
     * Integer} objects are prime or not.
     *
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @return A {@link List} of {@link Integer} objects indicating
     *         the primality of the corresponding {@code primeCandidates}
     *         elements
     */
    private List<Integer> testIndividualCallsParallel
        (List<Integer> primeCandidates) {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Get a List that contains the results of checking all
            // the primeCandidates for primality.
            var results = primeCandidates
                // Convert the List to a Stream.
                .stream()

                // Check each primeCandidates for primality.
                .map(primeCandidate -> scope
                     // Fork a new virtual thread to check the
                     // primeCandidate for primality.
                     .fork(() -> mPCProxy
                           .checkIfPrime(STRUCTURED_CONCURRENCY,
                                         primeCandidate)))
                                             
                // Convert the result into a List.
                .toList();

            // This barrier synchronizer waits for all threads to
            // finish or the task scope to shut down.
            scope.join();

            // Throw an exception if a remote call fails.
            scope.throwIfFailed();

            // Convert the List<Future<Integer>> to a List<Integer>
            // and return it.
            return futures2Objects(results);
        } catch (Exception exception) {
            System.out.println("Exception: "
                               + exception.getMessage());

            // Return an empty List if an exception occurs.
            return Collections.emptyList();
        }        
    }

    /**
     * Sends a {@link List} of {@code primeCandidate} {@link Integer}
     * objects in one HTTP GET request to the server to determine
     * which {@link List} elements are prime or not.
     *
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallelism, else false
     * @return A {@link List} of {@link Integer} objects indicating
     *         the primality of the corresponding {@code primeCandidates}
     *         elements
     */
    public List<Integer> testListCall(List<Integer> primeCandidates,
                                      boolean parallel) {
        return mPCProxy
            // Forward to the proxy.
            .checkIfPrimeList(STRUCTURED_CONCURRENCY,
                              primeCandidates,
                              parallel);
    }
}
