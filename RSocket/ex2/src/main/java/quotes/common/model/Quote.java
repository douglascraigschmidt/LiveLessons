package quotes.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class stores a quote from various sources, e.g., Zippy,
 * Handey, etc.
 *
 * The {@code @Data} annotation generates all the boilerplate that is
 * normally associated with simple Plain Old Java Objects (POJOs) and
 * beans, including getters for all fields and setters for all
 * non-final fields.
 *
 * The {@code NoArgsConstructor} annotation will generate a
 * constructor with no parameters.
 */
@Data
@NoArgsConstructor
public class Quote {
    /**
     * ID # of the quote.
     */
    private int quoteId;

    /**
     * A quote from some source, e.g., Zippy, Handey, etc.
     */
    private String quote;

    /**
     * The constructor initializes the fields.
     */
    public Quote(int quoteId, String quote) {
        this.quoteId = quoteId;
        this.quote = quote;
    }
}
