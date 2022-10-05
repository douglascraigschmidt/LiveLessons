package microservices.apigateway;

import datamodels.AirportInfo;
import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import datamodels.TripResponse;
import io.reactivex.rxjava3.core.Observable;
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
 * This class serves as a proxy to the asynchronous APIGateway
 * microservice that uses the RSocket framework to access all the
 * backend microservices.
 */
public class APIGatewayProxyRSocket {
    /**
     * The message name that denotes a remote method to find information about
     * all the airports asynchronously.
     */
    private final String mFindAirportsMessage =
        "_getAirportList";

    /**
     * The message name that denotes a remote method to find the best
     * flight price asynchronously.
     */
    private final String mFindBestPriceMessage =
        "_findBestPrice";

    /**
     * The message name that denotes a remote method to find all
     * flights asynchronously.
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
              .tcp("localhost", 8089));

    /**
     * Returns a Flux that emits {@code AirportInfo} objects.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @return A Flux that emits {@code AirportInfo} objects
     */
    public Flux<AirportInfo> findAirportInfo(Scheduler scheduler) {
        return Mono
            // Return a Flux containing the list of airport
            // information.
            .fromCallable(() -> rSocketRequester
                    // Create the data to send to the server.
                    .map(r -> r
                            .route(mFindAirportsMessage))

                    // Get the result back from the server as a
                    // Flux<TripResponse>.
                    .flatMapMany(r -> r.retrieveFlux(AirportInfo.class)))

            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Flux<AirportInfo>.
            .flatMapMany(Function.identity());
    }

    /**
     * Returns an Observable that emits {@code AirportInfo} objects.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @return An Observable that emits {@code AirportInfo} objects
     */
    public Observable<AirportInfo> findAirportInfoRx(Scheduler scheduler) {
        return Observable
            // Return an Observable that emits the AirportInfo
            // objects.
            .fromPublisher(findAirportInfo(scheduler));
    }

    /**
     * Returns the best price for {@code tripRequest} using the given
     * {@code currencyConversion} via asynchronous computations.
     *
     * @param scheduler The Scheduler context in which to run the
     *                  operation
     * @param tripRequest The desired trip to take
     * @param currencyConversion The currency to convert from and to
     * @return A Mono that emits the best priced {@code TripResponse}
     */
    public Mono<TripResponse> findBestPrice(Scheduler scheduler,
                                            TripRequest tripRequest,
                                            CurrencyConversion currencyConversion) {
        // Create a FlightRequest from the tripRequest and
        // currencyConversion.
        FlightRequest flightRequest =
            new FlightRequest(tripRequest, currencyConversion);

        // Return a Mono that emits the best priced TripResponse.
        return Mono
            .fromCallable(() -> rSocketRequester
                          // Create the data to send to the server.
                          .map(r -> r
                               .route(mFindBestPriceMessage)
                               .data(flightRequest))

                          // Get the result back from the server as a
                          // TripResponse.
                          .flatMap(r -> r.retrieveMono(TripResponse.class)))
                        
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
     * @param currencyConversion The currency to convert from and to
     * @return A Single that emits the {@code TripResponse} with the best price
     */
    public Single<TripResponse> findBestPriceRx(Scheduler scheduler,
                                                TripRequest tripRequest,
                                                CurrencyConversion currencyConversion) {
        return Single
            // Return a Single to the best price.
            .fromPublisher(findBestPrice(scheduler,
                                         tripRequest,
                                         currencyConversion));
    }

    /**
     * Finds all the flights that match the {@code tripRequest}
     * asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param tripRequest The desired trip to take
     * @param currencyConversion The currency to convert from and to
     * @return A Flux that emits all the matching {@code TripResponse} objects
     */
    public Flux<TripResponse> findFlights(Scheduler scheduler,
                                          TripRequest tripRequest,
                                          CurrencyConversion currencyConversion) {
        // Create a FlightRequest from the tripRequest and
        // currencyConversion.
        FlightRequest flightRequest =
            new FlightRequest(tripRequest, currencyConversion);

        // Return a Flux that emits all flights matching tripRequest.
        return Mono
            .fromCallable(() -> rSocketRequester
                          // Create the data to send to the server.
                          .map(r -> r
                               .route(mFindFlightsMessage)
                               .data(flightRequest))

                          // Get the result back from the server as a
                          // Flux<TripResponse>.
                          .flatMapMany(r -> r.retrieveFlux(TripResponse.class)))

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
     * @param currencyConversion The currency to convert from and to
     * @return An Observable that emits all the matching {@code TripResponse} objects
     */
    public Observable<TripResponse> findFlightsRx(Scheduler scheduler,
                                                  TripRequest tripRequest,
                                                  CurrencyConversion currencyConversion) {
        return Observable
            // Return an Observable that emits all the matching
            // flights.
            .fromPublisher(findFlights(scheduler,
                                       tripRequest,
                                       currencyConversion));
    }
}
