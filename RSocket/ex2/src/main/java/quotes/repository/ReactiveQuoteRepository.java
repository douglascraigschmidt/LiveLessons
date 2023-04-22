package quotes.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import quotes.common.model.Quote;

import quotes.common.model.SubscriptionType;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * A persistent repository containing information about
 * {@link Quote} objects using the R2DBC reactive database.
 *
 * The {@code @Repository} annotation indicates that this class
 * provides the mechanism for storage, retrieval, search, update and
 * delete operation on {@link Quote} objects.
 */
@Repository
public interface ReactiveQuoteRepository
       extends ReactiveCrudRepository<Quote, Integer> {
    /**
     * This method finds all {@link Quote} objects of the
     * given {@link SubscriptionType}.
     *
     * @param types A {@link List} of {@link SubscriptionType}
     *             objects to find, e.g., ZIPPY = 1, HANDEY
     *             = 2, etc.
     * @return A {@link Flux} that emits all {@link Quote} objects
     *         of the given {@link SubscriptionType}
     */
    Flux<Quote> findAllByTypeIn(List<Integer> types);
}
