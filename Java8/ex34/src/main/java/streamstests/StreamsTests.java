package streamstests;

import datamodels.Flight;
import utils.CheapestPriceCollector;
import utils.ExchangeRate;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This Java utility class provides three algorithms find the cheapest
 * flight(s) from a {@link List} of available flights using the Java
 * Streams framework.
 */
public class StreamTests {
    /**
     * A Java utility class should have a private constructor.
     */
    private StreamTests() {}

    /**
     * Print the cheapest flights via a two-pass algorithm that uses
     * {@code min()} and {@code filter()}.
     *
     * @param flightList The {@link List} of all available flights
     * @param currency The currency to convert to
     */
    public static Void printCheapestFlightsMin(List<Flight> flightList,
                                               String currency) {
        // Create a List of flights that match sTrip. 
        List<Flight> flights = StreamTests
            // Convert the flights into the requested currency.
            .convertFlightPrices(flightList, currency)

            // Collect into a List.
            .collect(toList());

        Optional<Flight> min1 = flights
                // Convert the List into a Stream.
                .stream()

                // Find the cheapest flight (returns an Optional).
                .min(Comparator.comparing(Flight::getPrice));

        min1
            // If there's a cheapest flight then find all the cheapest
            // flights (returns an Optional).
            .map(min -> flights
                 // Convert the List into a Stream (again).
                 .stream()

                 // Only allow flights matching the cheapest.
                 .filter(flight ->
                         flight.getPrice().equals(min.getPrice())))

            // If there are any cheapest flights then print them.
            .ifPresent(s -> s
                       // Print the cheapest flights.
                       .forEach(flight ->
                                System.out.println("printCheapestFlightsMin() = " 
                                                   + flight)));

        return null;
    }

    /**
     * Print the cheapest flights via a two-pass algorithm that first
     * calls {@code sort()}{ to order the trips by price and then uses
     * {@code takeWhile()} to return the cheapest flight(s).
     *
     * @param flightList The {@link List} of all available flights
     * @param currency The currency to convert to
     */
    public static Void printCheapestFlightsSorted(List<Flight> flightList,
                                                  String currency) {
        // Create a sorted List of flights that match sTrip. 
        List<Flight> sortedFlights = StreamTests
            // Convert the flights into the requested currency.
            .convertFlightPrices(flightList, currency)

            // Sort the flights from lowest to highest price.
            .sorted(Comparator.comparing(Flight::getPrice))

            // Collect into a List.
            .collect(toList());

        // If there's at least one flight then continue.
        if (sortedFlights.size() > 0) {
            // Store the cheapest price.
            Flight cheapest = sortedFlights.get(0);

            sortedFlights
                // Converts the List into a Stream.
                .stream()

                // Take all elements matching the cheapest price.
                .takeWhile(flight ->
                           flight.getPrice().equals(cheapest.getPrice()))

                // Print the cheapest flights.
                .forEach(flight ->
                         System.out.println("printCheapestFlightsSorted() = " + flight));
        }

        return null;
    }

    /**
     * Print the cheapest flights via a one-pass algorithm and a
     * custom Java Streams {@link Collector}.
     *
     * @param flightList The {@link List} of all available flights
     * @param currency The currency to convert to
     */
    public static Void printCheapestFlightsOnepass(List<Flight> flightList,
                                                   String currency) {
        StreamTests
            // Convert the flights into the requested currency.
            .convertFlightPrices(flightList, currency)

            // Convert the Stream of TripResponses into a List that
            // emits the cheapest priced trips(s).
            .collect(CheapestPriceCollector.toList())

            // Print the cheapest flights.
            .forEach(flight ->
                     System.out.println("printCheapestFlightsOnepass() = " 
                                        + flight));

        return null;
    }

    /**
     * Convert the flight prices according to the relevant exchange
     * rate.
     *
     * @param toCurrency The 3 letter currency to convert to
     * @return A {@link Stream} that emits flights with the correct
     *         prices
     */
    private static Stream<Flight> convertFlightPrices(List<Flight> flightList,
                                                      String toCurrency) {
        // Get the exchange rates.
        ExchangeRate exchangeRates = new ExchangeRate();

        // Return a stream containing all flights with converted
        // prices.
        return flightList
            // Convert the List into a Stream.
            .stream()

            // Convert flight prices via the currency rates.
            .map(flight ->
                 // Convert a flight price via the currency rates.
                 convertCurrency(toCurrency,
                                 flight,
                                 exchangeRates));
    }

    /**
     * Convert from {@code flight.getCurrency()} to {@code toCurrency}
     * and return an updated {@code flight}.
     *
     * @param toCurrency The currency to convert to
     * @param flight A {@link Flight} containing the price in the
     *               {@code flight.getCurrency()} format
     * @param rates The exchange rates
     * @return An updated {@link Flight} whose price reflects the
     *         exchange rate conversion
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
