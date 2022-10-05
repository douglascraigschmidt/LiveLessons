package microservices.airlines.AA;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * This super class factors out code that's common to the
 * AAPriceProxyAsync and AAPriceProxySync subclasses.
 */
public class AAPriceProxyBase {
    /**
     * A synchronous client used to perform HTTP requests via simple
     * template method API over underlying HTTP client libraries
     */
    final RestTemplate mRestTemplate = new RestTemplate();

    /**
     * Host/port where the server resides.
     */
    final String mSERVER_BASE_URL =
            "http://localhost:8084";

    /**
     * The WebClient provides the means to access the AAPrice
     * microservice.
     */
    WebClient mAAPrices = WebClient
        // Start building.
        .builder()

        // The URL where the server is running.
        .baseUrl(mSERVER_BASE_URL)

        // Build the webclient.
        .build();
}
