package quotes.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import quotes.common.model.Quote;

import reactor.core.publisher.Flux;

import java.util.List;

/**
 * A persistent repository containing information about
 * Shakespeare's plays and their quotes. This repository is used
 * to store and retrieve {@link Quote} objects.
 *
 * The {@code @Repository} annotation indicates that this class
 * provides the mechanism for storage, retrieval, search, update and
 * delete operation on {@link Quote} objects.
 */
@Repository
public interface ReactiveQuoteRepository
       extends ReactiveCrudRepository<Quote, Integer> {
    /**
     * This method finds all {@link Quote} objects associated
     * with the given Shakespeare play.
     *
     * @param play The Shakespeare play, e.g., "Hamlet",
     *             "Macbeth", "etc.
     * @return A {@link Flux} that emits all the {@link Quote}
     *         objects matching the given play
     */
    Flux<Quote> findAllByPlay(String play);

    /**
     * This method finds all {@link Quote} objects identified by
     * the given {@link List} of {@code queryIds}.
     *
     * @param queryIds The {@link List} of {@code queryIds} to find
     * @return A {@link List} that emits all identified
     *          {@link Quote} objects
     */
    Flux<Quote> findAllByIdIn(List<Integer> queryIds);
}
