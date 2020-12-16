package tests;

import datamodels.CurrencyConversion;
import datamodels.Trip;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import proxies.ExchangeRateProxy;
import proxies.FlightPriceProxy;
import utils.AsyncTaskBarrierRx;
import utils.Options;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
     * best price for a flight from London to NYC in British pounds.
     */
    public static void runAsyncSingles(Trip trip,
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
     * Returns the best price for a flight from London to NYC in
     * British pounds via asynchronous computations.
     *
     * @param iteration Current iteration count
     * @param trip The current trip being priced.
     * @return A Completable to synchronize with the AsyncTaskBarrier framework.
     */
    private static Completable getBestPriceInPoundsAsync(int iteration,
                                                         Trip trip,
                                                         CurrencyConversion currencyConversion) {
        Single<Double> priceS = sFlightPriceProxy
            // Asynchronously find the best price in US dollars
            // between London and New York city.
            .findBestPriceAsyncRx(trip);

        Single<Double> rateS = sExchangeRateProxy
            // Asynchronously determine exchange rate between US
            // dollars and British pounds.
            .queryExchangeRateForAsyncRx(currencyConversion,
                                         Single.just(Options.instance().defaultRate()));

        // When priceM and rateM complete convert the price in US
        // dollars to the price in British pounds.  If these async
        // operations take more than {@code maxTime} then throw the
        // TimeoutException.
        return combineAndConvertResults(priceS, rateS, Options.instance().maxTimeout())
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
            .onErrorResumeNext(ex -> {
                Options.print("Iteration #"
                        + iteration
                        + " The exception thrown was " + ex.toString());
                return Single.just(0.0);
            })

            // Return a Completable to synchronize with the
            // AsyncTaskBarrierRx framework.
            .ignoreElement();
    }

    /**
     * When {@code priceS} and {@code rateS} complete convert the
     * price in US dollars to the price in British pounds.  If these
     * operations take more than {@code maxTime} then throw the
     * TimeoutException.
     *
     * @param priceS Returns the best price for a flight leg
     * @param rateS Returns the exchange rate
     * @param maxTime Max time to wait for processing to complete
     * @return A conversion of best price into British pounds
     */
    private static Single<Double> combineAndConvertResults(Single<Double> priceS,
                                                           Single<Double> rateS,
                                                           Duration maxTime) {
        return Single
                // Call the this::convert method reference to convert the
                // price in dollars to the price in pounds when both
                // previous Monos complete their processing.
                .zip(priceS, rateS, RxJavaTests::convert)

                // If the total processing takes more than maxTime a
                // TimeoutException will be thrown.
                .timeout(maxTime.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Convert a price in one currency system by multiplying it by the
     * exchange rate.
     */
    private static double convert(double price, double rate) {
        return price * rate;
    }
}
