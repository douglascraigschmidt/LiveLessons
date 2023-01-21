package edu.vandy.quoteservices.microservice.zippy;

import edu.vandy.quoteservices.common.BaseController;
import edu.vandy.quoteservices.common.BaseService;
import edu.vandy.quoteservices.common.Quote;
import edu.vandy.quoteservices.common.QuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class defines implementation methods that are called by the
 * {@link BaseController}, which serves as the main "front-end" app
 * gateway entry point for remote clients that want to receive movie
 * recommendations.
 *
 * This class implements the abstract methods in {@link BaseService}
 * using the Java sequential streams framework.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning. It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class ZippyService
       extends BaseService<List<Quote>> {
    /**
     * Spring-injected repository that contains all quotes.
     */
    @Autowired
    private QuoteRepository mRepository;

    /**
     * @return A {@link List} of all {@link Quote} objects
     */
    public List<Quote> getAllQuotes() {
        System.out.println("ZippyService.getAllQuotes()");

        return mRepository
            // Forward to the repository.
            .findAll();
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link List} of all requested {@link Quote} objects
     */
    public List<Quote> getQuotes(List<Long> quoteIds) {
        System.out.println("ZippyService.getQuotes()");

        return mRepository
            // Forward to the repository.
            .findAllById(quoteIds);
    }

    /**
     * Search for quotes containing the given {@link String} queries
     * and return a {@link List} of matching {@link Quote} objects.
     *
     * @param queries The search queries
     * @param parallel Run the queries in parallel if true, else run
     *                 sequentially
     * @return A {@code List} of quotes containing {@link Quote}
     *         objects matching the given {@code queries}
     */
    public List<Quote> search(List<String> queries,
                              Boolean parallel) {
        // Use a Java sequential or parallel stream and the JPA to
        // locate all movies whose 'id' matches the List of 'queries'
        // and return them as a List of Movie objects.
        return StreamSupport
            // Convert the List to a Stream.
            .stream(queries.spliterator(), parallel)

            // Flatten the Stream of Streams into a Stream.
            .flatMap(query ->  mRepository
                     // Find all Quote rows in the database that match
                     // the 'query'.
                     .findByQuoteContainingIgnoreCase(query)

                     // Convert List to a Stream.
                     .stream())

            // Convert the Stream to a List.
            .toList();
    }
}
