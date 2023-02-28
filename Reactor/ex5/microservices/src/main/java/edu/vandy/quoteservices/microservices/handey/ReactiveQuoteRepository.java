package edu.vandy.quoteservices.microservices.handey;

import edu.vandy.quoteservices.common.Quote;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
// import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * A persistent repository that contains information about {@link
 * Quote} objects.
 *
 * The {@code @Repository} annotation indicates that this class
 * provides the mechanism for storage, retrieval, search, update and
 * delete operation on {@link Quote} objects.
 */
@Repository
public interface ReactiveQuoteRepository
       extends R2dbcRepository<Quote, Integer> /*,
                                               MultiQueryRepository */ {
    /**
     * Find all {@link Quote} rows in the database that contain the
     * {@code query} {@link String} (ignoring case).
     *
     * @param query The {@link String} to search for
     * @return A {@link Flux} of {@link Quote} objects that match the
     *         {@code query}
     */
    Flux<Quote> findByQuoteContainingIgnoreCase(String query);
}
