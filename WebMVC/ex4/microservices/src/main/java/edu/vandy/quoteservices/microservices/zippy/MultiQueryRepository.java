package edu.vandy.quoteservices.microservices.zippy;

import edu.vandy.quoteservices.common.Quote;

import java.util.List;

/**
 * This interface defines a method that returns a {@link List} of
 * {@link Quote} objects in the database containing all of
 * the {@code queries} (ignoring case).
 */
public interface MultiQueryRepository {
    /**
     * Find a {@link List} of {@link Quote} objects in the database
     * containing all of the {@code queries} (ignoring case).
     *
     * @param queries List of queries
     * @return A {@link List} of {@link Quote} objects in the database
     *         containing all of the {@code queries}
     *         (ignoring case)
     */
    List<Quote> findAllByQuoteContainingAllIn(List<String> queries);
}
