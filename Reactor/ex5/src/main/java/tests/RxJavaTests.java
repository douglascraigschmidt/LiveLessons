package tests;

import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import datamodels.TripResponse;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import microservices.exchangeRate.ExchangeRateProxy;
import microservices.flightPrice.FlightPriceProxy;
import reactor.core.scheduler.Schedulers;
import utils.AsyncTaskBarrierRx;
import utils.Options;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A Java utility class containing tests that use RxJava and WebFlux
 * to asynchronously and synchronously invoke microservices that
 * determine the best price for flights in an airline reservation
 * system.
 */
public class RxJavaTests {
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
     * best price for a {@code trip} using the given {@code
     * currencyConversion}.
     */
    public static void runAsyncSingles(TripRequest trip,
                                       CurrencyConversion currencyConversion) {
        System.out.println("begin runAsyncSingles()");

        // Iterate multiple times.
        for (int i = 0; i < Options.instance().maxIterations(); i++) {
            int iteration = i + 1;

            AsyncTaskBarrierRx
                // Register the test with the AsyncTaskBarrierRx framework so it
                // will run asynchronously wrt the other iterations.
                .register(() -> getBestPriceInPoundsAsync(iteration,
                                                          trip,
                                                          currencyConversion));
        }

        AsyncTaskBarrierRx
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow all the
            // computations to complete running asynchronously.
            .blockingGet();

        System.out.println("end runAsyncMonos()");
    }

    /**
     * Returns the best price for {@code trip} using the given {@code
     * currencyConversion} via asynchronous computations.
     *
     * @param iteration Current iteration count
     * @param tripRequest The current trip being priced
     * @param currencyConversion The currency to convert from and to
     * @return A Completable to synchronize with the AsyncTaskBarrierRx framework.
     */
    private static Completable getBestPriceInPoundsAsync(int iteration,
                                                         TripRequest tripRequest,
                                                         CurrencyConversion currencyConversion) {
        Single<TripResponse> tripS = sFlightPriceProxy
            // Asynchronously find the best price in US dollars
            // between London and New York city.
            .findBestPriceAsyncRx(Schedulers.parallel(),
                                  tripRequest);

        Single<Double> rateS = sExchangeRateProxy
            // Asynchronously determine exchange rate between US
            // dollars and British pounds.
            .queryExchangeRateForAsyncRx(currencyConversion);

        // When priceM and rateM complete convert the price in US
        // dollars to the price in British pounds.  If these async
        // operations take more than {@code maxTime} then throw the
        // TimeoutException.
        return combineAndConvertResults(tripS, rateS, Options.instance().maxTimeout())
            // Print the price if the call completed within
            // sMAX_TIME seconds.
            .doOnSuccess(tripResponse ->
                         Options.print("Iteration #"
                               + iteration
                               + " The price is: "
                               + tripResponse.getPrice()
                               + " GBP on "
                               + tripResponse.getAirlineCode()))
                    
            // Consume and print the TimeoutException if the call
            // took longer than sMAX_TIME.
            .onErrorResumeNext(ex -> {
                Options.print("Iteration #"
                        + iteration
                        + " The exception thrown was " + ex.toString());
                return Single.just(new TripResponse());
            })

            // Return a Completable to synchronize with the
            // AsyncTaskBarrierRx framework.
            .ignoreElement();
    }

    /**
     * When {@code tripM} and {@code rateM} complete convert the price
     * based on the exchange rate.  If these operations take more than
     * {@code maxTime} then the TimeoutException is thrown.
     *
     * @param tripS Returns the best price for a flight leg
     * @param rateS Returns the exchange rate
     * @param maxTime Max time to wait for processing to complete
     * @return A conversion of best price
     */
    private static Single<TripResponse> combineAndConvertResults(Single<TripResponse> tripS,
                                                                 Single<Double> rateS,
                                                                 Duration maxTime) {
        return Single
            // Call the this::convert method reference to convert the
            // price using the given exchange rate when both previous
            // Monos complete their processing.
            .zip(tripS, rateS, RxJavaTests::convert)

            // If the total processing takes more than maxTime a
            // TimeoutException will be thrown.
            .timeout(maxTime.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Convert the price of a Trip in one currency system by
     * multiplying it by the exchange rate.
     */
    private static TripResponse convert(TripResponse trip, double rate) {
        // Update the price to reflect the exchange rate!
        trip.setPrice(trip.getPrice() * rate);
        return trip;
    }
}
