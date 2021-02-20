package datamodels;

import java.time.LocalDate;

/**
 * This class keeps track of currency to convert from and the currency
 * to convert to.
 */
public class CurrencyConversion {
    /**
     * The name of the currency to convert from.
     */
    String from;

    /**
     * The name of the currency to convert to.
     */
    String to;

    /**
     * The exchange rate for the "from" currency to
     * the "to" currency.
     */
    Double exchangeRate;

    /**
     * Default constructor needed for WebFlux.
     */
    CurrencyConversion() {
    }

    /** 
     * This constructor initializes the fields.
     */
    CurrencyConversion(String from,
                       String to,
                       Double defaultExchangeRate) {
        this.from = from;
        this.to = to;
        this.exchangeRate = defaultExchangeRate;
    }

    /**
     * Get the currency to convert from.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Set the currency to convert from.
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Get the currency to convert to.
     */
    public String getTo() {
        return to;
    }

    /**
     * Set the currency to convert to.
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Get the exchange rate for the "from" currency to
     * the "to" currency.
     */
    public Double getExchangeRate() {
        return exchangeRate;
    }

    /**
     * Set the exchange rate for the "from" currency to
     * the "to" currency.
     */
    public void setExchangeRate(Double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    /**
     * A factory method that creates a {@code ConcurrencyConversion}
     * object.
     *
     * @param from Currency to convert from
     * @param to Currency to convert to
     * @param exchangeRate the exchange rate for the "from" currency to
     *        the "to" currency
     * @return A new {@code CurrencyConversion} object
     */
    public static CurrencyConversion valueOf(String from,
                                             String to,
                                             Double exchangeRate) {
        return new CurrencyConversion(from, to, exchangeRate);
    }
}
