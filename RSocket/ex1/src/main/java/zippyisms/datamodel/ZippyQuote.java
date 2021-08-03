package zippyisms.datamodel;

import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
public class ZippyQuote {
    /**
     * ID # of the quote.
     */
    private int quoteId;

    /**
     * A quote from Zippy th' Pinhead.
     */
    private String zippyism;

    /**
     * The constructor initializes the fields.
     */
    public ZippyQuote(int quoteId, String zippyism) {
        this.quoteId = quoteId;
        this.zippyism = zippyism;
    }
}
