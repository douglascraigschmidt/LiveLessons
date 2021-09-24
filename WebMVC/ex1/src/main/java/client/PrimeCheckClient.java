package client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import server.PrimeCheckController;
import utils.Options;
import utils.WebUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static common.Constants.EndPoint.CHECK_IF_PRIME;
import static common.Constants.EndPoint.CHECK_IF_PRIME_LIST;
import static common.Constants.SERVER_BASE_URL;
import static java.util.stream.Collectors.toList;

/**
 * This client performs calls to the {@link PrimeCheckController}
 * using Spring WebMVC features.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@Component
public class PrimeCheckClient {
    /**
     * Location of the server.
     */
    private final String mBaseUrl = SERVER_BASE_URL;

    /**
     * This auto-wired field connects the {@link
     * PrimeCheckClient} to the {@link RestTemplate}
     * that performs HTTP requests synchronously.
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
            // Perform a remote call for each primeCandidate.
            .map(primeCandidate -> WebUtils
                 // Create and send a GET request to the server to
                 // check if the primeCandidate is prime or not.
                 .makeGetRequest(mRestTemplate,
                                 // Create the encoded URL.
                                 makeCheckIfPrimeUrl(primeCandidate),
                                 // The return type is an Integer.
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
        // Create the encoded URL.
        var url = makeCheckIfPrimeListUrl
            (WebUtils
             // Convert the List to a String.
             .list2String(primeCandidates),
             // Use parallel streams or not.
             parallel);

        ResponseEntity<Integer[]> responseEntity = mRestTemplate
            // Send an HTTP GET request to the given URL and return
            // the response as ResponseEntity containing an Integer.
            .exchange(url,
                      // Send via an HTTP GET request.
                      HttpMethod.GET, null,
                      // The return type is an Integer.
                      Integer[].class);

        // Convert the array in the response into a List.
        List<Integer> results = List
            .of(Objects.requireNonNull(responseEntity.getBody()));

        // Display the results.
        Options.displayResults(primeCandidates, results);
        return null;
    }

    /**
     * This factory method creates a URL that can be passed to an HTTP
     * GET request to determine if an {@link Integer} is prime.
     *
     * @param integer An {@link Integer} to check for primality
     * @return A URL that can be passed to an HTTP GET request to
     *         determine if {@code integer} is prime
     */
    private String makeCheckIfPrimeUrl(Integer integer) {
        return mBaseUrl
            + CHECK_IF_PRIME
            + "?primeCandidate="
            + integer;
    }

    /**
     * This factory method creates a URL that can be passed to an HTTP
     * GET request to determine if a {@code listOfIntegers} is prime.
     *
     * @param listOfIntegers A comma-separated list of integers
     * @return A URL that can be passed to an HTTP GET request to
     *         determine if a {@code listOfIntegers} is prime
     */
    private String makeCheckIfPrimeListUrl(String listOfIntegers,
                                           boolean parallel) {
        return mBaseUrl
            + CHECK_IF_PRIME_LIST
            + "?primeCandidates="
            + listOfIntegers
            + "&parallel="
            + parallel;
    }
}
