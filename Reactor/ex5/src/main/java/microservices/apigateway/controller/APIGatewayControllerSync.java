package microservices.apigateway.controller;

import datamodels.TripResponse;
import microservices.apigateway.FlightRequest;
import microservices.exchangerate.ExchangeRateProxySync;
import microservices.flightprice.FlightPriceProxySync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.Options;

import java.time.Duration;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP POST requests via object-oriented programming.  These
 * requests are mapped to method(s) that provide the external entry
 * point into the ABA microservices synchronously.
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
 * requests onto methods in the {@code APIGatewaySync}.  POST requests
 * invoked from any HTTP web client (e.g., a web browser) or
 * command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/exchangeRateSync")
public class APIGatewayControllerSync {
    /**
     * An async proxy to the FlightPrice microservice.
     */
    private final FlightPriceProxySync mFlightPriceProxySync =
        new FlightPriceProxySync();

    /**
     * An async proxy to the ExchangeRate microservice.
     */
    private final ExchangeRateProxySync mExchangeRateProxySync =
        new ExchangeRateProxySync();

    /**
     * Returns the best price for {@code tripRequest} using the given
     * {@code currencyConversion} via synchronous computations.
     *
     * WebFlux maps HTTP POST requests sent to the /_findBestPrice
     * endpoint to this method.
     *
     * @param flightRequest The desired trip and currency to convert from and to
     * @return A {@code TripResponse} that contains the best price for the desired trip
     */
    @PostMapping("_findBestPrice")
    public TripResponse findBestPrice(FlightRequest flightRequest) {
        TripResponse trip = mFlightPriceProxySync
            // Synchronously find the best price in US dollars for the
            // tripRequest.
            .findBestPrice(flightRequest.tripRequest,
                           Options.instance().maxTimeout());

        double rate = mExchangeRateProxySync
            // Synchronously determine the exchange rate.
            .queryForExchangeRate(flightRequest.currencyConversion);

        // When trip and rate complete convert the price.  If these
        // async operations take more than {@code maxTime} then throw
        // the TimeoutException.
        return combineAndConvertResults(trip,
                                        rate,
                                        Options.instance().maxTimeout());
    }

    /**
     * Returns all flights for {@code tripRequest} using the given
     * {@code currencyConversion} via synchronous
     * computations/communications.
     *
     * WebFlux maps HTTP POST requests sent to the /_findFlights
     * endpoint to this method.
     *
     * @param flightRequest The desired trip and currency to convert from and to
     * @return A List that contains all the matching {@code TripResponse} objects
     */
    @PostMapping("_findFlights")
    public List<TripResponse> findFlights(FlightRequest flightRequest) {
        List<TripResponse> trip = mFlightPriceProxySync
            // Synchronously find all the flights in the tripRequest.
            .findFlights(flightRequest.tripRequest,
                         Options.instance().maxTimeout());

        Double rate = mExchangeRateProxySync
            // Synchronously determine the exchange rate.
            .queryForExchangeRate(flightRequest.currencyConversion);

        // When trip and rate complete convert the price.  If these
        // async operations take more than {@code maxTime} then throw
        // the TimeoutException.
        return combineAndConvertResults(trip,
                                        rate,
                                        Options.instance().maxTimeout());
    }

    /**
     * When {@code trip} and {@code rate} complete convert the price
     * based on the exchange rate.  If these operations take more than
     * {@code maxTime} then the TimeoutException is thrown.
     *
     * @param trips The List of TripResponse objects
     * @param rate The exchange rate
     * @param maxTime Max time to wait for processing to complete
     * @return A conversion of best price
     */
    private static List<TripResponse> combineAndConvertResults(List<TripResponse> trips,
                                                               double rate,
                                                               Duration maxTime) {
        return trips
            // Convert trips to a stream.
            .stream()

            // Convert each trip using the exchange rate.
            .map(trip -> trip.convert(rate))

            // Trigger intermediate operation processing and collect
            // the results into a List.
            .collect(toList());
    }

    /**
     * When {@code trip} and {@code rate} complete convert the price
     * based on the exchange rate.  If these operations take more than
     * {@code maxTime} then the TimeoutException is thrown.
     *
     * @param trip The best price for a flight leg
     * @param rate The exchange rate
     * @param maxTime Max time to wait for processing to complete
     * @return A conversion of best price
     */
    private static TripResponse combineAndConvertResults(TripResponse trip,
                                                         double rate,
                                                         Duration maxTime) {
        return trip.convert(rate);
    }
}
