package edu.vandy.quoteservices.common;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.PersistenceCreator;

import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * This class stores quotes.
 *
 * The {@code NoArgsConstructor} annotation will generate a
 * constructor with no parameters.
 */
@Value
@NoArgsConstructor(force = true)
@Table(name = "QUOTE")
public class Quote {
    /**
     * ID # of the quote.
     */
    @Id
    @Column("id")
    public Integer id;

    /**
     * A quote.
     */
    @Column("quote")
    public String quote;

    /**
     * Initialize columns in a {@link Quote}.
     */
    @PersistenceCreator
    public Quote(Integer id, String quote) {
        this.id = id;
        this.quote = quote;
    }
}
