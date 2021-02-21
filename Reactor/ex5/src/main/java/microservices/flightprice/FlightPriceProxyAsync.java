package microservices.flightprice;

import datamodels.TripRequest;
import datamodels.TripResponse;
import io.reactivex.rxjava3.core.Single;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.function.Function;

/**
 * This class serves as a proxy to the asynchronous FlightPrice
 * microservice.
 */
public class FlightPriceProxyAsync
       extends FlightPriceProxyBase {
    /**
     * The URI that denotes the remote method to find the best price
     * for a trip asynchronously.
     */
    private final String mFindBestPriceURIAsync =
        "/microservices/flightPriceAsync/_findBestPrice";

    /**
     * The URI that denotes the remote method to find all the matching
     * flights for a trip.
     */
    private final String mFindFlightsURIAsync =
        "/microservices/flightPriceAsync/_findFlights";

    /**
     * Finds all the flights that match the {@code tripRequest}
     * asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param tripRequest The desired trip 
     * @return A Flux that emits all the matching {@code TripResponse} objects
     */
    public Flux<TripResponse> findFlights(Scheduler scheduler,
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
    public Single<TripResponse> findFlightsRx(Scheduler scheduler,
                                              TripRequest tripRequest) {
        return Single
            // Return a Single to the best price.
            .fromPublisher(findFlights(scheduler, tripRequest));
    }

    /**
     * Finds the best price for the {@code tripRequest} asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param tripRequest The trip to price
     * @return A Mono that emits the {@code TripResponse} with the best price
     */
    public Mono<TripResponse> findBestPrice(Scheduler scheduler,
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
    public Single<TripResponse> findBestPriceRx(Scheduler scheduler,
                                                TripRequest tripRequest) {
        return Single
            // Return a Single to the best price.
            .fromPublisher(findBestPrice(scheduler, tripRequest));
    }
}
