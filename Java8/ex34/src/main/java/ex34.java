import datamodels.Flight;
import datamodels.TripRequest;
import streamtests.StreamTests;
import utils.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example demonstrates various functional algorithms for finding
 * all the minimum values in an unordered list, which is surprisingly
 * not well documented in the programming literature.  These three
 * algorithms use Java Streams to print the cheapest flight(s) from a
 * stream of available flights, which is part of an Flight Listing App
 * (FLApp) that we've created for our online course on Reactive
 * Microservices.
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
        System.out.println("Searching for best price flights for "
                           + sTrip.toString());

        // A List all the available flights.
        List<Flight> flightList = FlightFactory
            // Get all the flights.
            .findFlights(sTrip);
        
        // Print the cheapest flights via a two-pass algorithm that
        // uses min() and filter().
        CFTaskBarrier
            .register(() ->
                      runTest(flightList,
                              StreamTests::printCheapestFlightsMin,
                              "StreamTests::printCheapestFlightsMin",
                              sTrip.getCurrency()));

        // Print the cheapest flights via a two-pass algorithm that
        // first calls sort() to order the trips by price and then
        // uses takeWhile() to return the cheapest flight(s).
        CFTaskBarrier
            .register(() ->
                      runTest(flightList,
                              StreamTests::printCheapestFlightsSorted,
                              "StreamTests::printCheapestFlightsSorted",
                              sTrip.getCurrency()));

        // Print the cheapest flights via a one-pass algorithm and a
        // custom Java Streams Collector.
        CFTaskBarrier
            .register(() ->
                      runTest(flightList,
                              StreamTests::printCheapestFlightsOnepass,
                              "StreamTests::printCheapestFlightsOnepass",
                              sTrip.getCurrency()));

        int testCount = CFTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running.
            .join();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Run the test named {@code testName} by applying the {@code
     * findMinFlights} algorithm.
     *
     * @param flightList The {@link List} of flights used as input
     *                   to the {@code findMinFlights} algorithm
     * @param findMinFlights The algorithm used to find the lowest
     *                       priced flights
     * @param testName The name of the method that implements the
     *                 algorithm
     * @param currency The currency to convert into
     */
    private static CompletableFuture<Void> runTest
        (List<Flight> flightList,
         BiFunction<List<Flight>, String, Void> findMinFlights,
         String testName,
         String currency) {
        // Force the system to garbage collect first.
        System.gc();

        return CompletableFuture
            .supplyAsync(() -> RunTimer
                         // Start timing the test.
                         .timeRun(() -> findMinFlights
                                  // Run the test.
                                  .apply(ListAndArrayUtils
                                         // Deep copy flight list.
                                         .deepCopy(flightList, Flight::new),
                                         currency),
                                  testName));
    }
}
