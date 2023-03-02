package edu.vandy.quoteservices.common;

import reactor.core.publisher.Flux;

import java.util.List;

/**
 * This implementation defines a method that returns a {@link List} of
 * {@link Quote} objects in the database containing at least one of
 * the {@code queries} (ignoring case).
 */
public interface MultiQueryRepository {
    /**
     * Find a {@link List} of {@link Quote} objects in the database
     * containing at least one of the {@code queries} (ignoring case).
     *
     * @param queries List of queries
     * @return A {@link List} of {@link Quote} objects in the database
     *         containing at least one of the {@code queries}
     *         (ignoring case)
     */
    Flux<Quote> findAllByQuoteContainingAllIn(List<String> queries);
}
