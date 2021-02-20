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
 * This super class factors out code that's common to the
 * ExchangeRateProxyAsync and ExchangeRateProxySync subclasses.
 */
public class ExchangeRateProxyBase {
    /**
     * A synchronous client used to perform HTTP requests via simple
     * template method API over underlying HTTP client libraries
     */
    final RestTemplate mRestTemplate = new RestTemplate();

    /**
     * The WebClient provides the means to access the ExchangeRate
     * microservice.
     */
    final WebClient mExchangeRate;

    /**
     * Host/port where the server resides.
     */
    final String mSERVER_BASE_URL =
        "http://localhost:8081";

    /**
     * A cache of the latest exchange rate for a CurrencyConversion,
     * which is used if the ExchangeRate microservice fails to respond
     * before the designated timeout elapses.
     */
    final List<CurrencyConversion> mExchangeRateCache =
        new ArrayList<>();

    /**
     * Constructor initializes the fields.
     */
    public ExchangeRateProxyBase() {
        mExchangeRate = WebClient
            // Start building.
            .builder()

            // The URL where the server is running.
            .baseUrl(mSERVER_BASE_URL)

            // Build the webclient.
            .build();
    }

    /**
     * Update the cache for concurrency conversions.
     *
     * @param latestRate The latest exchange rate
     * @param currencyConversion The object that converts currency
     * @return The updated cached exchange rate
     */
    Mono<Double> updateCachedRate(Double latestRate,
                                  CurrencyConversion currencyConversion) {
        boolean updatedCacheEntry = false;

        for (CurrencyConversion cc : mExchangeRateCache)
            if (cc.getFrom().equals(currencyConversion.getFrom())
                    && cc.getTo().equals(currencyConversion.getTo())) {
                cc.setExchangeRate(latestRate);
                updatedCacheEntry = true;
            }

        if (!updatedCacheEntry) {
            currencyConversion.setExchangeRate(latestRate);
            mExchangeRateCache.add(currencyConversion);
        }

        return Mono.just(latestRate);
    }

    /**
     * Return the last cached exchange rate.
     *
     * @param currencyConversion The object that converts currency
     * @return The last cached exchange rate
     */
    Mono<Double> getLastCachedRate(CurrencyConversion currencyConversion) {
        for (CurrencyConversion cc : mExchangeRateCache)
            if (cc.getFrom().equals(currencyConversion.getFrom())
                && cc.getTo().equals(currencyConversion.getTo()))
                return Mono.just(cc.getExchangeRate());

        return Mono.just(Options.instance().defaultRate());
    }
}
