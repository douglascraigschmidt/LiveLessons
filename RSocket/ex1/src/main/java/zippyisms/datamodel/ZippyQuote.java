package zippyisms.datamodel;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class stores a quote from Zippy th' Pinhead.
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
