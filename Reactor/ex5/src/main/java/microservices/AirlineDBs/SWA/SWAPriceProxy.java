package microservices.AirlineDBs.SWA;

import datamodels.Trip;
import microservices.AirlineDBs.PriceProxy;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

/**
 * This class serves as a proxy to the SWAPrice microservice.
 */
public class SWAPriceProxy 
       implements PriceProxy {
    /**
     * The URI that denotes the remote method to query the SWA price database.
     */
    private final String mFindSWAPricesURIAsync =
            "/microservices/AirlineDBs/SWA/_getTripPrices";

    /**
     * Host/port where the server resides.
     */
    private final String mSERVER_BASE_URL =
            "http://localhost:8082";

    /**
     * The WebClient provides the means to access the SWAPrice
     * microservice.
     */
    private WebClient mSWAPrices = WebClient
        // Start building.
        .builder()

        // The URL where the server is running.
        .baseUrl(mSERVER_BASE_URL)

        // Build the webclient.
        .build();

    /**
     * Constructor initializes the fields.
     */
    public SWAPriceProxy() {
    }

    /**
     * Finds the best price for the {@code trip} asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param trip The trip to price
     * @return A Flux of {@code Trip} objects corresponding to the given {@code trip}
     */
    @Override
    public Flux<Trip> findTripsAsync(Scheduler scheduler,
                                     Trip trip) {
        // Return a Flux containing all Trip objects that map to the
        // trip param.
        return mSWAPrices
            // Create an HTTP POST request.
            .post()

            // Add the uri to the baseUrl.
            .uri(mFindSWAPricesURIAsync)

            // Encode the trip in the body of the request.
            .bodyValue(trip)

            // Retrieve the response.
            .retrieve()

            // Convert it to a Flux of Trips.
            .bodyToFlux(Trip.class)

            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler);
    }
}
