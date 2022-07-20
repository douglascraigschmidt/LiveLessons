package utils;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static utils.ExchangeRate.Currency.*;

/**
 * Manages exchange rates amongst various currencies.
 */
public class ExchangeRate {
    public enum Currency {
        USD,
        GBP,
        EUR
    }

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;
    private static final int RESULT_SCALE = 2;
    private static final int WORKING_SCALE = 20;

    private static final Map<Currency, Double> mRates = new HashMap<>();

    static {
        mRates.put(USD, 1.0);
        mRates.put(GBP, 0.84);
        mRates.put(EUR, 0.99);
    }

    /**
     * This method returns a Mono that emits the exchange rate between
     * the {@code from} and {@code to} parameters asynchronously.
     *
     * @param from 3 letter currency code String to convert from.
     * @param to   3 letter currency code String to convert to.
     * @return A Mono that emits the converted currency value.
     */
    public static Mono<Double> convertMono(Double value, String from, String to) {
        return convertMono(value, Currency.valueOf(from), Currency.valueOf(to));
    }

    /**
     * This method returns a Mono that emits the exchange rate between
     * the {@code from} and {@code to} parameters asynchronously.
     *
     * @param from Currency to convert from.
     * @param to   Currency to convert to.
     * @return A Mono that emits the converted currency value.
     */
    public static Mono<Double> convertMono(Double value, Currency from, Currency to) {
        return Mono.just(convertCurrency(from, to, value));
    }

    /**
     * This method converts the passed {@code from} currency
     * {@code value} to the {@code to} currency value.
     *
     * @param from 3 letter currency code String to convert from.
     * @param to   3 letter currency code String to convert to.
     * @return The converted currency value.
     */
    public static Double convert(Double value, String from, String to) {
        return convert(value, Currency.valueOf(from), Currency.valueOf(to));
    }

    /**
     * This method converts the passed {@code from} currency
     * {@code value} to the {@code to} currency value.
     *
     * @param from Currency to convert from.
     * @param to   Currency to convert to.
     * @return The converted currency value.
     */
    public static Double convert(Double value, Currency from, Currency to) {
        return convertCurrency(from, to, value);
    }

    private static Double convertCurrency(Currency from, Currency to, Double value) {
        if (from.equals(to)) {
            return value;
        } else {
            return convertFromUSD(to, convertToUSD(from, BigDecimal.valueOf(value)))
                    .setScale(RESULT_SCALE, ROUNDING_MODE)
                    .doubleValue();
        }
    }

    private static BigDecimal convertToUSD(Currency from, BigDecimal value) {
        if (from == USD) {
            return value;
        } else {
            return value.divide(
                    BigDecimal.valueOf(mRates.get(from)),
                    WORKING_SCALE,
                    ROUNDING_MODE
            );
        }
    }

    private static BigDecimal convertFromUSD(Currency to, BigDecimal value) {
        if (to == USD) {
            return value;
        } else {
            return BigDecimal.valueOf(mRates.get(to)).multiply(value);
        }
    }
}
