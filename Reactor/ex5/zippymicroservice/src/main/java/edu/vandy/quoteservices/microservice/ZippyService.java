package edu.vandy.quoteservices.microservice;

import edu.vandy.quoteservices.common.JPAQuoteRepository;
import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import reactor.core.publisher.Flux;

/**
 * This class defines implementation methods that are called by the
 * {@link ZippyController}, which serves as the main "front-end" app
 * gateway entry point for remote clients that want to receive Zippy
 * quotes.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning. It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class ZippyService {
    /**
     * Spring-injected repository that contains all quotes.
     */
    @Autowired
    private JPAQuoteRepository mRepository;

    /**
     * @return A {@link Flux} that emits all {@link Quote} objects
     */
    public Flux<Quote> getAllQuotes() {
        return Flux
            // Convert List to a Flux.
            .fromIterable(mRepository
                          // Forward to the repository.
                          .findAll());
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link Flux} that emits all requested {@link Quote} objects
     */
    public Flux<Quote> postQuotes(List<Integer> quoteIds) {
        return Flux
            // Convert List to a Flux.
            .fromIterable(mRepository
                          // Forward to the repository.
                          .findAllById(quoteIds));
    }

    /**
     * Search for quotes containing any of the given {@link List} of
     * {@code queries} and return a {@link Flux} that emits matching
     * {@link Quote} objects.
     *
     * @param queries The search queries
     * @return A {@code Flux} that emits {@link Quote} objects
     *         matching the given {@code queries}
     */
    public Flux<Quote> search(List<String> queries) {
        // Use a Java sequential or parallel stream and the JPA to
        // locate all quotes whose 'id' matches the List of 'queries'
        // and return them as a List of Quote objects.
        return Flux
            .fromIterable(queries.parallelStream()
                          // Flatten the Stream of Streams into a
                          // Stream.
                          .flatMap(query ->  mRepository
                                   // Find all Quote rows in the
                                   // database that match the 'query'.
                                   .findByQuoteContainingIgnoreCase(query)

                                   // Convert List to a Stream.
                                   .stream())

                          // Elimintate duplicate Zippy quotes.
                          .distinct()

                          // Convert the Stream to a List.
                          .toList());
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
        // Use the JPA to locate all quotes whose 'id' matches the
        // List of 'queries' and return them as a Flux of Quote
        // objects.
        return Flux
            // Convert the List to a Stream.
            .fromIterable(mRepository
                          // Forward to the repository.
                          .findAllByQuoteContainingAllIn(queries));
    }
}
