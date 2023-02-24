package edu.vandy.quoteservices.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * A persistent repository that contains information about {@link
 * Quote} objects.
 *
 * The {@code @Repository} annotation indicates that this class
 * provides the mechanism for storage, retrieval, search, update and
 * delete operation on {@link Quote} objects.
 */
@Repository
public interface QuoteRepository
       extends JpaRepository<Quote, Integer>,
        MultiQueryRepository {
    /**
     * Find all {@link Quote} rows in the database that contain the
     * {@code query} {@link String} (ignoring case).
     *
     * @param query The {@link String} to search for
     * @return A {@link List} of {@link Quote} objects that match the
     *         {@code query}
     */
    List<Quote> findByQuoteContainingIgnoreCase(String query);
}
