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
 * This class serves as a proxy to the synchronous ExchangeRate
 * microservice that returns the current exchange that converts one
 * currency to another.
 */
public class ExchangeRateProxySync 
       extends ExchangeRateProxyBase {
    /**
     * The URI that denotes a remote method to determine the current
     * exchange rate synchronously.
     */
    private final String mQueryExchangeRateURISync =
        "/microservices/exchangeRateSync/_queryForExchangeRate";

    /**
     * Constructor initializes the super class.
     */
    public ExchangeRateProxySync() {
        super();
    }

    /**
     * Finds the exchange rate for the {@code sourceAndDestination}
     * synchronously.
     *
     * @param currencyConversion The currency to convert from and to
     * @return A Double containing the exchange rate.
     */
    public Double queryForExchangeRate(CurrencyConversion currencyConversion) {
        // GET the given Double to the URI template and return the
        // response as an Http ResponseEntity.
        ResponseEntity<Double> responseEntity = mRestTemplate
            .getForEntity(mSERVER_BASE_URL + mQueryExchangeRateURISync,
                          Double.class,
                          currencyConversion);

        // Convert the ResponseEntity to a Double and return it.
        return Objects.requireNonNull(responseEntity.getBody());
    }
}
