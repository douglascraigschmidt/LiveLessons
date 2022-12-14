package mathservices.client;

import mathservices.common.GCDResult;
import mathservices.server.gcd.GCDApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import mathservices.server.gcd.GCDController;
import mathservices.utils.WebUtils;

import java.util.List;

import static mathservices.common.Components.getRestTemplate;
import static mathservices.common.Constants.EndPoint.COMPUTE_GCD_LIST;
import static mathservices.common.Constants.GCD_MICROSERVICE_BASE_URL;

/**
 * This class is a proxy to the {@link GCDApplication} microservice
 * and the {@link GCDController}.
 */
@Component
public class GCDProxy {
    /**
     * This field connects the {@link GCDProxy} to the {@link
     * RestTemplate} that performs HTTP requests synchronously.
     */
    private final RestTemplate mRestTemplate =
        getRestTemplate(GCD_MICROSERVICE_BASE_URL);

    /**
     * Compute the GCD of the {@code integers} param.  This method
     * also demonstrates how structured concurrency scopes can nest.
     *
     * @param integers The {@link List} of {@link Integer} objects
     *                 upon which to compute the GCD
     * @return A {@link List} of {@link GCDResult} objects
     */
    public List<GCDResult> computeGCDs
        (List<Integer> integers) {
        return WebUtils
            // Create and send a GET request to the server to compute
            // the GCDs of the integers.
            .makeGetRequestList(mRestTemplate,
                                // Create the encoded URL.
                                UriComponentsBuilder
                                .fromPath(COMPUTE_GCD_LIST)
                                .queryParam("integers",
                                            WebUtils
                                            // Convert List to String.
                                            .list2String(integers))
                                .build()
                                .toString(),
                                // Return type is a GCDResult array.
                                GCDResult[].class);
    }
}

