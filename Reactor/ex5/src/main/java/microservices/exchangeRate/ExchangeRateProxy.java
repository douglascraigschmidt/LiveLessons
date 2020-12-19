package microservices.exchangeRate;

import datamodels.CurrencyConversion;
import io.reactivex.rxjava3.core.Single;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.Options;

import java.time.Duration;
import java.util.function.Function;

/**
 * This class serves as a proxy to the ExchangeRate microservice.
 */
public class ExchangeRateProxy {
    /**
     * The URI that denotes a remote method to determine the current exchange rate.
     */
    private final String mQueryExchangeRateURIAsync =
        "/microservices/exchangeRate/_exchangeRateAsync";

    /**
     * The WebClient provides the means to access the ExchangeRate
     * microservice.
     */
    private final WebClient mExchangeRate;

    /**
     * Host/post where the server resides.
     */
    private final String mSERVER_BASE_URL =
        "http://localhost:8081";

    /**
     * Constructor initializes the fields.
     */
    public ExchangeRateProxy() {
        mExchangeRate = WebClient
            // Start building.
            .builder()

            // The URL where the server is running.
            .baseUrl(mSERVER_BASE_URL)

            // Build the webclient.
            .build();
    }

    /**
     * Finds the exchange rate for the {@code sourceAndDestination} asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param currencyConversion The currency to convert from and to
     * @return A Mono containing the exchange rate.
     */
    public Mono<Double> queryExchangeRateForAsync(Scheduler scheduler,
                                                  CurrencyConversion currencyConversion,
                                                  Mono<Double> defaultRate) {
        // Return a mono to the exchange rate.
        return Mono
            .fromCallable(() -> mExchangeRate
                          // Create an HTTP GET request.
                          .get()

                          // Add the uri to the baseUrl.
                          .uri(UriComponentsBuilder
                               .fromPath(mQueryExchangeRateURIAsync)
                               .queryParam("currencyConversion", currencyConversion)
                               .build()
                               .toString())

                          // Retrieve the response.
                          .retrieve()

                          // Convert it to a Mono of Double.
                          .bodyToMono(Double.class))
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Mono<Double>.
            .flatMap(Function.identity())

            // If this computation runs for more than 2 seconds
            // return the default rate.
            .timeout(Options.instance().exchangeRateTimeout(), defaultRate);
    }

    /**
     * Finds the exchange rate for the {@code sourceAndDestination} asynchronously.
     *
     * @param currencyConversion Indicates the currency to convert from and to
     * @return A Single containing the exchange rate.
     */
    public Single<Double> queryExchangeRateForAsyncRx(CurrencyConversion currencyConversion,
                                                      Single<Double> defaultRate) {
        return Single
            // Return a Single to the exchange rate.
            .fromPublisher(Mono
                           .fromCallable(() -> mExchangeRate
                                         // Create an HTTP GET
                                         // request.
                                         .get()

                                         // Add the uri to the
                                         // baseUrl.
                                         .uri(UriComponentsBuilder
                                              .fromPath(mQueryExchangeRateURIAsync)
                                              .queryParam("currencyConversion",
                                                          currencyConversion)
                                              .build()
                                              .toString())

                                         // Retrieve the response.
                                         .retrieve()

                                         // Convert it to a Mono of
                                         // Double.
                                         .bodyToMono(Double.class))

                           // Schedule this to run on the given
                           // scheduler.
                           .subscribeOn(Schedulers.parallel())

                           // De-nest the result so it's a
                           // Mono<Double>.
                           .flatMap(Function.identity())

                           // If this computation runs for more than 2
                           // seconds return the default rate.
                           .timeout(Options.instance().exchangeRateTimeout(),
                                    Mono.from(defaultRate.toFlowable())));
    }

    /**
     * Finds the exchange rate for the {@code sourceAndDestination} synchronously.
     *
     * @param currencyConversion The currency to convert from and to
     * @return A Mono containing the exchange rate.
     */
    public Mono<Double> queryExchangeRateForSync(CurrencyConversion currencyConversion,
                                                 Mono<Double> defaultRate) {
        // Return a mono to the exchange rate.
        return Mono
            .fromCallable(() -> mExchangeRate
                          // Create an HTTP GET request.
                          .get()

                          // Add the uri to the baseUrl.
                          .uri(UriComponentsBuilder
                               .fromPath(mQueryExchangeRateURIAsync)
                               .queryParam("currencyConversion", currencyConversion)
                               .build()
                               .toString())

                          // Retrieve the response.
                          .retrieve()

                          // Convert it to a Mono of Double.
                          .bodyToMono(Double.class))
            
            // De-nest the result so it's a Mono<Double>.
            .flatMap(Function.identity())

            // If this computation runs for more than 2 seconds
            // return the default rate.
            .timeout(Duration.ofSeconds(2), defaultRate);
    }
}
