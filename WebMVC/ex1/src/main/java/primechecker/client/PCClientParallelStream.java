package primechecker.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import primechecker.server.PCServerController;
import static primechecker.common.Constants.Strategies.PARALLEL_STREAM;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@link PCServerController} web
 * service to determine the primality of large integers.  These
 * invocations can be made individually or in bulk, as well as be make
 * sequentially or in parallel using Java Streams.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class PCClientParallelStream {
    /**
     * This auto-wired field connects the {@link PCClientParallelStream} to
     * the {@link PCProxy} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private PCProxy mPrimeCheckProxy;

    /**
     * Send individual HTTP GET requests to the server to check if a
     * the {@code primeCandidates} {@link List} of {@link Integer}
     * objects are prime or not.
     *
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallel streams, else false
     * @return A {@link List} of {@link Integer} objects indicating
     *         the primality of the corresponding {@code primeCandidates}
     *         elements
     */
    public List<Integer> testIndividualCalls(List<Integer> primeCandidates,
                                             boolean parallel) {
        return StreamSupport
            // Convert the List to a sequential or parallel stream.
            .stream(primeCandidates.spliterator(), parallel)

            // Forward each prime candidate to the proxy.
            .map(primeCandidate -> mPrimeCheckProxy
                 .checkIfPrime(PARALLEL_STREAM,
                               primeCandidate))

            // Trigger the intermediate operations and collect the
            // results into a List.
            .toList();
    }

    /**
     * Sends a {@link List} of {@code primeCandidate} {@link Integer}
     * objects in one HTTP GET request to the server to determine
     * which {@link List} elements are prime or not.
     *
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallel streams, else false
     * @return A {@link List} of {@link Integer} objects indicating
     *         the primality of the corresponding {@code primeCandidates}
     *         elements
     */
    public List<Integer> testListCall(List<Integer> primeCandidates,
                                      boolean parallel) {
        return mPrimeCheckProxy
            // Forward to the proxy.
            .checkIfPrimeList(PARALLEL_STREAM,
                              primeCandidates,
                              parallel);
    }
}
