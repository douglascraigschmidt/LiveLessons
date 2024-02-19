package edu.vandy.quoteservices.microservices.zippy;

import edu.vandy.quoteservices.common.BaseController;
import edu.vandy.quoteservices.common.BaseService;
import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

import static edu.vandy.quoteservices.common.Constants.ZIPPY_CACHE;

/**
 * This class defines implementation methods that are called by the
 * {@link BaseController}, which serves as the main "front-end" app
 * gateway entry point for remote clients that want to receive Zippy
 * quotes.
 *
 * This class implements the abstract methods in {@link BaseService}
 * using the Jakarta Persistence API (JPA) and the Java sequential
 * streams framework. It also demonstrates the use of Spring
 * server-side caching.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning. It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class ZippyService
       implements BaseService<List<Quote>> {
    /**
     * Spring-injected repository containing all the Zippy quotes.
     */
    @Autowired
    private ZippyQuoteRepository mRepository;

    /**
     * Return all the {@link Quote} objects.  The {@code Cachable}
     * annotation on this method enables server-side catching.
     *
     * @return A {@link List} of all {@link Quote} objects
     */
    @Cacheable(ZIPPY_CACHE)
    @Override
    public List<Quote> getAllQuotes() {
        System.out.println("ZippyService.getAllQuotes()");

        // Artificially delay this call to demonstrate the
        // benefits of server-side caching.
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return mRepository
            // Forward to the repository.
            .findAll();
    }

    /**
     * Get a {@link Quote} associated with the given {@code quoteId}.
     * This method enables server-side catching at the level of a
     * given {@code quoteId}.  The {@code Cachable} annotation on this
     * method enables server-side catching.
     *
     * @param quoteId An {@link Integer} containing the given {@code
     *                quoteId}
     * @return A {@link Quote} containing the requested {@code
     *         quoteId}
     */
    @Cacheable(value = ZIPPY_CACHE, key = "#quoteId")
    @Override
    public Quote getQuote(Integer quoteId) {
        System.out.println("ZippyService.getQuote()");

        // Artificially delay this call to demonstrate the
        // benefits of server-side caching.
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return mRepository
            // Forward to the repository.
            .findById(quoteId)
            // Throw IllegalArgumentException if quoteId not found.
            .orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @param parallel Run the queries in parallel if true, else run
     *                 sequentially
     * @return A {@link List} of all requested {@link Quote} objects
     */
    @Override
    public List<Quote> postQuotes(List<Integer> quoteIds,
                                  Boolean parallel) {
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
    @Override
    public List<Quote> search(List<String> queries,
                              Boolean parallel) {
        // Use a Java sequential or parallel stream and the JPA to
        // locate all quotes whose 'id' matches the List of 'queries'
        // and return them as a List of Quote objects.
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

            // Ensure duplicate Zippy quotes aren't returned.
            .distinct()

            // Convert the Stream to a List.
            .toList();
    }

    /**
     * Search for quotes that match all the given {@code queries} and
     * return the {@link List} of matches (if any).
     *
     * @param queries The search queries
     * @return A {@code List} containing {@link Quote} objects
     *         matching all the given {@code queries}
     */
    @Override
    public List<Quote> searchEx(List<String> queries,
                                Boolean notUsed) {
         return mRepository
             // Forward to the custom query method.
             .findAllByQuoteContainingIgnoreCaseAllIn(queries);
     }
}
