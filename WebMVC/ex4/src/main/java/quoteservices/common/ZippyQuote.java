package quoteservices.common;

/**
 * This class stores a quote from Zippy th' Pinhead.
 *
 * The {@code @Data} annotation generates all the boilerplate that is
 * normally associated with simple Plain Old Java Objects (POJOs) and
 * beans, including getters for all fields and setters for all
 * non-final fields.
 *
 * The {@code NoArgsConstructor} annotation will generate a
 * constructor with no parameters.
 */
public class ZippyQuote {
    /**
     * ID # of the quote.
     */
    private int quoteId;

    /**
     * A quote from Zippy th' Pinhead.
     */
    private String quote;

    public ZippyQuote() {}

    /**
     * The constructor initializes the fields.
     */
    public ZippyQuote(int quoteId, String quote) {
        this.quoteId = quoteId;
        this.quote = quote;
    }

    @Override
    public String toString() {
        return "[" + quoteId + "] " + quote;
    }

    public int getQuoteId() {
        return quoteId;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public void setQuoteId(int quoteId) {
        quoteId = quoteId;
    }
}
