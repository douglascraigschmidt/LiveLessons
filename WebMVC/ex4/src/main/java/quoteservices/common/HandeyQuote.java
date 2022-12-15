package quoteservices.common;

/**
 * This class stores a quote from Jack Handey.
 *
 * The {@code @Data} annotation generates all the boilerplate that is
 * normally associated with simple Plain Old Java Objects (POJOs) and
 * beans, including getters for all fields and setters for all
 * non-final fields.
 *
 * The {@code NoArgsConstructor} annotation will generate a
 * constructor with no parameters.
 */
public class HandeyQuote {
    /**
     * ID # of the quote.
     */
    private int quoteId;

    /**
     * A quote from Jack Handey.
     */
    private String quote;

    public HandeyQuote() {}

    /**
     * The constructor initializes the fields.
     */
    public HandeyQuote(int quoteId, String quote) {
        this.quoteId = quoteId;
        this.quote = quote;
    }

    @Override
    public String toString() {
        return "[" + quoteId + "] " + quote;
    }

    public String getQuote() {
        return quote;
    }

    public int getQuoteId() {
        return quoteId;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public void setQuoteId(int quoteId) {
        this.quoteId = quoteId;
    }
}
