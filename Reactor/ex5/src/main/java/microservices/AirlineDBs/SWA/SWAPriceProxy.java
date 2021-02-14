package microservices.AirlineDBs.SWA;

import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.AirlineDBs.PriceProxy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * This class serves as a proxy to the SWAPrice microservice, which
 * provides prices for Southwest Airlines (SWA) flights.

 */
public class SWAPriceProxy 
       implements PriceProxy {
    /**
     * The URI that denotes the remote method to query the SWA price
     * database asynchronously.
     */
    private final String mFindSWAPricesURIAsync =
            "/microservices/AirlineDBs/SWA/_getTripPricesAsync";

    /**
     * The URI that denotes the remote method to query the SWA price
     * database synchronously.
     */
    private final String mFindSWAPricesURISync =
            "/microservices/AirlineDBs/SWA/_getTripPricesSync";

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
     * Returns a Flux that emits {@code TripResponse} objects that
     * match the {@code trip} param.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param tripRequest The trip to price
     * @return A Flux that emits {@code TripResponse} objects that
     *         match the {@code trip} param
     */
    @Override
    public Flux<TripResponse> findTripsAsync(Scheduler scheduler,
                                             TripRequest tripRequest) {
        return Mono
            // Return a Flux containing all TripResponse objects that
            // map to the TripRequest param.
            .fromCallable(() -> mSWAPrices
                          // Create an HTTP POST request.
                          .post()

                          // Add the uri to the baseUrl.
                          .uri(mFindSWAPricesURIAsync)

                          // Encode the tripRequest in the body of the
                          // request.
                          .bodyValue(tripRequest)

                          // Retrieve the response.
                          .retrieve()

                          // Convert it to a Flux of TripResponse
                          // objects.
                          .bodyToFlux(TripResponse.class))

            // Schedule this computation to run on the given
            // scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Flux<TripResponse>.
            .flatMapMany(Function.identity());
    }

    /**
     * Returns a List that contains {@code TripResponse} objects that
     * match the {@code trip} param.
     *
     * @param tripRequest The trip to price
     * @return A List that contains {@code TripResponse} objects that
     *         match the {@code trip} param
     */
    @Override
    public List<TripResponse> findTripsSync(TripRequest tripRequest) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<TripResponse[]> responseEntity = restTemplate
            .postForEntity(mSERVER_BASE_URL + mFindSWAPricesURISync,
                          tripRequest,
                          TripResponse[].class);
        return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
    }
}
