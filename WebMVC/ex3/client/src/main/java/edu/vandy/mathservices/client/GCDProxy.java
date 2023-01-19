package edu.vandy.mathservices.client;

import edu.vandy.mathservices.common.Components;
import edu.vandy.mathservices.common.Constants;
import edu.vandy.mathservices.common.GCDResult;
import edu.vandy.mathservices.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static edu.vandy.mathservices.common.Constants.*;
import static edu.vandy.mathservices.common.Constants.EndPoint.COMPUTE_GCD_LIST;

/**
 * This class is a proxy to the {@code GCDApplication} microservice
 * and the {@code GCDController}.
 */
@Component
public class GCDProxy {
    /**
     * This field connects the {@link GCDProxy} to the {@link
     * RestTemplate} that performs HTTP requests synchronously.
     */
    @Autowired
    private RestTemplate mRestTemplate;

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
        // Create the encoded URI.
        var uri = UriComponentsBuilder
            .newInstance()
            .scheme("http")
            .port(GCD_MICROSERVICE_PORT)
            .host(HOST)
            .path(COMPUTE_GCD_LIST)
            .queryParam("integers",
                        WebUtils
                        // Convert List to String.
                        .list2String(integers))
            .build()
            .toUriString();

        return WebUtils
            // Create and send a GET request to the server to compute
            // the GCDs of the integers.
            .makeGetRequestList(mRestTemplate,
                                uri,
                                // Return type is a GCDResult array.
                                GCDResult[].class);
    }
}

