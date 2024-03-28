package edu.vandy.quoteservices.common;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Comparator;

/**
 * This class stores {@link Quote} objects in the JPA and R2DBC
 * databases.
 *
 * The {@code @Entity} annotation indicates that {@link Quote}
 * entities are persistence objects stored as records in
 * the database.
 *
 * The {@code @Table} annotation specifies the table in the
 * database with which each {@link Quote} entity is mapped.
 *
 */
@SuppressWarnings("LombokGetterMayBeUsed")
@Entity
@Table(name = "QUOTE")
public class Quote
       implements Comparable<Quote> {
    /**
     * ID # of the quote.  The {@code @Id} annotation specifies
     * the primary key of an entity.
     */
    @Id // For JPA
    @org.springframework.data.annotation.Id // For R2DBC
    public Integer id;

    /**
     * A quote that's stored in the database.
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
     * @return The {@link String} value of the quote
     */
    public String getQuote() {
        return quote;
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
        // Compare quote field of this Quote with quote field of the
        // other Quote and return the results in a null-safe way.
        Comparator<String> nullSafeStringComparator = Comparator
            .nullsFirst(String::compareToIgnoreCase);

        return nullSafeStringComparator.compare(this.quote, 
                                                other.quote);
    }

    /**
     * Overrides the {@code equals()} method to compare two {@link
     * Quote} objects based on their {@code id} only.
     *
     * @param object The other {@link Object} to compare with this
     *              object
     * @return true if the object ids are equal, false otherwise
     */
    @Override
    public boolean equals(Object object) {
        return object instanceof Quote other
            && Objects.equals(this.id, other.id);
    }

    /**
     * @return A hash of the {@link Quote} {@code quote}
     */
    @Override
    public int hashCode() {
        return Objects.hash(quote);
    }
}
