package utils;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class ExchangeRate {
    private final Map<String, Map<String, Double>> mExchangeRate =
        new HashMap<>();

    public ExchangeRate() {
        Map<String, Double> usdExchangeRates = new HashMap<>();
        usdExchangeRates.put("USD", 1.0);
        usdExchangeRates.put("GBP", 0.72);
        usdExchangeRates.put("EUR", 0.85);
        mExchangeRate.put("USD", usdExchangeRates);

        Map<String, Double> eurExchangeRates = new HashMap<>();
        eurExchangeRates.put("EUR", 1.0);
        eurExchangeRates.put("GBP", 0.85);
        eurExchangeRates.put("USD", 1.18);
        mExchangeRate.put("EUR", eurExchangeRates);

        Map<String, Double> gbpExchangeRates = new HashMap<>();
        gbpExchangeRates.put("GBP", 1.0);
        gbpExchangeRates.put("EUR", 1.18);
        gbpExchangeRates.put("USD", 1.38);
        mExchangeRate.put("GBP", gbpExchangeRates);

    }

    public Double queryForExchangeRate(String fromCurrency, String toCurrency) {
        return mExchangeRate.get(fromCurrency).get(toCurrency);
    }
}
