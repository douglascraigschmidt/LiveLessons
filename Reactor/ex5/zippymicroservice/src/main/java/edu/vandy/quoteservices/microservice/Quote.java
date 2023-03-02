package edu.vandy.quoteservices.microservice;

import jakarta.persistence.*;
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
    @Column(name = "quote", nullable = false)
    public String quote;
}
