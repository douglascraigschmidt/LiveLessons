import datamodels.Flight;
import datamodels.TripRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.math.MathFlux;
import utils.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * This example demonstrates various reactive algorithms for finding
 * all the minimum values in an unordered list, which is surprisingly
 * not well documented in the programming literature.  These three
 * algorithms use Project Reactor features to return Flux/Mono
 * reactive types that emit the cheapest flight(s) from a Flux of
 * available flights, which is part of an Airline Booking App we've
 * created an online course on Reactive Microservices.
 *
 * This example also shows how to use the AsyncTaskBarrier framework
 * and the flatMapMany() operator that converts flight prices to the
 * given currency after asynchronous operations complete.
 */
public class ex2 {
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
     * A {@link List} all the available flights.
     */
    private static final List<Flight> sFlightList = TestDataFactory
        // Get all the flights.
        .findFlights(sTrip);

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv) {
        System.out.println("Searching for best price flights for "
                           + sTrip.toString());

        // Print the cheapest flights via a two pass algorithm that
        // uses min() and filter().
        AsyncTaskBarrier
                .register(() -> ex2
                        .runTest(ex2::printCheapestFlightsMin,
                                "printCheapestFlightsMin"));

        // Print the cheapest flights via a one-pass algorithm and a
        // custom Java Streams Collector.
        AsyncTaskBarrier
            .register(() -> ex2
                      .runTest(ex2::printCheapestFlightsOnepass,
                               "printCheapestFlightsOnepass")); 

        // Print the cheapest flights via a two-pass algorithm that
        // first calls sort() to order the trips by price and then
        // uses takeWhile() to return the cheapest flight(s).
        AsyncTaskBarrier
            .register(() -> ex2
                      .runTest(ex2::printCheapestFlightsSorted,
                               "printCheapestFlightsSorted"));

        @SuppressWarnings("ConstantConditions")
            long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running.
            .block();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Run the test named {@code testName} by applying the {@code
     * downloadAndStoreImage} function.
     */
    private static Mono<Void> runTest(Function<List<Flight>, Mono<Void>> findMinFlights,
                                      String testName) {
        // Let the system garbage collect.
        System.gc();

        // Make a deep copy of the flight list.
        List<Flight> flightList = Flight.deepCopy(sFlightList);

        // System.out.println("flightList.size() = " + flightList.size());

        RunTimer
            // Record how long the test takes to run.
            .timeRun(() ->
                     // Run the test in blocking mode.
                     findMinFlights.apply(flightList).block(),
                     testName);
        return Mono.empty();
    }

    /**
     * Convert the flight prices according to the relevant exchange rate.
     *
     * @param toCurrency The 3 letter currency to convert to
     * @return A {@link Flux} that emits flights with the correct prices
     */
    private static Flux<Flight> convertFlightPrices(List<Flight> flightList,
                                                    String toCurrency) {
        // Asynchronous get all the exchange rates.
        Mono<ExchangeRate> exchangeRates = Mono
            .fromCallable(ExchangeRate::new)

            // Run this computation in the parallel thread pool.
            .subscribeOn(Schedulers.parallel());

        // Asynchronously get all the flights.
        Flux<Flight> flights = Flux
            .fromIterable(flightList)

            // Run this computation in the parallel thread pool.
            .subscribeOn(Schedulers.parallel());

        // Return a Flux that emits flights with the correct prices.
        return exchangeRates
            // Wait for both the flights and the exchange rates
            // to complete.
            .flatMapMany(rates -> flights
                         .map(flight ->
                              // Convert flight prices via the
                              // currency rates.
                              convertCurrency(toCurrency,
                                              flight,
                                              rates)))

            // Turn this Flux into a hot source and cache last
            // emitted signals for further Subscribers.
            .cache();
    }

    /**
     * Print the cheapest flights via a two pass algorithm that uses
     * min() and filter().
     */
    private static Mono<Void> printCheapestFlightsMin(List<Flight> flightList) {
        Flux<Flight> flights = ex2
            // Convert the flights into the requested currency.
            .convertFlightPrices(flightList, sTrip.getCurrency());

        // Find the cheapest flights.
        Flux<Flight> lowestPrices = MathFlux
            // Find the cheapest flight (returns a Mono).
            .min(flights,
                 Comparator.comparing(Flight::getPrice))

            // Converts the Mono into a Flux that contains the
            // cheapest flight(s).
            .flatMapMany(min -> flights
                         // Only allow flights matching the cheapest.
                         .filter(tr -> tr.getPrice().equals(min.getPrice())));

        return lowestPrices
            // Print the cheapest flights.
            .doOnNext(flight -> 
                      System.out.println("printCheapestFlightsMin() = " + flight))

            // Sync with the AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Print the cheapest flights via a two-pass algorithm that first
     * calls sort() to order the trips by price and then uses
     * takeWhile() to return the cheapest flight(s).
     */
    private static Mono<Void> printCheapestFlightsSorted(List<Flight> flightList) {
        Flux<Flight> sortedFlights = ex2
            // Convert the flights into the requested currency.
            .convertFlightPrices(flightList, sTrip.getCurrency())

            // Sort the flights from lowest to highest price.
            .sort(Comparator.comparing(Flight::getPrice));

        Flux<Flight> lowestPrices = sortedFlights
            // Get the cheapest price.
            .take(1)

            // Converts the Mono into a Flux that contains the
            // cheapest flight(s).
            .flatMap(min -> sortedFlights
                     // Take all elements matching the cheapest price.
                     .takeWhile(tr -> tr
                                .getPrice()
                                .equals(min.getPrice())));


        return lowestPrices
            // Print the cheapest flights.
            .doOnNext(flight ->
                      System.out.println("printCheapestFlightsSorted() = "
                                         + flight))

            // Sync with the AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Print the cheapest flights via a one-pass algorithm and a
     * custom Java Streams Collector.
     */
    private static Mono<Void> printCheapestFlightsOnepass(List<Flight> flightList) {
        Flux<Flight> lowestPrices = ex2
            // Convert the flights into the requested currency.
            .convertFlightPrices(flightList, sTrip.getCurrency())

            // Converts a stream of TripResponses into a Flux that
            // emits the cheapest priced trips(s).
            .collect(CheapestPriceCollector.toFlux())

            // Convert the Mono into a Flux that emits the cheapest
            // priced trip(s).
            .flatMapMany(Function.identity());

        return lowestPrices
            // Print the cheapest flights.
            .doOnNext(flight ->
                      System.out.println("printCheapestFlightsOnepass() = "
                                         + flight))

            // Sync with the AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Convert from {@code flight.getCurrency()} to {@code toCurrency}
     * and return an updated {@code flight}.
     *
     * @param toCurrency Current to convert to
     * @param flight Flight containing the price in the {@code
     *        flight.getCurrency()} format
     * @param rates The exchange rates
     * @return An updated flight whose price reflects the exchange
     *         rate conversion
     */
    private static Flight convertCurrency(String toCurrency,
                                          Flight flight,
                                          ExchangeRate rates) {
        // Only convert the currency if necessary.
        if (!flight.getCurrency().equals(toCurrency)) {
            // Update the price by multiplying it by the currency
            // conversion rate.
            flight.setPrice((int) (flight.getPrice()
                                   * rates.getRates(flight.getCurrency()).get(toCurrency)));
            // Update the currency.
            flight.setCurrency(toCurrency);
        }

        // Return the flight (which may or may not be updated).
        return flight;
    }
}
