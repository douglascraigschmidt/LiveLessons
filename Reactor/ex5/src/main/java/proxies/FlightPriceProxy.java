package proxies;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.function.Function;

/**
 * This class serves as a proxy to the FlightPrice microservice.
 */
public class FlightPriceProxy {
    /**
     * The URI that finds the best price.
     */
    private final String mFindBestPriceURIM = "/microservices/flightPrice/_bestPriceM";

    /**
     * The WebClient provides the means to access the FlightPrice
     * microservice.
     */
    private final WebClient mFlightPrice;

    /**
     * Host/post where the server resides.
     */
    private final String mSERVER_BASE_URL =
        "http://localhost:8080";

    /**
     * Constructor initializes the fields.
     */
    public FlightPriceProxy() {
        mFlightPrice = WebClient
            // Start building.
            .builder()

            // The URL where the server is running.
            .baseUrl(mSERVER_BASE_URL)

            // Build the webclient.
            .build();
    }

    /**
     * Finds the best price for the {@code flightLeg} asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param flightLeg The flight leg to price
     * @return A Mono containing the best price.
     */
    public Mono<Double> findBestPriceAsync(Scheduler scheduler,
                                           String flightLeg) {
        // Return a Mono to the best price.
        return Mono
            .fromCallable(() -> mFlightPrice
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mFindBestPriceURIM)
                 .queryParam("flightLeg", flightLeg)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a Mono of Double.
            .bodyToMono(Double.class))
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Mono<Double>.
            .flatMap(Function.identity());
    }
}
