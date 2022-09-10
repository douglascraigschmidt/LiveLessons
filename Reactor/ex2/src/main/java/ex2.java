import datamodels.Flight;
import datamodels.FlightRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactortests.ReactorTests;
import streamstests.StreamsTests;
import utils.Options;
import utils.RunTimer;
import utils.AsyncTaskBarrier;
import utils.FlightFactory;

import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

import static utils.ExceptionUtils.rethrowSupplier;

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

    // Configure the FlightFactory to generate a set of random flights.
    private final List<Flight> mFlights =
            new FlightFactory
                    .Builder()
                    .minFlights(5_000_000)
                    .maxFlights(5_000_000)
                    .minLowestPriceMatches(2)
                    .maxLowestPriceMatches(4)
                    .fromAirport("NYC")
                    .toAirport("FCO")
                    .from(LocalDate.now().plusDays(1))
                    .generateFlights(false);

    // Generate a random FlightRequest from the list of random
    // flights.
    private final FlightRequest mFlightRequest =
            FlightFactory.buildFlightRequestFrom(mFlights)
                    .withCurrency("USD");
    /**
     * Keep track of the invocation count.
     */
    private final AtomicLong mInvocationCount = new AtomicLong();

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv) {
        Options.instance().parseArgs(argv);
        new ex2().runTests();
    }

    /**
     * A {@link List} of all the Streams find-min algorithms and their
     * associated metadata.
     */
    private final List
        <SimpleEntry<BiFunction<List<Flight>, String, List<Flight>>, String>>
        streamsAlgorithmsMap = new ArrayList<>() {
                {
                    // Print the cheapest flights via a two-pass algorithm
                    // that uses min() and filter().
                    add(new SimpleEntry<>
                        (StreamsTests::findCheapestFlightsMin,
                         "StreamsTests::findCheapestFlightsMin"));
                }

                {
                    // Print the cheapest flights via a two-pass algorithm
                    // that first calls sort() to order the trips by price and
                    // then uses takeWhile() to return the cheapest flight(s).
                    add(new SimpleEntry<>
                        (StreamsTests::findCheapestFlightsSorted,
                         "StreamsTests::findCheapestFlightsSorted"));
                }

                {
                    // Print the cheapest flights via a one-pass algorithm and
                    // a custom Java Streams Collector.
                    add(new SimpleEntry<>
                        (StreamsTests::findCheapestFlightsOnePass,
                         "StreamsTests::findCheapestFlightsOnePass"));
                }
            };

    /**
     * A {@link List} of all the Reactor find-min algorithms and their
     * associated metadata.
     */
    private final List
        <SimpleEntry<TriFunction<List<Flight>, String, String, Flux<Flight>>, String>>
        sReactorAlgorithmsMap = new ArrayList<>() {
                {
                    // Print the cheapest flights via a two-pass algorithm
                    // that uses min() and filter().
                    add(new SimpleEntry<>
                        (ReactorTests::findCheapestFlightsMin,
                         "ReactorTests::findCheapestFlightsMin"));
                }

                {
                    // Print the cheapest flights via a two-pass algorithm
                    // that first calls sort() to order the trips by price and
                    // then uses takeWhile() to return the cheapest flight(s).
                    add(new SimpleEntry<>
                        (ReactorTests::findCheapestFlightsSorted,
                         "ReactorTests::findCheapestFlightsSorted"));
                }

                {
                    // Print the cheapest flights via a one-pass algorithm and
                    // a custom Java Streams Collector.
                    add(new SimpleEntry<>
                        (ReactorTests::findCheapestFlightsOnePass,
                         "ReactorTests::findCheapestFlightsOnePass"));
                }
            };

    private void runTests() {
        Options.debug("Searching among "
                      + mFlights.size()
                      + " flights for best price flights matching "
                      + mFlightRequest
                      + "\n");

        // Create an entry barrier to ensure all algorithms start at
        // the same time.
        CyclicBarrier entryBarrier =
            new CyclicBarrier(streamsAlgorithmsMap.size()
                              + sReactorAlgorithmsMap.size());

        streamsAlgorithmsMap
            // Register all the Streams find-min algorithms.
            .forEach(entry -> AsyncTaskBarrier
                     .register(() ->
                               // Run the algorithm.
                               runAlgorithm(entryBarrier,
                                       mFlights,
                                            entry.getKey(),
                                            entry.getValue(),
                                            mFlightRequest.getCurrency())));

        sReactorAlgorithmsMap
            // Register all the Reactor find-min algorithms.
            .forEach(entry -> AsyncTaskBarrier
                     .register(() ->
                               // Run the algorithm.
                               runAlgorithm(entryBarrier,
                                       mFlights,
                                            entry.getKey(),
                                            entry.getValue(),
                                            mFlightRequest.getCurrency())));

        var testCount = AsyncTaskBarrier
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
     * Run the Reactor test named {@code methodName} by applying the
     * {@code findMinFlights} algorithm.
     *
     * @param flights        The {@link List} of flights used as input
     *                       to the {@code findMinFlights} algorithm
     * @param findMinFlights The algorithm used to find the lowest
     *                       priced flights
     * @param methodName  The name of the method that implements the
     *                       algorithm
     * @param currency       The currency to convert into
     * @return A {@link Mono<Void>} that synchronizes with the {@link
     * AsyncTaskBarrier}.
     */
    private Mono<Void> runAlgorithm
        (CyclicBarrier entryBarrier,
         List<Flight> flights,
         TriFunction<List<Flight>, String, String, Flux<Flight>> findMinFlights,
         String methodName,
         String currency) {
        // Force the system to garbage collect.
        System.gc();

        // Get a unique invocation identifier.
        long invocationId = mInvocationCount.getAndIncrement();

        return Mono
            // Create a Mono from a callable lambda.
            .fromCallable(() -> {
                    var cheapestFlights = RunTimer
                        // Start timing the test.
                        .startTimeRun(() -> {
                                // Wait for all the other tasks to reach the entry
                                // barrier before proceeding.
                                rethrowSupplier(entryBarrier::await).get();

                                return findMinFlights
                                    // Run the test.
                                    .apply(flights,
                                           methodName,
                                           currency);
                            },
                            methodName,
                            invocationId);

                    // Print the cheapest flights.
                    return printResults(cheapestFlights,
                                        methodName,
                                        invocationId);
                })
            // Run the algorithm in the parallel thread pool.
            .subscribeOn(Schedulers.parallel())

            // Return a Mono<Void> to synchronize with AsyncTaskBarrier.
            .flatMap(Function.identity());
    }

    /**
     * Print the results from the {@code methodName}.
     *
     * @param lowestPrices  A {@link Flux} containing the lowest priced flights
     * @param methodName The algorithm that computed the flight results
     * @return A {@link Mono<Void>} that synchronizes with the {@link
     *         AsyncTaskBarrier}.
     */
    private Mono<Void> printResults(Flux<Flight> lowestPrices,
                                    String methodName,
                                    long invocationId) {
        return lowestPrices
            // Print the cheapest flights.
            .doOnNext(flight -> printResults(methodName + " = " + flight))

            // Record the time for this run.
            .doFinally(___ -> RunTimer
                       .stopTimeRun(methodName, invocationId))

            // Sync with the AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Run the Java Streams algorithm named {@code methodName} by
     * applying the {@code findMinFlights} algorithm.
     *
     * @param flights        The {@link List} of flights used as input
     *                       to the {@code findMinFlights} algorithm
     * @param findMinFlights The algorithm used to find the lowest
     *                       priced flights
     * @param methodName  The name of the method that implements the
     *                       algorithm
     * @param currency       The currency to convert into
     * @return A {@link Mono<Void>} that synchronizes with the {@link
     *         AsyncTaskBarrier}.
     */
    private Mono<Void> runAlgorithm
        (CyclicBarrier entryBarrier,
         List<Flight> flights,
         BiFunction<List<Flight>, String, List<Flight>> findMinFlights,
         String methodName,
         String currency) {
        // Force the system to garbage collect first.
        System.gc();

        // Get a unique invocation number.
        long invocationId = mInvocationCount.getAndIncrement();

        return Mono
            // Create a Mono from a callable lambda.
            .fromCallable(() -> {
                    // Wait for all the other tasks to reach the entry
                    // barrier before proceeding.
                    rethrowSupplier(entryBarrier::await).get();

                    RunTimer
                        // Time the algorithm synchronously.
                        .timeRun(() -> {
                                var results = findMinFlights
                                    // Run the test.
                                    .apply(flights, currency);

                                // Print the cheapest flights.
                                results
                                    .forEach(flight ->
                                             printResults(methodName + " = " + flight));

                                // Return the results.
                                return results;
                            },
                            methodName,
                            invocationId);

                    // Synchronize with the AsyncTaskBarrier.
                    return Mono.empty();
                })
            // Run the algorithm in the parallel thread pool.
            .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()))

            // Return a Mono<Void> to synchronize with AsyncTaskBarrier.
            .then();
    }

    /**
     * Display the {@code results} by prepending the current thread
     * id.
     *
     * @param results The {@link String} to display
     */
    private void printResults(String results) {
        Options.debug("["
                      + Thread.currentThread().getId()
                      + "] "
                      + results);
    }
}
