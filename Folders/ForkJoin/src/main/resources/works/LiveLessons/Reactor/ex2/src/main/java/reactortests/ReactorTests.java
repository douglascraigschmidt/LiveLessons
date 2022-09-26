package reactortests;

import datamodels.Flight;
import reactor.core.publisher.Flux;
import reactor.math.MathFlux;
import utils.CheapestPriceCollectorFlux;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * This Java utility class provides three algorithms find the cheapest
 * flight(s) from a {@link List} of available flights using Project
 * Reactor reactive types.
 */
public class ReactorTests {
    /**
     * A Java utility class should have a private constructor.
     */
    private ReactorTests() {
    }

    /**
     * Print the cheapest flights via a two pass algorithm that uses
     * {@code MathFlux.min()} and {@code Flux.filter().
     *
     * @param flightList The {@link List} of all available flights
     * @param testName   The algorithm that computed the flight results
     * @param currency   The currency to convert to
     * @return A {@link Flux} that emits the cheapest {@link Flight} objects
     */
    public static Flux<Flight> findCheapestFlightsMin(List<Flight> flightList,
                                                      String testName,
                                                      String currency) {
        Flux<Flight> flights = ReactorTests
                // Convert the flights into the requested currency.
                .convertFlightPrices(flightList, currency);

        // Find the cheapest flights.
        return MathFlux
                // Find the cheapest flight (returns a Mono).
                .min(flights,
                        Comparator.comparing(Flight::getPrice))

                // Converts the Mono into a Flux that contains the
                // cheapest flight(s).
                .flatMapMany(min -> flights
                        // Only allow flights matching the cheapest.
                        .filter(tr -> tr.getPrice() == min.getPrice()));
    }

    /**
     * Print the cheapest flights via a two-pass algorithm that first
     * calls {@code sort()} to order the trips by price and then uses
     * {@code takeWhile()} to return the cheapest flight(s).
     *
     * @param flightList The {@link List} of all available flights
     * @param testName   The algorithm that computed the flight results
     * @param currency   The currency to convert to
     * @return A {@link Flux} that emits the cheapest {@link Flight} objects
     */
    public static Flux<Flight> findCheapestFlightsSorted(List<Flight> flightList,
                                                         String testName,
                                                         String currency) {
        Flux<Flight> sortedFlights = ReactorTests
                // Convert the flights into the requested currency.
                .convertFlightPrices(flightList, currency)

                // Sort the flights from lowest to highest price.
                .sort(Comparator.comparing(Flight::getPrice));

        return sortedFlights
                // Get the cheapest price (i.e., the first item in the
                // sorted Flux stream).
                .take(1)

                // Converts the Mono into a Flux that contains the
                // cheapest flight(s).
                .flatMap(min -> sortedFlights
                        // Take all elements matching the cheapest price, which
                        // is efficient since the Flux is sorted!
                        .takeWhile(tr -> tr.getPrice() == min.getPrice()));
    }

    /**
     * Print the cheapest flights via a one-pass algorithm and a
     * custom Java Streams Collector.
     *
     * @param flightList The {@link List} of all available flights
     * @param testName   The algorithm that computed the flight results
     * @param currency   The currency to convert to
     * @return A {@link Flux} that emits the cheapest {@link Flight} objects
     */
    public static Flux<Flight> findCheapestFlightsOnePass(List<Flight> flightList,
                                                          String testName,
                                                          String currency) {
        return ReactorTests
                // Convert the flights into the requested currency.
                .convertFlightPrices(flightList, currency)

                // Converts a stream of TripResponses into a Flux that
                // emits the cheapest priced trips(s).
                .collect(CheapestPriceCollectorFlux.toFlux())

                // Convert the Mono into a Flux that emits the cheapest
                // priced trip(s).
                .flatMapMany(Function.identity());
    }

    /**
     * Convert the flight prices according to the relevant exchange
     * rate.
     *
     * @param flightList The {@link List} of all available flights
     * @param currency   The 3 letter currency to convert to
     * @return A {@link Flux} that emits flights with the converted prices
     */
    private static Flux<Flight> convertFlightPrices(List<Flight> flightList,
                                                    String currency) {
        // Asynchronously get all the flights.
        return Flux
                // Convert the List into a Flux stream.
                .fromIterable(flightList)

                // Return a Flux that emits flights with the converted prices.
                .map(flight -> flight.inCurrency(currency))

                // Turn this Flux into a hot source and cache last emitted
                // signals for further Subscribers.
                .cache();
    }
}
