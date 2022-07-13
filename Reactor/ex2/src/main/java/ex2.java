import datamodels.Flight;
import datamodels.TripRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactortests.ReactorTests;
import streamtests.StreamsTests;
import utils.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiFunction;

/**
 * This example demonstrates various reactive algorithms for finding
 * all the minimum values in an unordered list, which is surprisingly
 * not well documented in the programming literature.  Three
 * algorithms use Project Reactor features to return {@link Flux} and
 * {@link Mono} reactive types that emit the cheapest flight(s) from a
 * Flux of available flights.  Another three algorithms use Java
 * Streams features to perform the same computations.
 */
public class ex2 {
    /**
     * This functional interface is used to represent the signature of
     * the Project Reactor min-finding algorithms.
     */
    @FunctionalInterface
    public interface TriFunction<T1, T2, T3, R> {
        R apply(T1 t1, T2 t2, T3 t3);
    }

    /**
     * The {@link TripRequest} used for these tests.
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
        System.out.println("Searching for best price flights for "
                           + sTrip.toString());

        // A List all the available flights.
        var flightList = FlightFactory
            // Get all the flights.
            .findFlights(sTrip);

        // Print the cheapest flights via a two-pass algorithm that
        // first calls sort() to order the trips by price and then
        // uses takeWhile() to return the cheapest flight(s).
        AsyncTaskBarrier
            .register(() -> ex2
                      .runTest(flightList,
                               ReactorTests::printCheapestFlightsSorted,
                               "ReactorTests::printCheapestFlightsSorted",
                               sTrip.getCurrency()));

        // Print the cheapest flights via a two-pass algorithm that
        // first calls sort() to order the trips by price and then
        // uses takeWhile() to return the cheapest flight(s).
        AsyncTaskBarrier
            .register(() ->
                      runTest(flightList,
                              StreamsTests::printCheapestFlightsSorted,
                              "StreamTests::printCheapestFlightsSorted",
                              sTrip.getCurrency()));

        // Print the cheapest flights via a two pass algorithm that
        // uses min() and filter().
        AsyncTaskBarrier
            .register(() -> ex2
                      .runTest(flightList,
                               ReactorTests::printCheapestFlightsMin,
                               "ReactorTests::printCheapestFlightsMin",
                               sTrip.getCurrency()));

        // Print the cheapest flights via a two-pass algorithm that
        // uses min() and filter().
        AsyncTaskBarrier
            .register(() ->
                      runTest(flightList,
                              StreamsTests::printCheapestFlightsMin,
                              "StreamTests::printCheapestFlightsMin",
                              sTrip.getCurrency()));

        // Print the cheapest flights via a one-pass algorithm and a
        // custom Java Streams Collector.
        AsyncTaskBarrier
            .register(() -> ex2
                      .runTest(flightList,
                               ReactorTests::printCheapestFlightsOnepass,
                               "ReactorTests::printCheapestFlightsOnepass",
                               sTrip.getCurrency()));


        // Print the cheapest flights via a one-pass algorithm and a
        // custom Java Streams Collector.
        AsyncTaskBarrier
            .register(() ->
                      runTest(flightList,
                              StreamsTests::printCheapestFlightsOnepass,
                              "StreamTests::printCheapestFlightsOnepass",
                              sTrip.getCurrency()));

        var testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running.
            .block();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
        System.out.println(AsyncRunTimer.getTimingResults());
    }

    /**
     * Run the Reactor test named {@code testName} by applying the
     * {@code findMinFlights} algorithm.
     *
     * @param flightList The {@link List} of flights used as input
     *                   to the {@code findMinFlights} algorithm
     * @param findMinFlights The algorithm used to find the lowest
     *                       priced flights
     * @param testName The name of the method that implements the
     *                 algorithm
     * @param currency The currency to convert into
     * @return A {@link Mono<Void>} that synchronizes with the {@link
     *         AsyncTaskBarrier}.
     */
    private static Mono<Void> runTest
        (List<Flight> flightList,
         TriFunction<List<Flight>, String, String, Mono<Void>> findMinFlights,
         String testName,
         String currency) {
        // Force the system to garbage collect.
        System.gc();

        // Make a deep copy of the flight list.
        var flights = ListAndArrayUtils
            .deepCopy(flightList, Flight::new);

        return AsyncRunTimer
            // Start timing the test.
            .startTimeRun(() -> findMinFlights
                          // Run the test.
                          .apply(flights,
                                 testName,
                                 currency),
                          testName);
    }

    /**
     * Run the Streams test named {@code testName} by applying the
     * {@code findMinFlights} algorithm.
     *
     * @param flightList The {@link List} of flights used as input
     *                   to the {@code findMinFlights} algorithm
     * @param findMinFlights The algorithm used to find the lowest
     *                       priced flights
     * @param testName The name of the method that implements the
     *                 algorithm
     * @param currency The currency to convert into
     * @return A {@link Mono<Void>} that synchronizes with the {@link
     *         AsyncTaskBarrier}.
     */
    private static Mono<Void> runTest
        (List<Flight> flightList,
         BiFunction<List<Flight>, String, Void> findMinFlights,
         String testName,
         String currency) {
        // Force the system to garbage collect.
        System.gc();

        // Make a deep copy of the flight list.
        var flights = ListAndArrayUtils
            .deepCopy(flightList, Flight::new);

        AsyncRunTimer
            // Start timing the test.
            .startTimeRun(() -> {
                    var result = findMinFlights
                        // Run the test.
                        .apply(flights,
                               currency);

                    // Stop the timer for this test.
                    AsyncRunTimer.stopTimeRun(testName);
                    return result;
                },
                testName);

        // Synchronize with the AsyncTaskBarrier.
        return Mono.empty();
    }
}
