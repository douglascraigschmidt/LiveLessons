package microservices.flightprice;

import datamodels.TripRequest;
import datamodels.TripResponse;
import io.reactivex.rxjava3.core.Single;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

/**
 * This class serves as a proxy to the asynchronous FlightPrice
 * microservice.
 */
public class FlightPriceProxyRSocket {
    /**
     * The message name that denotes the remote method to find the
     * best price for a trip asynchronously.
     */
    private final String mFindBestPriceMessage =
        "_findBestPrice";

    /**
     * The message name that denotes the remote method to find all the
     * matching flights for a trip.
     */
    private final String mFindFlightsMessage =
        "_findFlights";

    /**
     * Initialize the RSocketRequestor.
     */
    private final Mono<RSocketRequester> rSocketRequester = Mono
        .just(RSocketRequester.builder()
              .rsocketConnector(rSocketConnector -> rSocketConnector
                                .reconnect(Retry.fixedDelay(2,
                                                            Duration.ofSeconds(2))))
              .dataMimeType(MediaType.APPLICATION_CBOR)
              .rsocketStrategies(RSocketStrategies.builder()
                                 .encoders(encoders ->
                                           encoders.add(new Jackson2CborEncoder()))
                                 .decoders(decoders ->
                                           decoders.add(new Jackson2CborDecoder()))
                                 .build())
              .tcp("localhost", 8088));

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
            .fromCallable(() -> rSocketRequester
                          // Create the data to send to the server.
                          .map(r -> r
                               .route(mFindFlightsMessage)
                               .data(tripRequest))

                          // Get the result back from the server as a
                          // Flux<TripResponse>.
                          .flatMapMany(r -> r.retrieveFlux(TripResponse.class)))
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Flux<TripResponse>.
            .flatMapMany(Function.identity());
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
            .fromCallable(() -> rSocketRequester
                          // Create the data to send to the server.
                          .map(r -> r
                               .route(mFindBestPriceMessage)
                               .data(tripRequest))

                          // Get the result back from the server as a
                          // Mono<TripResponse>.
                          .flatMap(r -> r.retrieveMono(TripResponse.class)))
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Mono<TripResponse>.
            .flatMap(Function.identity());
    }
}
