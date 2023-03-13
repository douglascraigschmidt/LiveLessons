package edu.vandy.quoteservices.common;

/*
import org.springframework.data.relational.core.mapping.Table;
*/
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.PersistenceCreator;

import jakarta.persistence.Table;

import org.springframework.data.annotation.Id;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    /**
     * A quote.
     */
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
