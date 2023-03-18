package edu.vandy.quoteservices.common;

import jakarta.persistence.*;

/**
 * This class stores {@link Quote} objects in the JPA and R2DBC
 * databases.
 */
@Entity
@Table(name = "QUOTE")
public class Quote
       implements Comparable<Quote> {
    /**
     * ID # of the quote.
     */
    @Id // For JPA
    @org.springframework.data.annotation.Id // For R2DBC
    public Integer id;

    /**
     * A quote.
     */
    public String quote;

    /**
     * Construct a new {@link Quote} object with the given ID and
     * quote text.
     *
     * @param id The unique identifier for the {@link Quote}
     * @param quote The text of the {@link Quote}
     */
    public Quote(Integer id, String quote) {
        this.id = id;
        this.quote = quote;
    }

    /**
     * This class needs a default constructor.
     */
    public Quote() {
    }

    /**
     * Perform a comparison of this {@link Quote} with the {@code
     * other} {@link Quote} based on their {@code quote} columns.
     *
     * @param other The {@link Quote} to compare to this {@link Quote}
     * @return A negative integer, zero, or a positive integer as this
     *         movie's ID is less than, equal to, or greater than the
     *         specified movie's ID (ignoring case)
     */
    @Override
    public int compareTo(Quote other) {
        assert this.id != null;
        assert other.id != null;
        return this.id
            // Compare the quote field of this Quote with the quote
            // field of the other Quote and return the results.
            .compareTo(other.id);
    }

}
