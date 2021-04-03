import datamodels.TripRequest;
import datamodels.Flight;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.math.MathFlux;
import utils.AsyncTaskBarrier;
import utils.CheapestPriceCollector;
import utils.ExchangeRate;
import utils.TestDataFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * This example demonstrates various reactive algorithms for finding
 * all the minimum values in an unordered list, which is surprisingly
 * not well documented in the literature.  These three algorithms
 * return a Flux that emits the cheapest flight(s) from a Flux of
 * available flights, which is part of an Airline Booking App that
 * we're creating for an upcoming MOOC on Reactive Microservices.
 */
public class ex2 {
    /**
     * The trip flight leg used for the tests.
     */
    private static final TripRequest sTrip = TripRequest
        .valueOf(LocalDateTime.parse("2025-01-01T07:00:00"),
                 LocalDateTime.parse("2025-02-01T19:00:00"),
                 "LHR",
                 "JFK",
                 "EUR",
                 1);

    private static final Map<String, Double> sExchangeRateCache =
        new ConcurrentHashMap<>() {
            { put("USD", 0.0); }
            { put("EUR", 0.0); }
            { put("GBP", 0.0); }
        };

    private static final ExchangeRate sExchangeRates =
        new ExchangeRate();
    
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Print the cheapest flights via a two pass algorithm that
        // uses min() and filter().
        AsyncTaskBarrier.register(ex2::printCheapestFlightsMin);

        // Print the cheapest flights via a two-pass algorithm that
        // first calls sort() to order the trips by price and then
        // uses takeWhile() to return the cheapest flight(s).
        AsyncTaskBarrier.register(ex2::printCheapestFlightsSorted);

        // Print the cheapest flights via a one-pass algorithm and a
        // custom Collector.
        AsyncTaskBarrier.register(ex2::printCheapestFlightsOnepass);

        @SuppressWarnings("ConstantConditions")
        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running.
            .block();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }

    /**
     * ...
     */
    private static Flux<Flight> convertFlightPrices() {
        String toCurrency = sTrip.getCurrency();
        Mono<Boolean> computeExchangeRates = Mono
            .fromCallable(() -> {
                sExchangeRateCache
                    // Update the rate cache with the latest rates.
                    .replaceAll((fromCurrency, rate) ->
                                    sExchangeRates.queryForExchangeRate(fromCurrency,
                                                                        toCurrency));
                return true;
            })
            .subscribeOn(Schedulers.parallel());

        Flux<Flight> flights = TestDataFactory
            // Get all the flights.
            .findFlights(sTrip)
            .subscribeOn(Schedulers.parallel());

        return flights
            .zipWith(computeExchangeRates,
                     (flight, __) ->
                     convertCurrency(toCurrency, flight));
    }

    private static Flight convertCurrency(String toCurrency, Flight flight) {
        if (!flight.getCurrency().equals(toCurrency)) {
            flight.setPrice(flight.getPrice() * sExchangeRateCache.get(flight.getCurrency()));
        }
        return flight;
    }

    /**
     * Print the cheapest flights via a two pass algorithm that uses
     * min() and filter().
     */
    private static Mono<Void> printCheapestFlightsMin() {
        System.out.println("printCheapestFlightsMin():");

        Flux<Flight> flights = convertFlightPrices();

        // Find the cheapest flights.
        Flux<Flight> lowestPrices = MathFlux
            // Find the cheapest flight.
            .min(flights,
                 Comparator.comparing(Flight::getPrice))
            // Create a Flux that contains the cheapest flights.
            .flatMapMany(min -> flights
                         // Only allow flights that match the cheapest.
                         .filter(tr -> tr.getPrice().equals(min.getPrice())));

        // Print the cheapest flights.
        return lowestPrices
            .doOnNext(flight ->  System.out.println("Cheapest flight = " + flight))
            .then();
    }

    /**
     * Print the cheapest flights via a two-pass algorithm that first
     * calls sort() to order the trips by price and then uses
     * takeWhile() to return the cheapest flight(s).
     */
    private static Mono<Void> printCheapestFlightsSorted() {
        System.out.println("printCheapestFlightsSorted():");

        // Sort the flights from lowest to highest price.
        Flux<Flight> sortedFlights = convertFlightPrices()
            .sort(Comparator.comparing(Flight::getPrice));

        // Create a flux containing the cheapest prices.
        Flux<Flight> lowestPrices = sortedFlights
            // Get the cheapest price.
            .elementAt(0)

            // Take all the elements that match the cheapest price.
            .flatMapMany(min -> sortedFlights
                         .takeWhile(tr -> tr.getPrice().equals(min.getPrice())));

        // Print the cheapest flights.
        return lowestPrices
            .doOnNext(flight ->  System.out.println("Cheapest flight = " + flight))
            .then();
    }

    /**
     * Print the cheapest flights via a one-pass algorithm and a
     * custom Collector.
     */
    private static Mono<Void> printCheapestFlightsOnepass() {
        System.out.println("printCheapestFlightsOnepass():");

        Flux<Flight> lowestPrices = convertFlightPrices()
            // Converts a stream of TripResponses into a Flux that
            // emits the cheapest priced trips(s).
            .collect(CheapestPriceCollector.toFlux())

            // Convert the Mono into a Flux that emits the cheapest
            // priced trip(s).
            .flatMapMany(Function.identity());

        // Print the cheapest flights.
        return lowestPrices
            .doOnNext(flight -> System.out.println("Cheapest flight = " + flight))
            .then();
    }
}
