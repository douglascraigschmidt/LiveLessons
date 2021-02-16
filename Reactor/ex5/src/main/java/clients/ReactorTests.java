package clients;

import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.exchangeRate.ExchangeRateProxyAsync;
import microservices.flightPrice.FlightPriceProxyAsync;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.AsyncTaskBarrier;
import utils.Options;

import java.time.Duration;
import java.util.function.Function;

/**
 * A Java utility class containing tests that use Project Reactor and
 * WebFlux to asynchronously and synchronously invoke microservices
 * that determine the best price for flights in an airline reservation
 * system.
 */
public class ReactorTests {
    /**
     * A proxy that's used to communicate with the FlightPrice
     * microservice.
     */
    private static final FlightPriceProxyAsync sFlightPriceProxyAsync =
        new FlightPriceProxyAsync();

    /**
     * A proxy that's used to communicate with the ExchangeRate
     * microservice asynchronously.
     */
    private static final ExchangeRateProxyAsync sExchangeRateProxyAsync =
            new ExchangeRateProxyAsync();

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
        Mono<TripResponse> tripM = sFlightPriceProxyAsync
            // Asynchronously find the best price in US dollars for
            // the tripRequest.
            .findBestPrice(Schedulers.parallel(),
                           tripRequest);

        Mono<Double> rateM = sExchangeRateProxyAsync
            // Asynchronously determine the exchange rate.
            .queryForExchangeRate(Schedulers.parallel(),
                                  currencyConversion);

        // The behavior to perform if an exception occurs.
        Function<? super Throwable,
            ? extends Mono<? extends TripResponse>> handleEx = ex -> {
            Options.print("Iteration #"
                          + iteration
                          + " The exception thrown was " + ex.toString());
            return Mono.just(new TripResponse());
        };

        // When tripM and rateM complete convert the price.  If these
        // async operations take more than {@code maxTime} then throw
        // the TimeoutException.
        return combineAndConvertResults(tripM, rateM, Options.instance().maxTimeout())
            // Print the price if the call completed within
            // sMAX_TIME seconds.
            .doOnSuccess(tripResponse ->
                         Options.print("Iteration #"
                                       + iteration
                                       + " The best price is: "
                                       + tripResponse.getPrice()
                                       + " GBP on "
                                       + tripResponse.getAirlineCode()))
                    
            // Consume and print the TimeoutException if the call
            // took longer than sMAX_TIME.
            .onErrorResume(handleEx)

            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Returns all flights for {@code tripRequest} using the given
     * {@code currencyConversion} via asynchronous computations/communications.
     *
     * @param iteration Current iteration count
     * @param tripRequest The current desired trip
     * @param currencyConversion The currency to convert from and to
     * @return An empty Mono to synchronize with the AsyncTaskBarrier framework.
     */
    private static Mono<Void> findFlightsAsync(int iteration,
                                               TripRequest tripRequest,
                                               CurrencyConversion currencyConversion) {
        Flux<TripResponse> tripF = sFlightPriceProxyAsync
            // Asynchronously find all the flights in the tripRequest.
            .findFlights(Schedulers.parallel(),
                         tripRequest);

        Mono<Double> rateM = sExchangeRateProxyAsync
            // Asynchronously determine the exchange rate.
            .queryForExchangeRate(Schedulers.parallel(),
                                  currencyConversion);

        // The behavior to perform if an exception occurs.
        Function<? super Throwable,
            ? extends Mono<? extends TripResponse>> handleEx = ex -> {
            Options.print("Iteration #"
                          + iteration
                          + " The exception thrown was " + ex.toString());
            return Mono.just(new TripResponse());
        };

        // When tripF and rateM complete convert the price.  If these
        // async operations take more than {@code maxTime} then throw
        // the TimeoutException.
        return combineAndConvertResults(tripF, rateM, Options.instance().maxTimeout())
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

    /**
     * When {@code tripF} and {@code rateM} complete convert the price
     * based on the exchange rate.  If these operations take more than
     * {@code maxTime} then the TimeoutException is thrown.
     *
     * @param tripF Emits the TripResponse objects
     * @param rateM Emits the exchange rate
     * @param maxTime Max time to wait for processing to complete
     * @return A conversion of best price
     */
    private static Flux<TripResponse> combineAndConvertResults(Flux<TripResponse> tripF,
                                                               Mono<Double> rateM,
                                                               Duration maxTime) {
        return rateM
            // When rateM emits use the resulting exchange rate to update
            // all trip responses accordingly.
            .flatMapMany(rate -> tripF
                         // map() is called when both the Flux and
                         // Mono complete their processing to convert
                         // the price using the exchange rate.
                         .map(trip -> trip.convert(rate)))

            // If the total processing takes more than maxTime a
            // TimeoutException will be thrown.
            .timeout(maxTime);
    }

    /**
     * When {@code tripM} and {@code rateM} complete convert the price
     * based on the exchange rate.  If these operations take more than
     * {@code maxTime} then the TimeoutException is thrown.
     *
     * @param tripM Emits the best price for a flight leg
     * @param rateM Emits the exchange rate
     * @param maxTime Max time to wait for processing to complete
     * @return A conversion of best price
     */
    private static Mono<TripResponse> combineAndConvertResults(Mono<TripResponse> tripM,
                                                               Mono<Double> rateM,
                                                               Duration maxTime) {
        return Mono
            // Call the ReactorTests::convert method reference to
            // convert the price using the given exchange rate when
            // both previous Monos complete their processing.
            .zip(rateM, tripM, (rate, trip) -> trip.convert(rate))

            // If the total processing takes more than maxTime a
            // TimeoutException will be thrown.
            .timeout(maxTime);
    }
}
