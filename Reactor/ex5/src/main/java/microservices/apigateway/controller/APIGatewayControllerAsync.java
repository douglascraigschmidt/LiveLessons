package microservices.apigateway.controller;

import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.apigateway.FlightRequest;
import microservices.exchangerate.ExchangeRateProxyAsync;
import microservices.flightprice.FlightPriceProxyAsync;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.Options;

import java.time.Duration;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP POST requests via reactive programming.  These requests
 * are mapped to method(s) that provide the external entry point into
 * the ABA microservices asynchronously.
 *
 * In Spring's approach to building RESTful web services, HTTP
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @GetMapping}, {@code @PostMapping}, {@code @PutMapping} and
 * {@code @DeleteMapping}, which correspond to the HTTP GET, POST,
 * PUT, and DELETE calls, respectively.  These components are
 * identified by the @RestController annotation below.
 *
 * WebFlux uses the {@code @PostMapping} annotation to map HTTP POST
 * requests onto methods in the {@code APIGatewayAsync}.  POST
 * requests invoked from any HTTP web client (e.g., a web browser) or
 * command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/APIGatewayAsync")
public class APIGatewayControllerAsync {
    /**
     * An async proxy to the FlightPrice microservice.
     */
    private final FlightPriceProxyAsync mFlightPriceProxyAsync =
        new FlightPriceProxyAsync();

    /**
     * An async proxy to the ExchangeRate microservice.
     */
    private final ExchangeRateProxyAsync mExchangeRateProxyAsync =
        new ExchangeRateProxyAsync();

    /**
     * Returns the best price for {@code tripRequest} using the given
     * {@code currencyConversion} via asynchronous computations.
     *
     * WebFlux maps HTTP POST requests sent to the /_findBestPrice
     * endpoint to this method.
     *
     * @param flightRequest The desired trip and currency to convert from and to
     * @return A Mono that emits the best price for the desired trip
     */
    @PostMapping("_findBestPrice")
    public Mono<TripResponse> findBestPrice(@RequestBody FlightRequest flightRequest) {
        Mono<TripResponse> tripM = mFlightPriceProxyAsync
            // Asynchronously find the best price in US dollars for
            // the tripRequest.
            .findBestPrice(Schedulers.parallel(),
                           flightRequest.tripRequest);

        Mono<Double> rateM = mExchangeRateProxyAsync
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
     * WebFlux maps HTTP POST requests sent to the /_findFlights
     * endpoint to this method.
     *
     * @param flightRequest The desired trip and currency to convert from and to
     * @return A Flux that emits all the matching {@code TripResponse} objects
     */
    @PostMapping("_findFlights")
    public Flux<TripResponse> findFlights(@RequestBody FlightRequest flightRequest) {
        Flux<TripResponse> tripF = mFlightPriceProxyAsync
            // Asynchronously find all the flights in the tripRequest.
            .findFlights(Schedulers.parallel(),
                         flightRequest.tripRequest);

        Mono<Double> rateM = mExchangeRateProxyAsync
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
