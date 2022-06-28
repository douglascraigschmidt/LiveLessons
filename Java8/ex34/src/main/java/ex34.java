import datamodels.Flight;
import datamodels.TripRequest;
import utils.CheapestPriceCollector;
import utils.ExchangeRate;
import utils.TestDataFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example demonstrates various functional algorithms for finding
 * all the minimum values in an unordered list, which is surprisingly
 * not well documented in the programming literature.  These three
 * algorithms print the cheapest flight(s) from a Stream of available
 * flights, which is part of an Flight Listing App (FLApp) that we've
 * create for our online course on Scalable Microservices.
 */
public class ex34 {
    /**
     * The trip flight leg used for these tests.
     */
    private static final TripRequest sTrip = TripRequest
        .valueOf(LocalDateTime.parse("2025-01-01T07:00:00"),
                 LocalDateTime.parse("2025-02-01T19:00:00"),
                 "LHR",
                 "JFK",
                 "EUR",
                 1);

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv) {
        // Print the cheapest flights via a two-pass algorithm that
        // uses min() and filter().
        printCheapestFlightsMin();

        // Print the cheapest flights via a two-pass algorithm that
        // first calls sort() to order the trips by price and then
        // uses takeWhile() to return the cheapest flight(s).
        printCheapestFlightsSorted();

        // Print the cheapest flights via a one-pass algorithm and a
        // custom Java Streams Collector.
        printCheapestFlightsOnepass();
    }

    /**
     * Print the cheapest flights via a two-pass algorithm that uses
     * min() and filter().
     */
    private static void printCheapestFlightsMin() {
        // Create a List of flights that match sTrip. 
        List<Flight> flightList = ex34
            // Convert the flights into the requested currency.
            .convertFlightPrices(sTrip.getCurrency())

            // Collect into a List.
            .collect(toList());

        Optional<Flight> min1 = flightList
                // Convert the List into a Stream.
                .stream()

                // Find the cheapest flight (returns an Optional).
                .min(Comparator.comparing(Flight::getPrice));

        // If there's a cheapest flight then find all the cheapest
            // flights (returns an Optional).
        min1.map(min -> flightList
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
    }

    /**
     * Print the cheapest flights via a two-pass algorithm that first
     * calls sort() to order the trips by price and then uses
     * takeWhile() to return the cheapest flight(s).
     */
    private static void printCheapestFlightsSorted() {
        // Create a sorted List of flights that match sTrip. 
        List<Flight> sortedFlights = ex34
            // Convert the flights into the requested currency.
            .convertFlightPrices(sTrip.getCurrency())

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
    }

    /**
     * Print the cheapest flights via a one-pass algorithm and a
     * custom Java Streams Collector.
     */
    private static void printCheapestFlightsOnepass() {
        ex34
            // Convert the flights into the requested currency.
            .convertFlightPrices(sTrip.getCurrency())

            // Convert the Stream of TripResponses into a List that
            // emits the cheapest priced trips(s).
            .collect(CheapestPriceCollector.toList())

            // Print the cheapest flights.
            .forEach(flight ->
                     System.out.println("printCheapestFlightsOnepass() = " 
                                        + flight));
    }

    /**
     * Convert the flight prices according to the relevant exchange
     * rate.
     *
     * @param toCurrency The 3 letter currency to convert to
     * @return A {@link Stream} that emits flights with the correct
     * prices
     */
    private static Stream<Flight> convertFlightPrices(String toCurrency) {
        // Get the exchange rates.
        ExchangeRate exchangeRates = new ExchangeRate();

        // Return a stream containing all flights with converted
        // prices.
        return TestDataFactory
            // Get all the flights that match the given trip request.
            .findFlights(sTrip)

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
     * @param toCurrency Current to convert to
     * @param flight     Flight containing the price in the {@code
     *                   flight.getCurrency()} format
     * @param rates      The exchange rates
     * @return An updated flight whose price reflects the exchange
     * rate conversion
     */
    private static Flight convertCurrency(String toCurrency,
                                          Flight flight,
                                          ExchangeRate rates) {
        // Only convert the currency if necessary.
        if (!flight.getCurrency().equals(toCurrency)) {
            // Update the price by multiplying it by the currency
            // conversion rate.
            flight.setPrice(flight.getPrice()
                            * rates.getRates(flight.getCurrency()).get(toCurrency));
        }

        // Return the flight (which may or may not be updated).
        return flight;
    }
}
