package microservices.exchangerate;

import datamodels.CurrencyConversion;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

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
        // POST the given Double to the URI template and return the
        // response as an Http ResponseEntity.
        ResponseEntity<Double> responseEntity = mRestTemplate
            .postForEntity(mSERVER_BASE_URL + mQueryExchangeRateURISync,
                           currencyConversion,
                           Double.class);

        // Convert the ResponseEntity to a Double and return it.
        return Objects.requireNonNull(responseEntity.getBody());
    }
}
