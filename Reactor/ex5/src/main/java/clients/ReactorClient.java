package clients;

import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.apigateway.APIGatewayProxyAsync;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.AsyncTaskBarrier;
import utils.Options;

import java.util.function.Function;

/**
 * A Java utility class containing a client that uses Project Reactor
 * and WebFlux to asynchronously invoke microservices that provide
 * various flight-related services for the Airline Booking App (ABA).
 */
public class ReactorClient {
    /**
     * A proxy that's used to communicate with the APIGateway
     * microservice asynchronously.
     */
    private static final APIGatewayProxyAsync sAPIGatewayProxyAsync =
            new APIGatewayProxyAsync();

    /**
     * This test invokes microservices to asynchronously determine the
     * best price for a {@code trip} using the given {@code
     * currencyConversion}.
     */
    public static void runAsyncTests(TripRequest trip,
                                     CurrencyConversion currencyConversion) {
        System.out.println("begin runAsyncTests()");

        // Iterate multiple times.
        for (int i = 0; i < Options.instance().maxIterations(); i++) {
            int iteration = i + 1;

            // Register tests with the AsyncTaskBarrier framework so
            // they will run asynchronously wrt the other iterations.
            AsyncTaskBarrier
                .register(() -> findBestPriceAsync(iteration,
                          trip,
                          currencyConversion));

            AsyncTaskBarrier
                .register(() -> findFlightsAsync(iteration,
                                                 trip,
                                                 currencyConversion));
        }

        AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow all the
            // computations to complete running asynchronously.
            .block();

        System.out.println("end runAsyncTests()");
    }

    /**
     * Returns the best price for {@code tripRequest} using the given {@code
     * currencyConversion} via asynchronous computations.
     *
     * @param iteration Current iteration count
     * @param tripRequest The current trip being priced
     * @param currencyConversion The currency to convert from and to
     * @return An empty Mono to synchronize with the AsyncTaskBarrier framework.
     */
    private static Mono<Void> findBestPriceAsync(int iteration,
                                                 TripRequest tripRequest,
                                                 CurrencyConversion currencyConversion) {
        // The behavior to perform if an exception occurs.
        Function<? super Throwable,
            ? extends Mono<? extends TripResponse>> handleEx = ex -> {
            Options.print("Iteration #"
                          + iteration
                          + " The exception thrown was " + ex.toString());
            return Mono.just(new TripResponse());
        };

        return sAPIGatewayProxyAsync
            // Asynchronously find the best price for the tripRequest.
            .findBestPrice(Schedulers.parallel(),
                           tripRequest,
                           currencyConversion)

            // Print the price if the call completed within
            // sMAX_TIME seconds.
            .doOnSuccess(tripResponse ->
                         Options.print("Iteration #"
                                       + iteration
                                       + " The best price is: "
                                       + tripResponse.getPrice()
                                       + " GBP on "
                                       + tripResponse.getAirlineCode()))
                    
            // Consume and print the TimeoutException if the call took
            // longer than sMAX_TIME.
            .onErrorResume(handleEx)

            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Returns all flights for {@code tripRequest} using the given
     * {@code currencyConversion} via asynchronous
     * computations/communications.
     *
     * @param iteration Current iteration count
     * @param tripRequest The current desired trip
     * @param currencyConversion The currency to convert from and to
     * @return An empty Mono to synchronize with the AsyncTaskBarrier framework.
     */
    private static Mono<Void> findFlightsAsync(int iteration,
                                               TripRequest tripRequest,
                                               CurrencyConversion currencyConversion) {
        // The behavior to perform if an exception occurs.
        Function<? super Throwable,
            ? extends Mono<? extends TripResponse>> handleEx = ex -> {
            Options.print("Iteration #"
                          + iteration
                          + " The exception thrown was " + ex.toString());
            return Mono.just(new TripResponse());
        };

        return sAPIGatewayProxyAsync
            // Asynchronously find all the flights in the tripRequest.
            .findFlights(Schedulers.parallel(),
                         tripRequest,
                         currencyConversion)

            // Print the price if the call completed within
            // sMAX_TIME seconds.
            .doOnNext(tripResponse -> Options
                      .print("Iteration #"
                             + iteration
                             + " The price is: "
                             + tripResponse.getPrice()
                             + " "
                             + currencyConversion.getTo() 
                             + " on "
                             + tripResponse.getAirlineCode()))
                    
            // Consume and print the TimeoutException if the call took
            // longer than sMAX_TIME.
            .onErrorResume(handleEx)

            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }
}
