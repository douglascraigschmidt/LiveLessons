package tests;

import datamodels.CurrencyConversion;
import datamodels.Trip;
import microservices.exchangeRate.ExchangeRateProxy;
import microservices.flightPrice.FlightPriceProxy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.AsyncTaskBarrier;
import utils.Options;

import java.time.Duration;
import java.util.function.Function;

public class ReactorTests {
    /**
     * A proxy that's used to communicate with the FlightPrice
     * microservice.
     */
    private static final FlightPriceProxy sFlightPriceProxy =
        new FlightPriceProxy();

    /**
     * A proxy that's used to communicate with the ExchangeRate
     * microservice.
     */
    private static final ExchangeRateProxy sExchangeRateProxy =
        new ExchangeRateProxy();

    /**
     * This test invokes microservices to asynchronously determine the
     * best price for a flight from London to NYC in British pounds.
     */
    public static void runAsyncMonos(Trip trip,
                                     CurrencyConversion currencyConversion) {
        System.out.println("begin runAsyncMonos()");

        // Iterate multiple times.
        for (int i = 0; i < Options.instance().maxIterations(); i++) {
            int iteration = i + 1;

            AsyncTaskBarrier
                // Register the test with the AsyncTaskBarrier framework so it
                // will run asynchronously wrt the other iterations.
                .register(() -> getBestPriceInPoundsAsync(iteration,
                                                          trip,
                                                          currencyConversion));
        }

        AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow all the
            // computations to complete running asynchronously.
            .block();

        System.out.println("end runAsyncMonos()");
    }

    /**
     * This test invokes microservices to synchronously determine the
     * best price for a flight from London to NYC in British pounds.
     */
    public static void runSyncMonos(Trip trip,
                                    CurrencyConversion currencyConversion) {
        System.out.println("begin runSyncMonos()");

        // Iterate multiple times.
        for (int i = 0; i < Options.instance().maxIterations(); i++)
            // Call the test synchronously.
            getBestPriceInPoundsSync(i + 1,
                                     trip,
                                     currencyConversion);

        System.out.println("end runSyncMonos()");
    }

    /**
     * Returns the best price for a flight from London to NYC in
     * British pounds via asynchronous computations.
     *
     * @param iteration Current iteration count
     * @param trip The current trip being priced
     * @param currencyConversion The currency to convert from and to
     * @return An empty Mono to synchronize with the AsyncTaskBarrier framework.
     */
    private static Mono<Void> getBestPriceInPoundsAsync(int iteration,
                                                        Trip trip,
                                                        CurrencyConversion currencyConversion) {
        Mono<Trip> tripM = sFlightPriceProxy
            // Asynchronously find the best price in US dollars
            // between London and New York city.
            .findBestPriceAsync(Schedulers.parallel(),
                                trip);

        Mono<Double> rateM = sExchangeRateProxy
            // Asynchronously determine exchange rate between US
            // dollars and British pounds.
            .queryExchangeRateForAsync(Schedulers.parallel(),
                                       currencyConversion,
                                       Mono.just(Options.instance().defaultRate()));

        // The behavior to perform if an exception occurs.
        Function<? super Throwable,
                    ? extends Mono<? extends Double>> handleEx = ex -> {
            Options.print("Iteration #"
                  + iteration
                  + " The exception thrown was " + ex.toString());
            return Mono.just(0.0);
        };

        // When tripM and rateM complete convert the price in US
        // dollars to the price in British pounds.  If these async
        // operations take more than {@code maxTime} then throw the
        // TimeoutException.
        return combineAndConvertResults(tripM, rateM, Options.instance().maxTimeout())
            // Print the price if the call completed within
            // sMAX_TIME seconds.
            .doOnSuccess(amount ->
                         Options.print("Iteration #"
                               + iteration
                               + " The price is: "
                               + amount
                               + " GBP"))
                    
            // Consume and print the TimeoutException if the call
            // took longer than sMAX_TIME.
            .onErrorResume(handleEx)

            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Returns the best price for a flight from London to NYC in
     * British pounds via synchronous computations.
     *
     * @param iteration Current iteration count
     * @param trip The current trip being priced.
     */
    private static void getBestPriceInPoundsSync(int iteration,
                                                 Trip trip,
                                                 CurrencyConversion currencyConversion) {
        Mono<Trip> tripM = sFlightPriceProxy
            // Synchronously find the best price in US dollars between
            // London and New York city.
            .findBestPriceSync(trip, Options.instance().maxTimeout());

        Mono<Double> rateM = sExchangeRateProxy
            // Synchronously determine exchange rate between US
            // dollars and British pounds.
            .queryExchangeRateForSync(currencyConversion,
                                      Mono.just(Options.instance().defaultRate()));

        // The behavior to perform if an exception occurs.
        Function<? super Throwable,
                ? extends Mono<? extends Double>> handleEx = ex -> {
            Options.print("Iteration #"
                  + iteration
                  + " The exception thrown was " + ex.toString());
            return Mono.just(0.0);
        };

        // When tripM and rateM complete convert the price in US
        // dollars to the price in British pounds.  If these sync
        // operations take more than {@code maxTime} then throw the
        // TimeoutException.
        combineAndConvertResults(tripM, rateM, Options.instance().maxTimeout())
            // Print the price if the call completed within sMAX_TIME
            // seconds.
            .doOnSuccess(amount ->
                         Options.print("Iteration #"
                               + iteration
                               + " The price is: "
                               + amount
                               + " GBP"))
                    
            // Consume and print the TimeoutException if the call took
            // longer than sMAX_TIME.
            .onErrorResume(handleEx)

            // Block until the computation is done.
            .block();
    }

    /**
     * When {@code tripM} and {@code rateM} complete convert the
     * price in US dollars to the price in British pounds.  If these
     * operations take more than {@code maxTime} then throw the
     * TimeoutException.
     *
     * @param tripM Returns the best price for a flight leg
     * @param rateM Returns the exchange rate
     * @param maxTime Max time to wait for processing to complete
     * @return A conversion of best price into British pounds
     */
    private static Mono<Double> combineAndConvertResults(Mono<Trip> tripM,
                                                         Mono<Double> rateM,
                                                         Duration maxTime) {
        return Mono
            // Call the this::convert method reference to convert the
            // price in dollars to the price in pounds when both
            // previous Monos complete their processing.
            .zip(tripM, rateM, ReactorTests::convert)

            // If the total processing takes more than maxTime a
            // TimeoutException will be thrown.
            .timeout(maxTime);
    }

    /**
     * Convert the price of a Trip in one currency system by
     * multiplying it by the exchange rate.
     */
    private static double convert(Trip trip, double rate) {
        return trip.getPrice() * rate;
    }
}
