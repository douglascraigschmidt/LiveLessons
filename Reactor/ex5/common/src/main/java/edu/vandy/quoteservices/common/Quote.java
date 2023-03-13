package edu.vandy.quoteservices.common;

import jakarta.persistence.*;

/**
 * This class stores quotes.
 */
@Entity
@Table(name = "QUOTE")
public class Quote {
    /**
     * ID # of the quote.
     */
    @Id // For JPA
    @org.springframework.data.annotation.Id // For r2dbc
    //@@Doug: you can get rid of this line if you don't add new values to the table from source.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    /**
     * A quote.
     */
    public String quote;

    public Quote(int id, String quote) {
        this.id = id;
        this.quote = quote;
    }

    public Quote() {
    }
}
