package edu.vandy.quoteservices.common;

import java.util.List;

/**
 * This implementation defines a method that returns a {@link List} of
 * {@link Quote} objects in the database containing all
 * the {@code queries} (ignoring case).
 */
public interface MultiQueryRepository {
    /**
     * Find a {@link List} of {@link Quote} objects in the database
     * containing all of the matching {@code queries} (ignoring case).
     *
     * @param queries List of queries
     * @return A {@link List} of {@link Quote} objects in the database
     *         containing all of the matching {@code queries}
     *         (ignoring case)
     */
    List<Quote> findAllByQuoteContainingAllIn(List<String> queries);
}
