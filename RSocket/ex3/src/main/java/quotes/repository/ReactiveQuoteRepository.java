package quotes.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import quotes.common.model.Quote;

import reactor.core.publisher.Flux;

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
     * This method finds all quotes of the given type.
     *
     * @param play The type of quote to find, e.g., Zippy = 1, Handey = 2, etc.
     * @return A {@link Flux} that emits all quotes of the given type.
     */
    Flux<Quote> findAllByPlay(String play);
}
