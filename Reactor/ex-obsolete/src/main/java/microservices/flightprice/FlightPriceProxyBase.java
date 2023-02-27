package microservices.flightprice;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * This super class factors out code that's common to the
 * FlightPriceProxyAsync and FlightPriceProxySync subclasses.
 */
public class FlightPriceProxyBase {
    /**
     * A synchronous client used to perform HTTP requests via simple
     * template method API over underlying HTTP client libraries
     */
    final RestTemplate mRestTemplate = new RestTemplate();

    /**
     * The WebClient provides the means to access the FlightPrice
     * microservice.
     */
    final WebClient mFlightPrice;

    /**
     * Host/port where the server resides.
     */
    final String mSERVER_BASE_URL =
        "http://localhost:8083";

    /**
     * Constructor initializes the fields.
     */
    public FlightPriceProxyBase() {
        mFlightPrice = WebClient
            // Start building.
            .builder()

            // The URL where the server is running.
            .baseUrl(mSERVER_BASE_URL)

            // Build the webclient.
            .build();
    }
}
