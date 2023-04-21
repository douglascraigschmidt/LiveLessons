package zippyisms.common.model;


/**
 * This record stores a quote from Zippy th' Pinhead.
 */
public record Quote (
    /*
     * ID # of the quote.
     */
     int quoteId,

    /*
     * A quote from Zippy th' Pinhead.
     */
    String quote) {}
