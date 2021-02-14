package microservices.AirlineDBs.AA;

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
 * This class serves as a proxy to the AAPrice microservice, which
 * provides prices for American Airlines flight legs.
 */
public class AAPriceProxy
       implements PriceProxy {
    /**
     * The URI that denotes the remote method to query the AA price
     * database.
     */
    private final String mFindAAPricesURIAsync =
            "/microservices/AirlineDBs/AA/_getTripPricesAsync";

    /**
     * The URI that denotes the remote method to query the AA price
     * database synchronously.
     */
    private final String mFindAAPricesURISync =
            "/microservices/AirlineDBs/AA/_getTripPricesSync";

    /**
     * Host/port where the server resides.
     */
    private final String mSERVER_BASE_URL =
            "http://localhost:8084";

    /**
     * The WebClient provides the means to access the AAPrice
     * microservice.
     */
    private WebClient mAAPrices = WebClient
        // Start building.
        .builder()

        // The URL where the server is running.
        .baseUrl(mSERVER_BASE_URL)

        // Build the webclient.
        .build();

    /**
     * Constructor initializes the fields.
     */
    public AAPriceProxy() {
    }

    /**
     * Returns a Flux that emits {@code TripResponse} objects that
     * match the {@code trip} param.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param trip The trip to price
     * @return A Flux that emits {@code TripResponse} objects that
     *         match the {@code trip} param
     */
    @Override
    public Flux<TripResponse> findTripsAsync(Scheduler scheduler,
                                             TripRequest trip) {
        return Mono
            // Return a Flux containing all Trip objects that map to the
            // trip param.
            .fromCallable(() -> mAAPrices
                          // Create an HTTP POST request.
                          .post()

                          // Add the uri to the baseUrl.
                          .uri(mFindAAPricesURIAsync)

                          // Encode the trip in the body of the request.
                          .bodyValue(trip)

                          // Retrieve the response.
                          .retrieve()

                          // Convert it to a Flux of TripResponse
                          // objects.
                          .bodyToFlux(TripResponse.class))

            // Schedule this to run on the given scheduler.
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
            .postForEntity(mSERVER_BASE_URL + mFindAAPricesURISync,
                          tripRequest,
                          TripResponse[].class);
        return Arrays.asList(Objects.requireNonNull(responseEntity.getBody()));
    }
}
