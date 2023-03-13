package edu.vandy.quoteservices.common;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import org.springframework.data.annotation.Id;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

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
@Entity // For JPA
@Table(name = "QUOTE")
public class Quote
       implements Comparable<Quote> {
    /**
     * ID # of the quote.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    /**
     * A quote.
     */
    // @Column(name = "quote", nullable = false)
    public String quote;

/**
 * Perform a comparison of this {@link Quote}
 * with the {@code other} {@link Quote} based on their {@code quote}
 * columns.
 *
 * @param other The {@link Quote} to compare to this {@link Quote}
 * @return A negative integer, zero, or a positive integer as this
 *         movie's ID is less than, equal to, or greater than the
 *         specified movie's ID (ignoring case)
 */
    @Override
    public int compareTo(Quote other) {
        // Compare the quote field of this Quote with the quote
        // field of the other Quote and return the results.
        assert this.id != null;
        assert other.id != null;
        return this.id
            .compareTo(other.id);
    }
}
