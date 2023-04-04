package edu.vandy.quoteservices.repository;

import edu.vandy.quoteservices.common.Quote;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * This interface defines methods that return a {@link Flux} that
 * emits {@link Quote} objects in the database containing all or any
 * of the queries (ignoring case).
 */
public interface MultiQueryRepository {
    /**
     * Return a {@link Flux} that emits {@link Quote} objects in the
     * database containing all the {@code queries} (ignoring case).
     *
     * @param queries A {@code List} of {@code String} objects
     *                containing the queries to search for
     * @return A {@code Flux} that emits {@code Quote} objects that
     *         match any of the specified {@code queries} (ignoring
     *         case)
     */
    Flux<Quote> findAllByQuoteContainingIgnoreCaseAllIn(List<String> queries);


    /**
     * Return a {@link Flux} that emits {@link Quote} objects in the
     * database containing any of the {@code queries} (ignoring case).
     *
     * @param queries A {@code List} of {@code String} objects
     *                containing the queries to search for
     * @return A {@link Flux} that emits {@link Quote} objects in the
     *         database containing all the {@code queries} (ignoring
     *         case)
     */
    Flux<Quote> findAllByQuoteContainingIgnoreCaseAnyIn(List<String> queries);
}
