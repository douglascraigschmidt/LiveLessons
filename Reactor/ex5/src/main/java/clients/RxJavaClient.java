package clients;

import datamodels.CurrencyConversion;
import datamodels.TripRequest;
import datamodels.TripResponse;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import microservices.apigateway.APIGatewayProxyAsync;
import microservices.exchangerate.ExchangeRateProxyAsync;
import microservices.flightprice.FlightPriceProxyAsync;
import reactor.core.scheduler.Schedulers;
import utils.AsyncTaskBarrierRx;
import utils.Options;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A Java utility class containing a client that provides an RxJava
 * binding to Proect Reactor and WebFlux capabilities to
 * asynchronously invoke microservices that provide various
 * flight-related services for the Airline Booking App (ABA).
 */
public class RxJavaClient {
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
    public static void runAsyncTestsRx(TripRequest trip,
                                       CurrencyConversion currencyConversion) {
        System.out.println("begin runAsyncTestsRx()");

        // Iterate multiple times.
        for (int i = 0; i < Options.instance().maxIterations(); i++) {
            int iteration = i + 1;

            // Register tests with the AsyncTaskBarrierRx framework so
            // they run asynchronously wrt the other iterations.
            AsyncTaskBarrierRx
                .register(() -> findBestPriceAsync(iteration,
                                                   trip,
                                                   currencyConversion));
            AsyncTaskBarrierRx
                .register(() -> findFlightsAsync(iteration,
                                                 trip,
                                                 currencyConversion));
        }

        AsyncTaskBarrierRx
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow all the
            // computations to complete running asynchronously.
            .blockingGet();

        System.out.println("end runAsyncTestsRx()");
    }

    /**
     * Returns the best price for the {@code tripRequest} using the
     * given {@code currencyConversion} via asynchronous computations.
     *
     * @param iteration Current iteration count
     * @param tripRequest The current trip being priced
     * @param currencyConversion The currency to convert from and to
     * @return A Completable to synchronize with the AsyncTaskBarrierRx framework.
     */
    private static Completable findBestPriceAsync(int iteration,
                                                  TripRequest tripRequest,
                                                  CurrencyConversion currencyConversion) {
        return sAPIGatewayProxyAsync
            // Asynchronously find the best price for the tripRequest.
            .findBestPriceRx(Schedulers.parallel(),
                             tripRequest,
                             currencyConversion)

            .doOnSuccess(tripResponse -> Options
                         .print("Iteration #"
                                + iteration
                                + " The best price is: "
                                + tripResponse.getPrice()
                                + " "
                                + currencyConversion.getTo() 
                                + " on "
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
     * Returns all flights for {@code tripRequest} using the given
     * {@code currencyConversion} via asynchronous
     * computations/communications.
     *
     * @param iteration Current iteration count
     * @param tripRequest The current desired trip
     * @param currencyConversion The currency to convert from and to
     * @return A Completable to synchronize with the AsyncTaskBarrierRx framework.
     */
    private static Completable findFlightsAsync(int iteration,
                                                TripRequest tripRequest,
                                                CurrencyConversion currencyConversion) {
        return sAPIGatewayProxyAsync
            // Asynchronously find all the flights in the tripRequest.
            .findFlightsRx(Schedulers.parallel(),
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
                    
            // Consume and print the TimeoutException if the call
            // took longer than sMAX_TIME.
            .onErrorResumeNext(ex -> {
                    Options.print("Iteration #"
                                  + iteration
                                  + " The exception thrown was " + ex.toString());
                    return Observable.just(new TripResponse());
                })

            // Return a Completable to synchronize with the
            // AsyncTaskBarrierRx framework.
            .ignoreElements();
    }
}
