package mathservices.client;

import mathservices.common.PrimeResult;
import mathservices.server.primality.PrimalityApplication;
import mathservices.server.primality.PrimalityController;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import mathservices.utils.WebUtils;

import java.util.List;

import static mathservices.common.Components.getRestTemplate;
import static mathservices.common.Constants.EndPoint.CHECK_PRIMALITY_LIST;
import static mathservices.common.Constants.PRIMALITY_MICROSERVICE_BASE_URL;

/**
 * This class is a proxy to the {@link PrimalityApplication} service
 * and the {@link PrimalityController}.
 */
@Component
public class PrimalityProxy {
    /**
     * This field connects the {@link PrimalityProxy} to the {@link
     * RestTemplate} that performs HTTP requests synchronously.
     */
    private final RestTemplate mRestTemplate =
        getRestTemplate(PRIMALITY_MICROSERVICE_BASE_URL);

    /**
     * Checks all the elements in the {@code primeCandidates} {@link
     * List} param for primality and return a corresponding {@link
     * List} whose {@link PrimeResult} elements indicate 0 if an
     * element is prime or the smallest factor if it's not.
     *
     * @param primeCandidates The {@link List} of {@link Integer}
     *                        objects to check for primality
     * @return An {@link List} to {@link PrimeResult} objects
     */
    public List<PrimeResult> checkPrimalities
        (List<Integer> primeCandidates) {
        return WebUtils
            // Create and send a GET request to the server to check if
            // the Integer objects in primeCandidates are prime or
            // not.
            .makeGetRequestList(mRestTemplate,
                                // Create the encoded URL.
                                UriComponentsBuilder
                                .fromPath(CHECK_PRIMALITY_LIST)
                                .queryParam("primeCandidates",
                                            WebUtils
                                            // Convert List to String.
                                            .list2String(primeCandidates))
                                .build()
                                .toString(),
                                // Return type is a PrimeResult array.
                                PrimeResult[].class);
    }
}

