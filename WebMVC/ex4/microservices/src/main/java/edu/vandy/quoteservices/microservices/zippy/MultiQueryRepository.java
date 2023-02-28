package edu.vandy.quoteservices.microservices.zippy;

import edu.vandy.quoteservices.common.Quote;

import java.util.List;

/**
 * This interface defines a means to perform multiple queries
 * on the Quote database.
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
    List<Quote> findAllByQuoteContainingIgnoreCaseAllIn
                    (List<String> queries);
}
