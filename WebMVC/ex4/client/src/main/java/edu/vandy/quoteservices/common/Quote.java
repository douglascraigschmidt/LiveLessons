package edu.vandy.quoteservices.common;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * This class stores quotes.
 *
 * The {@code @Value} annotation generates all the boilerplate that is
 * normally associated with simple Plain Old Java Objects (POJOs) and
 * beans, including getters for all fields and setters for all
 * non-final fields.
 *
 * The {@code RequiredArgsConstructor} annotation will generate a
 * constructor with required arguments.
 *
 * The {@code NoArgsConstructor} annotation will generate a
 * constructor with no parameters.
 */
@Value
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class Quote {
    /**
     * ID # of the quote.
     */
    public int quoteId;

    /**
     * A quote.
     */
    public String quote;
}
