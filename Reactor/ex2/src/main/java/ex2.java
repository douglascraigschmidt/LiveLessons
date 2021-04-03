import datamodels.TripRequest;
import datamodels.Flight;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;
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
        // Call various algorithms to print the cheapest flights.
        printCheapestFlights(convertFlightPrices(sTrip.getCurrency()));
    }

    /**
     * ...
     */
    private static Flux<Flight> convertFlightPrices(String toCurrency) {
        // @@ Monte, this will run asynchronously.
        // Update the rate cache with the latest rates.
        sExchangeRateCache.replaceAll((fromCurrency, rate) ->
                                      sExchangeRates.queryForExchangeRate(fromCurrency, toCurrency));

        // @@ Monte, this will also run asynchronously.
        // Get all the flights.
        final Flux<Flight> flights = TestDataFactory.findFlights(sTrip);

        // @@ Monte, when both of the async calls above complete we'll zip them
        // together like this:
        return flights
            .map(flight ->
                     convertCurrency(toCurrency, flight));
    }

    private static Flight convertCurrency(String toCurrency, Flight flight) {
        if (!flight.getCurrency().equals(toCurrency)) {
            flight.setPrice(flight.getPrice() * sExchangeRateCache.get(flight.getCurrency()));
        }
        return flight;
    }

    /**
     * Call various algorithms to print the cheapest flights.
     */
    private static void printCheapestFlights(Flux<Flight> flights) {
        // Print the cheapest flights via a two pass algorithm that
        // uses min() and filter().
        printCheapestFlightsMin(flights);

        // Print the cheapest flights via a two-pass algorithm that
        // first calls sort() to order the trips by price and then
        // uses takeWhile() to return the cheapest flight(s).
        printCheapestFlightsSorted(flights);

        // Print the cheapest flights via a one-pass algorithm and a
        // custom Collector.
        printCheapestFlightsOnepass(flights);
    }

    /**
     * Print the cheapest flights via a two pass algorithm that uses
     * min() and filter().
     */
    private static void printCheapestFlightsMin(Flux<Flight> flights) {
        System.out.println("printCheapestFlightsMin():");

        // Find the cheapest flights.
        Flux<Flight> lowestPrices = MathFlux
            // Find the cheapest flight.
            .min(flights, Comparator.comparing(Flight::getPrice))
            // Create a Flux that contains the cheapest flights.
            .flatMapMany(min -> flights
                         // Only allow flights that match the cheapest.
                         .filter(tr -> tr.getPrice().equals(min.getPrice())));

        // Print the cheapest flights.
        lowestPrices
            .subscribe(flight ->  System.out.println("Cheapest flight = " + flight));
    }

    /**
     * Print the cheapest flights via a two-pass algorithm that first
     * calls sort() to order the trips by price and then uses
     * takeWhile() to return the cheapest flight(s).
     */
    private static void printCheapestFlightsSorted(Flux<Flight> flights) {
        System.out.println("printCheapestFlightsSorted():");

        // Sort the flights from lowest to highest price.
        Flux<Flight> sortedFlights = flights
            .sort(Comparator.comparing(Flight::getPrice));

        // Create a flux containing the cheapest prices.
        Flux<Flight> lowestPrices = sortedFlights
            // Get the cheapest price.
            .elementAt(0)

            // Take all the elements that match the cheapest price.
            .flatMapMany(min -> sortedFlights
                         .takeWhile(tr -> tr.getPrice().equals(min.getPrice())));

        // Print the cheapest flights.
        lowestPrices
            .subscribe(flight ->  System.out.println("Cheapest flight = " + flight));
    }

    /**
     * Print the cheapest flights via a one-pass algorithm and a
     * custom Collector.
     */
    private static void printCheapestFlightsOnepass(Flux<Flight> flights) {
        System.out.println("printCheapestFlightsOnepass():");

        Flux<Flight> lowestPrices = flights
            // Converts a stream of TripResponses into a Flux that
            // emits the cheapest priced trips(s).
            .collect(CheapestPriceCollector.toFlux())

            // Convert the Mono into a Flux that emits the cheapest
            // priced trip(s).
            .flatMapMany(Function.identity());

        // Print the cheapest flights.
        lowestPrices
            .subscribe(flight ->  System.out.println("Cheapest flight = " + flight));
    }
}
