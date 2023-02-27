package microservices.airlines.AA;

import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.airlines.PriceProxyAsync;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.function.Function;

/**
 * This class serves as a proxy to the asynchronous AAPrice
 * microservice that provides prices for American Airlines (AA)
 * flights.
 */
public class AAPriceProxyAsync
       extends AAPriceProxyBase
       implements PriceProxyAsync {
    /**
     * The URI that denotes the remote method to query the AA price
     * database asynchronously.
     */
    private final String mFindAAPricesURIAsync =
        "/microservices/AirlineDBs/AAASync/_getTripPrices";

    /**
     * Constructor initializes the super class.
     */
    public AAPriceProxyAsync() {
        super();
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
    public Flux<TripResponse> findTrips(Scheduler scheduler,
                                        TripRequest tripRequest) {
        return Mono
            // Return a Flux containing all TripResponse objects that
            // map to the TripRequest param.
            .fromCallable(() -> mAAPrices
                          // Create an HTTP POST request.
                          .post()

                          // Add the uri to the baseUrl.
                          .uri(mFindAAPricesURIAsync)

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
}
