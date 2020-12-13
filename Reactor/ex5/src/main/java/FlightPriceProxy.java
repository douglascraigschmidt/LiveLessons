import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;

/**
 * This class serves as a proxy to the FlightPrice microservice.
 */
class FlightPriceProxy {
    /**
     * The URI that finds the best price.
     */
    private final String mFindBestPriceURI = "/flightPrice/_bestPrice";

    /**
     * The WebClient provides the means to access the flight
     * micro-service.
     */
    private final WebClient mFlight;

    /**
     * Host/post where the server resides.
     */
    private final String mSERVER_BASE_URL =
        "http://localhost:8080";

    /**
     * Constructor initializes the fields to initialize a flight
     * that emits a stream of random numbers.
     */
    public FlightPriceProxy() {
        mFlight = WebClient
            // Start building.
            .builder()

            // The URL where the server is running.
            .baseUrl(mSERVER_BASE_URL)

            // Build the webclient.
            .build();
    }

    /**
     * 
     *
     * @param scheduler
     * @param flightLeg
     * @return
     */
    public Mono<Double> findBestPrice(Scheduler scheduler,
                                      String flightLeg) {
        // Return a Mono to the best price.
        return mFlight
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mFindBestPriceURI)
                 .queryParam("flightLeg", flightLeg)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a Mono of doubles.
            .bodyToMono(Double.class)
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler);
    }
}
