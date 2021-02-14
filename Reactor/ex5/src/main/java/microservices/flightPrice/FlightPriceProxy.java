package microservices.flightPrice;

import datamodels.TripRequest;
import datamodels.TripResponse;
import io.reactivex.rxjava3.core.Single;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 * This class serves as a proxy to the FlightPrice microservice.
 */
public class FlightPriceProxy {
    /**
     * The URI that denotes the remote method to find the best price
     * for a trip asynchronously.
     */
    private final String mFindBestPriceURIAsync =
        "/microservices/flightPrice/_bestPriceAsync";

    /**
     * The URI that denotes the remote method to find the best price
     * for a trip synchronously.
     */
    private final String mFindBestPriceURISync =
            "/microservices/flightPrice/_bestPriceSync";

    /**
     * The URI that denotes the remote method to find all the matching
     * flights for a trip.
     */
    private final String mFindFlightsURIAsync =
        "/microservices/flightPrice/_findFlightsAsync";

    /**
     * The URI that denotes the remote method to find all the matching
     * flights for a trip.
     */
    private final String mFindFlightsURISync =
            "/microservices/flightPrice/_findFlightsSync";

    /**
     * The WebClient provides the means to access the FlightPrice
     * microservice.
     */
    private final WebClient mFlightPrice;

    /**
     * Host/port where the server resides.
     */
    private final String mSERVER_BASE_URL =
        "http://localhost:8083";

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
     * Finds all the flights that match the {@code tripRequest}
     * asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param tripRequest The desired trip 
     * @return A Flux that emits all the matching {@code TripResponse} objects
     */
    public Flux<TripResponse> findFlightsAsync(Scheduler scheduler,
                                               TripRequest tripRequest) {
        // Return a Flux that emits all the matching flights.
        return Mono
            .fromCallable(() -> mFlightPrice
                          // Create an HTTP POST request.
                          .post()

                          // Add the uri to the baseUrl.
                          .uri(mFindFlightsURIAsync)

                          // Encode the trip in the body of the request.
                          .bodyValue(tripRequest)

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
     * Finds all the flights that match the {@code tripRequest}
     * asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param tripRequest The desired trip 
     * @return An Observable that emits all the matching {@code TripResponse} objects
     */
    public Single<TripResponse> findFlightsAsyncRx(Scheduler scheduler,
                                                   TripRequest tripRequest) {
        return Single
            // Return a Single to the best price.
            .fromPublisher(findFlightsAsync(scheduler, tripRequest));
    }

    /**
     * Finds all the flights that match the {@code tripRequest}
     * synchronously.
     *
     * @param tripRequest The desired trip 
     * @param maxTime Max time to wait before throwing TimeoutException
     * @return A Flux that emits all the matching {@code TripResponse} objects
     */
    public Flux<TripResponse> findFlightsSync(TripRequest tripRequest,
                                              Duration maxTime) {
        // Return a Mono to the best price.
        return Mono
            .fromCallable(() -> mFlightPrice
                          // Create an HTTP POST request.
                          .post()

                          // Add the uri to the baseUrl.
                          .uri(mFindBestPriceURIAsync)

                          // Encode the trip in the body of the request.
                          .bodyValue(tripRequest)

                          // Retrieve the response.
                          .retrieve()

                          // Convert it to a Mono of Trip.
                          .bodyToMono(TripResponse.class))

            // De-nest the result so it's a Flux<TripResponse>.
            .flatMapMany(Function.identity())

            // If the total processing takes more than maxTime a
            // TimeoutException will be thrown.
            .timeout(maxTime);
    }

    /**
     * Finds the best price for the {@code tripRequest} asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param tripRequest The trip to price
     * @return A Mono that emits the {@code TripResponse} with the best price
     */
    public Mono<TripResponse> findBestPriceAsync(Scheduler scheduler,
                                                 TripRequest tripRequest) {
        // Return a Mono to the best price.
        return Mono
            .fromCallable(() -> mFlightPrice
                          // Create an HTTP POST request.
                          .post()

                          // Add the uri to the baseUrl.
                          .uri(mFindBestPriceURIAsync)

                          // Encode the trip in the body of the request.
                          .bodyValue(tripRequest)

                          // Retrieve the response.
                          .retrieve()

                          // Convert it to a Mono of Trip.
                          .bodyToMono(TripResponse.class))
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Mono<TripResponse>.
            .flatMap(Function.identity());
    }

    /**
     * Finds the best price for the {@code tripRequest} asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param tripRequest The trip to price
     * @return A Single that emits the {@code TripResponse} with the best price
     */
    public Single<TripResponse> findBestPriceAsyncRx(Scheduler scheduler,
                                                     TripRequest tripRequest) {
        return Single
            // Return a Single to the best price.
            .fromPublisher(findBestPriceAsync(scheduler, tripRequest));
    }

    /**
     * Finds the best price for the {@code tripRequest} synchronously.
     *
     * @param tripRequest The trip to price
     * @param maxTime Max time to wait before throwing TimeoutException
     * @return A TripResponse containing the best price
     */
    public TripResponse findBestPriceSync(TripRequest tripRequest,
                                          Duration maxTime) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<TripResponse> responseEntity = restTemplate
                .postForEntity(mSERVER_BASE_URL + mFindBestPriceURISync,
                        tripRequest,
                        TripResponse.class);
        return Objects.requireNonNull(responseEntity.getBody());
    }
}
