package primechecker.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import primechecker.common.Constants;
import primechecker.server.PrimeCheckController;
import primechecker.utils.Options;
import primechecker.utils.WebUtils;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static primechecker.common.Constants.EndPoint.CHECK_IF_PRIME;
import static primechecker.common.Constants.EndPoint.CHECK_IF_PRIME_LIST;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@link PrimeCheckController} web
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
@SuppressWarnings("ResultOfMethodCallIgnored")
@Component
public class PrimeCheckClient {
    /**
     * This auto-wired field connects the {@link PrimeCheckClient} to
     * the {@link PrimeCheckProxy} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private PrimeCheckProxy mPrimeCheckProxy;

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
            .map(mPrimeCheckProxy::checkIfPrime)

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
            .checkIfPrimeList(primeCandidates, parallel);
    }
}
