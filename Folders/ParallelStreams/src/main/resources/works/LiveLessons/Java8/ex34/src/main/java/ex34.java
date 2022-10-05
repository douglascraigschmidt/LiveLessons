import datamodels.Flight;
import datamodels.TripRequest;
import streamstests.StreamsTests;
import utils.AsyncTaskBarrier;
import utils.FlightFactory;
import utils.ListAndArrayUtils;
import utils.RunTimer;

import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.function.BiFunction;

import static utils.ExceptionUtils.rethrowSupplier;

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
     * A {@link List} of all the find-min algorithms and
     * their associated metadata.
     */
    private static final List
        <SimpleEntry<BiFunction<List<Flight>, String, List<Flight>>, String>>
        sAlgorithmsMap = new ArrayList<>() { {
            // Print the cheapest flights via a two-pass algorithm
            // that uses min() and filter().
            add(new SimpleEntry<>
                (StreamsTests::findCheapestFlightsMin,
                 "StreamsTests::findCheapestFlightsMin"));
        } {
            // Print the cheapest flights via a two-pass algorithm
            // that first calls sort() to order the trips by price and
            // then uses takeWhile() to return the cheapest flight(s).
            add(new SimpleEntry<>
                (StreamsTests::findCheapestFlightsSorted,
                 "StreamsTests::findCheapestFlightsSorted"));
        } {
            // Print the cheapest flights via a one-pass algorithm and
            // a custom Java Streams Collector.
            add(new SimpleEntry<>
                (StreamsTests::findCheapestFlightsOnepass,
                 "StreamsTests::findCheapestFlightsOnepass"));
        } };

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

        // Create an entry barrier to ensure all
        // algorithms start at the same time.
        CyclicBarrier entryBarrier =
            new CyclicBarrier(sAlgorithmsMap.size());
        
        sAlgorithmsMap
            // Register all the find-min algorithms.
            .forEach(entry -> AsyncTaskBarrier
                     .register(() -> ex34
                               // Run the algorithm.
                               .runTest(entryBarrier,
                                        flightList,
                                        entry.getKey(),
                                        entry.getValue(),
                                        sTrip.getCurrency())));

        int algorithmCount = AsyncTaskBarrier
            // Run all the algorithms.
            .runTasks()

            // Block until all the algorithms are done to allow future
            // computations to complete running.
            .join();

        // Print the results.
        System.out.println("Completed " + algorithmCount + " algorithms");
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Run the Streams algorithm named {@code algorithmName} by
     * applying the {@code findMinFlights} algorithm.
     *
     * @param flightList The {@link List} of flights used as input
     *                   to the {@code findMinFlights} algorithm
     * @param findMinFlights The algorithm used to find the lowest
     *                       priced flights
     * @param algorithmName The name of the method that implements the
     *                 algorithm
     * @param currency The currency to convert into
     */
    private static CompletableFuture<Void> runTest
        (CyclicBarrier entryBarrier,
         List<Flight> flightList,
         BiFunction<List<Flight>, String, List<Flight>> findMinFlights,
         String algorithmName,
         String currency) {
        // Force the system to garbage collect first.
        System.gc();

        // Deep copy flight list.
        var flights = ListAndArrayUtils
            .deepCopy(flightList, Flight::new);

        return CompletableFuture
            .supplyAsync(() -> {
                    // Wait for all the other tasks to reach the entry
                    // barrier before proceeding.
                    rethrowSupplier(entryBarrier::await).get();

                    var cheapestFlights = RunTimer
                        // Start timing the test.
                        .timeRun(() -> findMinFlights
                                 // Run the test.
                                 .apply(flights, currency),
                                 algorithmName);

                       // Print the cheapest flights.
                    cheapestFlights
                        .forEach(flight -> ex34
                                 .printResults("algorithmName = "
                                          + flight));
                    return null;
                });
    }

    /**
     * Display the {@code results} by prepending the current thread
     * id.
     *
     * @param results The string to display
     */
    private static void printResults(String results) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + results);
    }
}
