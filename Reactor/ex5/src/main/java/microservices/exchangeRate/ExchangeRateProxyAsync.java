package microservices.exchangeRate;

import datamodels.CurrencyConversion;
import datamodels.TripResponse;
import io.reactivex.rxjava3.core.Single;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.Options;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * This class serves as a proxy to the asynchronous ExchangeRate
 * microservice that returns the current exchange that converts one
 * currency to another.
 */
public class ExchangeRateProxyAsync
       extends ExchangeRateProxyBase {
    /**
     * The URI that denotes a remote method to determine the current
     * exchange rate asynchronously.
     */
    private final String mQueryExchangeRateURIAsync =
        "/microservices/exchangeRateAsync/_queryForExchangeRate";

    /**
     * Constructor initializes the super class.
     */
    public ExchangeRateProxyAsync() {
        super();
    }

 /**
     * Finds the exchange rate for the {@code sourceAndDestination}
     * asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the
     *                  operation
     * @param currencyConversion The currency to convert from and to
     * @return A Mono containing the exchange rate.
     */
    public Mono<Double> queryForExchangeRate(Scheduler scheduler,
                                             CurrencyConversion currencyConversion) {
        // Return a mono to the exchange rate.
        return Mono
            .fromCallable(() -> mExchangeRate
                          // Create an HTTP GET request.
                          .get()

                          // Update the uri and add it to the baseUrl.
                          .uri(UriComponentsBuilder
                               .fromPath(mQueryExchangeRateURIAsync)
                               // Insert the currencyConversion into
                               // the URL.
                               .queryParam("currencyConversion", 
                                           currencyConversion)
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

            // Update the cache with the latest rate.
            .flatMap(latestRate -> updateCachedRate(latestRate, currencyConversion))

            // If this computation runs for more than the configured
            // number of seconds return the last cached rate.
            .timeout(Options.instance().exchangeRateTimeout(),
                     getLastCachedRate(currencyConversion));
    }
    
    /**
     * Finds the exchange rate for the {@code sourceAndDestination} asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the
     *                  operation
     * @param currencyConversion Indicates the currency to convert from and to
     * @return A Single containing the exchange rate.
     */
    public Single<Double> queryForExchangeRateRx(Scheduler scheduler,
                                                 CurrencyConversion currencyConversion) {
        return Single
            // Return a Single to the exchange rate.
            .fromPublisher(queryForExchangeRate(scheduler,
                                                currencyConversion));
    }
}
