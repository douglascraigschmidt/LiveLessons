package datamodels;

import java.time.LocalDate;

/**
 * This class keeps track of currency to convert from and the currency to convert to.
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
     * Default constructor needed for WebFlux.
     */
    CurrencyConversion() {

    }
    CurrencyConversion(String from, String to) {
        this.from = from;
        this.to = to;
    }

    /**
     *
     */
    public String getFrom() {
        return from;
    }

    /**
     *
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     *
     */
    public String getTo() {
        return to;
    }

    /**
     *
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     *
     * @param from
     * @param to
     * @return
     */
    public static CurrencyConversion valueOf(String from,
                                             String to) {
        return new CurrencyConversion(from, to);
    }
}
