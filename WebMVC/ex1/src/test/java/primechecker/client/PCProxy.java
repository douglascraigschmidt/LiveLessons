package primechecker.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import primechecker.server.PCServerApplication;
import primechecker.server.PCServerController;
import primechecker.utils.WebUtils;

import java.util.List;

import static primechecker.common.Constants.EndPoint.CHECK_IF_PRIME;
import static primechecker.common.Constants.EndPoint.CHECK_IF_PRIME_LIST;

/**
 * This class is a proxy to the {@link PCServerController}.
 */
@Component
public class PCProxy {
    /**
     * This auto-wired field connects the {@link PCClientParallelStream} to
     * the {@link RestTemplate} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private RestTemplate mRestTemplate;

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
    public Integer checkIfPrime(int strategy,
                                int primeCandidate) {
        // Create the encoded URL.
        var url = UriComponentsBuilder
                .fromPath(CHECK_IF_PRIME)
                .queryParam("strategy", strategy)
                .queryParam("primeCandidate", primeCandidate)
                .build()
                .toUriString();

        return WebUtils
            // Create and send a GET request to the server to check if
            // the primeCandidate is prime or not.
            .makeGetRequest(mRestTemplate,
                            // Pass the encoded URL.
                            url,
                            // The return type is an Integer.
                            Integer.class);
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
    public List<Integer> checkIfPrimeList
        (int strategy,
         List<Integer> primeCandidates,
         Boolean parallel) {
        // Create the encoded URL.
        var url = UriComponentsBuilder
                .fromPath(CHECK_IF_PRIME_LIST)
                .queryParam("strategy", strategy)
                .queryParam("primeCandidates",
                        WebUtils
                                // Convert the List to a String.
                                .list2String(primeCandidates))
                .queryParam("parallel", parallel)
                .build()
                .toUriString();

        return WebUtils
            // Create and send a GET request to the server to
            // check if the Integer objects in primeCandidates
            // are prime or not.
            .makeGetRequestList(mRestTemplate,
                                url,
                                // The return type is an array of Integer objects.
                                Integer[].class);
    }
}

