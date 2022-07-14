package reactortests;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;

import datamodels.Flight;
import utils.AsyncRunTimer;
import utils.AsyncTaskBarrier;
import utils.CheapestPriceCollectorFlux;
import utils.ExchangeRate;

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
    private ReactorTests() {}

    /**
     * Convert the flight prices according to the relevant exchange
     * rate.
     *
     * @param flightList The {@link List} of all available flights
     * @param toCurrency The 3 letter currency to convert to
     * @return A {@link Flux} that emits flights with the converted prices
     */
    private static Flux<Flight> convertFlightPrices(List<Flight> flightList,
                                                    String toCurrency) {
        // Asynchronous get all the exchange rates.
        Mono<ExchangeRate> exchangeRates = Mono
            .fromCallable(ExchangeRate::new);

        // Asynchronously get all the flights.
        Flux<Flight> flights = Flux
            // Convert the List into a Flux stream.
            .fromIterable(flightList);

        // Return a Flux that emits flights with the converted prices.
        return exchangeRates
            // Wait for both the flights and the exchange rates to
            // complete initialization and the convert prices.
            .flatMapMany(rates -> flights
                         .map(flight ->
                              // Convert flight prices via the
                              // currency rates.
                              convertCurrency(toCurrency,
                                              flight,
                                              rates)))

            // Turn this Flux into a hot source and cache last emitted
            // signals for further Subscribers.
            .cache();
    }

    /**
     * Print the cheapest flights via a two pass algorithm that uses
     * min() and filter().
     *
     * @param flightList The {@link List} of all available flights
     * @param testName The algorithm that computed the flight results
     * @param currency The currency to convert to
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
                         .filter(tr -> tr.getPrice().equals(min.getPrice())));
    }

    /**
     * Print the cheapest flights via a two-pass algorithm that first
     * calls sort() to order the trips by price and then uses
     * takeWhile() to return the cheapest flight(s).
     *
     * @param flightList The {@link List} of all available flights
     * @param testName The algorithm that computed the flight results
     * @param currency The currency to convert to
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
                     .takeWhile(tr -> tr
                                .getPrice()
                                .equals(min.getPrice())));
    }

    /**
     * Print the cheapest flights via a one-pass algorithm and a
     * custom Java Streams Collector.
     *
     * @param flightList The {@link List} of all available flights
     * @param testName The algorithm that computed the flight results
     * @param currency The currency to convert to
     * @return A {@link Flux} that emits the cheapest {@link Flight} objects
     */
    public static Flux<Flight> findCheapestFlightsOnepass(List<Flight> flightList,
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
     * Convert from {@code flight.getCurrency()} to {@code toCurrency}
     * and return an updated {@code flight}.
     *
     * @param toCurrency Currency to convert to
     * @param flight {@link Flight}  containing the price in the {@code
     *        flight.getCurrency()} format
     * @param rates The exchange rates
     * @return An updated {@link Flight} whose price reflects the exchange
     *         rate conversion
     */
    private static Flight convertCurrency(String toCurrency,
                                          Flight flight,
                                          ExchangeRate rates) {
        // Only convert the currency if necessary.
        if (!flight.getCurrency().equals(toCurrency)) {
            // Update the price by multiplying it by the currency
            // conversion rate.
            flight.setPrice((int) (flight.getPrice() * rates
                                   .getRates(flight.getCurrency())
                                   .get(toCurrency)));

            // Update the currency.
            flight.setCurrency(toCurrency);
        }

        // Return the flight (which may or may not be updated).
        return flight;
    }
}
