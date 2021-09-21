package client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import utils.Options;
import utils.WebUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static common.Constants.EndPoint.CHECK_IF_PRIME;
import static common.Constants.EndPoint.CHECK_IF_PRIME_LIST;
import static java.util.stream.Collectors.toList;

/**
 * This client performs calls to the {@link
 * PrimeCheckServerController} using Spring WebMVC features.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Component
public class PrimeCheckClient {
    /**
     * Location of the server.
     */
    private final String baseUrl = "http://localhost:8081";

    /**
     * Synchronous client to perform HTTP requests.
     */
    @Autowired
    private RestTemplate mRestTemplate;

    /**
     * Test individual HTTP GET requests to the server to check if a
     * the {@code primeCandidates} {@link List} of {@link Integer}
     * objects are prime or not.
     *
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallel streams, else false
     */
    public Void testIndividualCalls(List<Integer> primeCandidates,
                                            Boolean parallel) {
        var stream = primeCandidates
            // Convert the List to a stream.
            .stream();

        // Conditionally convert to a parallel stream.
        if (parallel)
            stream.parallel();

        var results = stream
            // Create and send a GET request to the server to check if
            // the primeCandidate is prime or not.
            .map(primeCandidate -> WebUtils
                 .makeGetRequest(mRestTemplate,
                                 makeCheckIfPrimeUrl(primeCandidate),
                                 Integer.class))

            // Trigger the intermediate operations and collect the
            // results into a List.
            .collect(toList());

        // Display the results.
        Options.displayResults(primeCandidates, results);
        return null;
    }

    /**
     * Test passing a {@link List} of {@code primeCandidate} {@link
     * Integer} objects in one HTTP GET request to the server to
     * determine which {@link List} elements are prime or not.
     *
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallel streams, else false
     */
    public Void testListCall(List<Integer> primeCandidates,
                                    Boolean parallel) {
        ResponseEntity<Integer[]> responseEntity = mRestTemplate
            // Execute the HTTP method to the given URI template,
            // writing the given request entity to the request,
            // and returns the response as ResponseEntity.
            .exchange(makeCheckIfPrimeListUrl(WebUtils
                                                  .list2String(primeCandidates),
                                              parallel),
                      HttpMethod.GET, null,
                      Integer[].class);

        // Convert the array into a List.
        List<Integer> results = Arrays
            .asList(Objects.requireNonNull(responseEntity.getBody()));

        // Display the results.
        Options.displayResults(primeCandidates, results);
        return null;
    }

    /**
     * This factory method creates a URL that can be passed to an HTTP
     * GET request to determine if an {@code integer} is prime.

     * @param integer An integer to check for primality
     * @return A URL that can be passed to an HTTP GET request to
     *         determine if the {@code integer} is prime
     */
    private String makeCheckIfPrimeUrl(Integer integer) {
        return baseUrl
            + CHECK_IF_PRIME
            + "?primeCandidate="
            + integer;
    }

    /**
     * This factory method creates a URL that can be passed to an HTTP
     * GET request to determine if a {@code listOfIntegers} is prime.

     * @param listOfIntegers A comma-separated list of integers
     * @return A URL that can be passed to an HTTP GET request to
     *         determine if a {@code listOfIntegers} is prime
     */
    private String makeCheckIfPrimeListUrl(String listOfIntegers,
                                                  boolean parallel) {
        return baseUrl
            + CHECK_IF_PRIME_LIST
            + "?primeCandidates="
            + listOfIntegers
            + "&parallel="
            + parallel;
    }
}
