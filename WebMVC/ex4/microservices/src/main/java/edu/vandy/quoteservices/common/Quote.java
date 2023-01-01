package edu.vandy.quoteservices.common;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.persistence.*;

/**
 * This class stores quotes.
 *
 * The {@code @Data} annotation generates all the boilerplate that is
 * normally associated with simple Plain Old Java Objects (POJOs) and
 * beans, including getters for all fields and setters for all
 * non-final fields.
 *
 * The {@code NoArgsConstructor} annotation will generate a
 * constructor with no parameters.
 */
@Value
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Entity // For Jpa
@Table(name = "QUOTE")
public class Quote {
    /**
     * ID # of the quote.
     */
    @Id
    @Column(name = "id", nullable = false)
    // public int quoteId;
    public Integer quoteId;

    /**
     * A quote.
     */
    @Column(columnDefinition="LONGTEXT")
    public String quote;
}
