package edu.vandy.quoteservices.handeymicroservice.model;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import javax.persistence.*;
import java.util.List;

/**
 * Movie title and vector as stored and returned from the database
 * microservice.
 */
@Value
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
@Entity // For Jpa
@Table(name = "QUOTE")
public class Quote {
    /**
     * The movie name.
     */
    @Id
    @Column(name = "id", nullable = false)
    public Integer id;

    /**
     * The encoding of the movie properties.
     */
    @Column(columnDefinition="LONGTEXT")
    public String quote;
}
