package edu.vandy.quoteservices.microservice;

import edu.vandy.quoteservices.common.*;
import edu.vandy.quoteservices.repository.ReactiveQuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * This class defines implementation methods that are called by the
 * {@link HandeyController}, which serves as the main "front-end" app
 * gateway entry point for remote clients that want to receive Handey
 * quotes.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning. It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class HandeyService {
    /**
     * Spring-injected repository that contains all quotes.
     */
    @Autowired
    private ReactiveQuoteRepository mRepository;

    /**
     * @return A {@link Flux} of all {@link Quote} objects
     */
    public Flux<Quote> getAllQuotes() {
        return mRepository
            .findAll();
    }

    /**
     * Get a {@link Flux} that contains the requested quotes.
     *
     * @param quoteIds A {@link Flux} containing the given random
     *                 {@code quoteIds}
     * @return A {@link Flux} of all requested {@link Quote} objects
     */
    public Flux<Quote> postQuotes(List<Integer> quoteIds) {
        return mRepository
            .findAllById(quoteIds);
    }

    /**
     * Search for quotes containing any of the given {@link List} of
     * {@code queries} and return a {@link Flux} that emits matching
     * {@link Quote} objects.
     *
     * @param queries The search queries
     * @return A {@code Flux} of quotes containing the given {@code
     *         queries}
     */
    public Flux<Quote> search(List<String> queries) {
        return mRepository.findAllByQuoteContainingIgnoreCaseAnyIn(queries);
    }

    /**
     * Search for quotes containing all the given {@link String} and
     * return a {@link Flux} that emits the matching {@link Quote}
     * objects.
     *
     * @param queries The search queries
     * @return A {@code Flux} that emits {@link Quote} objects
     *         containing the given {@code queries}
     */
    public Flux<Quote> searchEx(List<String> queries) {
        return mRepository.findAllByQuoteContainingIgnoreCaseAllIn(queries);
    }
}
