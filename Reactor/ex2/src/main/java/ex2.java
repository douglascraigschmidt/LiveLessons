import datamodels.TripRequest;
import datamodels.TripResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;
import utils.TestDataFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * This example demonstrates various algorithms for finding the
 * cheapest flight(s) from a Flux of TripResponses.
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

        printCheapestFlightsMin(flights);
        printCheapestFlightsSorted(flights);
        printCheapestFlightsOnepass(flights);
    }

    /**
     * Print the cheapest flights via a two pass algorithm that uses
     * min() and filter().
     */
    private static void printCheapestFlightsMin(Flux<TripResponse> flights) {
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
     * Define a collector that converts a stream of TripResponses into
     * a Flux that emits the cheapest priced trips(s).
     */
    private static class CheapestPriceCollector
                   implements Collector<TripResponse, List<TripResponse>, Flux<TripResponse>> {
        /**
         * The minimum value seen by the collector.
         */
        Double mMin = Double.MAX_VALUE;

        @Override
        public Supplier<List<TripResponse>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<TripResponse>, TripResponse> accumulator() {
            return (lowestPrices, tripResponse) -> {
                if (tripResponse.getPrice() < mMin) {
                    lowestPrices.clear();
                    lowestPrices.add(tripResponse);
                    mMin = tripResponse.getPrice();
                } else if (tripResponse.getPrice().equals(mMin)) {
                    lowestPrices.add(tripResponse);
                }
            };
        }

        @Override
        public BinaryOperator<List<TripResponse>> combiner() {
            return (one, another) -> {
                one.addAll(another);
                return one;
            };
        }

        @Override
        public Function<List<TripResponse>, Flux<TripResponse>> finisher() {
            return Flux::fromIterable;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }

    /**
     * Print the cheapest flights via a one-pass algorithm and a
     * custom Collector.
     */
    private static void printCheapestFlightsOnepass(Flux<TripResponse> flights) {
        Flux<TripResponse> lowestPrices = flights
            // Converts a stream of TripResponses into a Flux that
            // emits the cheapest priced trips(s).
            .collect(new CheapestPriceCollector())

            // Convert the Mono into a Flux that emits the cheapest
            // priced trip(s).
            .flatMapMany(Function.identity());

        // Print the cheapest flights.
        lowestPrices
            .subscribe(System.out::println);
    }
}
