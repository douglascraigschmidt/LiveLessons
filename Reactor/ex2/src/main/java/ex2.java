import datamodels.TripRequest;
import datamodels.TripResponse;
import reactor.core.publisher.Flux;
import reactor.math.MathFlux;
import utils.CheapestPriceCollector;
import utils.TestDataFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.function.Function;

/**
 * This example demonstrates various reactive algorithms for finding
 * all the minimium values in an unordered list, which is surprisingly
 * not well documented in the literature.  The three algorithms below
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
                 1);

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Get all the flights.
        final Flux<TripResponse> flights = TestDataFactory.findFlights(sTrip);

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
    private static void printCheapestFlightsMin(Flux<TripResponse> flights) {
        System.out.println("printCheapestFlightsMin():");

        // Find the cheapest flights.
        Flux<TripResponse> lowestPrices = MathFlux
            // Find the cheapest flight.
            .min(flights, Comparator.comparing(TripResponse::getPrice))
            // Create a Flux that contains the cheapest flights.
            .flatMapMany(min -> flights
                         // Only allow flights that match the cheapest.
                         .filter(tr -> tr.getPrice().equals(min.getPrice())));

        // Print the cheapest flights.
        lowestPrices
            .subscribe(System.out::println);
    }

    /**
     * Print the cheapest flights via a two-pass algorithm that first
     * calls sort() to order the trips by price and then uses
     * takeWhile() to return the cheapest flight(s).
     */
    private static void printCheapestFlightsSorted(Flux<TripResponse> flights) {
        System.out.println("printCheapestFlightsSorted():");

        // Sort the flights from lowest to highest price.
        Flux<TripResponse> sortedFlights = flights
            .sort(Comparator.comparing(TripResponse::getPrice));

        // Create a flux containing the cheapest prices.
        Flux<TripResponse> lowestPrices = sortedFlights
            // Get the cheapest price.
            .elementAt(0)

            // Take all the elements that match the cheapest price.
            .flatMapMany(min -> sortedFlights
                         .takeWhile(tr -> tr.getPrice().equals(min.getPrice())));

        // Print the cheapest flights.
        lowestPrices
            .subscribe(System.out::println);
    }

    /**
     * Print the cheapest flights via a one-pass algorithm and a
     * custom Collector.
     */
    private static void printCheapestFlightsOnepass(Flux<TripResponse> flights) {
        System.out.println("printCheapestFlightsOnepass():");

        Flux<TripResponse> lowestPrices = flights
            // Converts a stream of TripResponses into a Flux that
            // emits the cheapest priced trips(s).
            .collect(CheapestPriceCollector.toFlux())

            // Convert the Mono into a Flux that emits the cheapest
            // priced trip(s).
            .flatMapMany(Function.identity());

        // Print the cheapest flights.
        lowestPrices
            .subscribe(System.out::println);
    }
}
