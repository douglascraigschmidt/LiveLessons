package microservices.apigateway.controller;

import datamodels.AirportInfo;
import datamodels.TripResponse;
import microservices.airports.AirportListProxyAsync;
import microservices.apigateway.FlightRequest;
import microservices.exchangerate.ExchangeRateProxyAsync;
import microservices.exchangerate.ExchangeRateProxyRSocket;
import microservices.flightprice.FlightPriceProxyAsync;
import microservices.flightprice.FlightPriceProxyRSocket;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.Options;

import java.time.Duration;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle RSocket requests via reactive programming.  These requests
 * are mapped to method(s) that provide the external entry point into
 * the ABA microservices asynchronously.
 *
 * In Spring's approach to building RSocket services, message
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @MessageMapping}.  These components are
 * identified by the @Controller annotation below.
 *
 * WebFlux uses the {@code @MessageMapping} annotation to map RSocket
 * requests onto methods in the {@code APIGatewayControllerRSocket}.
 */
@Controller
public class APIGatewayControllerRSocket {
    /**
     * An RSocket proxy to the FlightPrice microservice.
     */
    private final FlightPriceProxyRSocket mFlightPriceProxyRSocket =
            new FlightPriceProxyRSocket();

    /**
     * An RSocket proxy to the ExchangeRate microservice.
     */
    private final ExchangeRateProxyRSocket mExchangeRateProxyRSocket =
        new ExchangeRateProxyRSocket();

    /**
     * An async proxy to the AirportList microservice
     */
    private final AirportListProxyAsync mAirportListProxy =
        new AirportListProxyAsync();

    /**
     * This method finds information about all the airports
     * asynchronously.
     *
     * WebFlux maps HTTP GET requests sent to the /_getAirportList
     * endpoint to this method.
     *
     * @return A Flux that emits all {@code AirportInfo} objects
     */
    @GetMapping("_getAirportList")
    public Flux<AirportInfo> getAirportInfo() {
        return mAirportListProxy.findAirportInfo(Schedulers.parallel());
    }

    /**
     * Returns the best price for {@code tripRequest} using the given
     * {@code currencyConversion} via asynchronous computations.
     *
     * WebFlux maps RSocket requests sent to the _findBestPrice
     * endpoint to this method.
     *
     * @param flightRequest The desired trip and currency to convert from and to
     * @return A Mono that emits the best price for the desired trip
     */
    @MessageMapping("_findBestPrice")
    public Mono<TripResponse> findBestPrice(@RequestBody FlightRequest flightRequest) {
        Mono<TripResponse> tripM = mFlightPriceProxyRSocket
            // Asynchronously find the best price in US dollars for
            // the tripRequest.
            .findBestPrice(Schedulers.parallel(),
                           flightRequest.tripRequest);

        Mono<Double> rateM = mExchangeRateProxyRSocket
            // Asynchronously determine the exchange rate.
            .queryForExchangeRate(Schedulers.parallel(),
                                  flightRequest.currencyConversion);

        // When tripM and rateM complete convert the price.  If these
        // async operations take more than {@code maxTime} then throw
        // the TimeoutException.
        return combineAndConvertResults(tripM,
                                        rateM,
                                        Options.instance().maxTimeout());
    }

    /**
     * Returns all flights for {@code tripRequest} using the given
     * {@code currencyConversion} via asynchronous computations/communications.
     *
     * WebFlux maps RSocket requests sent to the _findFlights endpoint
     * to this method.
     *
     * @param flightRequest The desired trip and currency to convert from and to
     * @return A Flux that emits all the matching {@code TripResponse} objects
     */
    @MessageMapping("_findFlights")
    public Flux<TripResponse> findFlights(@RequestBody FlightRequest flightRequest) {
        Flux<TripResponse> tripF = mFlightPriceProxyRSocket
            // Asynchronously find all the flights in the tripRequest.
            .findFlights(Schedulers.parallel(),
                         flightRequest.tripRequest);

        Mono<Double> rateM = mExchangeRateProxyRSocket
            // Asynchronously determine the exchange rate.
            .queryForExchangeRate(Schedulers.parallel(),
                                  flightRequest.currencyConversion);

        // When tripF and rateM complete convert the price.  If these
        // async operations take more than {@code maxTime} then throw
        // the TimeoutException.
        return combineAndConvertResults(tripF,
                                        rateM,
                                        Options.instance().maxTimeout());
    }

    /**
     * When {@code tripF} and {@code rateM} complete convert the price
     * based on the exchange rate.  If these operations take more than
     * {@code maxTime} then the TimeoutException is thrown.
     *
     * @param tripF Emits the TripResponse objects
     * @param rateM Emits the exchange rate
     * @param maxTime Max time to wait for processing to complete
     * @return A conversion of best price
     */
    private static Flux<TripResponse> combineAndConvertResults(Flux<TripResponse> tripF,
                                                               Mono<Double> rateM,
                                                               Duration maxTime) {
        return rateM
            // When rateM emits use the resulting exchange rate to update
            // all trip responses accordingly.
            .flatMapMany(rate -> tripF
                         // map() is called when both the Flux and
                         // Mono complete their processing to convert
                         // the price using the exchange rate.
                         .map(trip -> trip.convert(rate)))

            // If the total processing takes more than maxTime a
            // TimeoutException will be thrown.
            .timeout(maxTime);
    }

    /**
     * When {@code tripM} and {@code rateM} complete convert the price
     * based on the exchange rate.  If these operations take more than
     * {@code maxTime} then the TimeoutException is thrown.
     *
     * @param tripM Emits the best price for a flight leg
     * @param rateM Emits the exchange rate
     * @param maxTime Max time to wait for processing to complete
     * @return A conversion of best price
     */
    private static Mono<TripResponse> combineAndConvertResults(Mono<TripResponse> tripM,
                                                               Mono<Double> rateM,
                                                               Duration maxTime) {
        return Mono
            // Convert the price using the given exchange rate when
            // both previous Monos complete their processing.
            .zip(rateM, tripM, (rate, trip) -> trip.convert(rate))

            // If the total processing takes more than maxTime a
            // TimeoutException will be thrown.
            .timeout(maxTime);
    }
}
