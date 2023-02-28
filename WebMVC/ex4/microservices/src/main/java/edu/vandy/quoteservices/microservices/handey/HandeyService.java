package edu.vandy.quoteservices.microservices.handey;

import edu.vandy.quoteservices.common.BaseController;
import edu.vandy.quoteservices.common.BaseService;
import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Objects;

import static edu.vandy.quoteservices.utils.RegexUtils.makeAllMatchRegex;
import static edu.vandy.quoteservices.utils.RegexUtils.makeAnyMatchRegex;

/**
 * This class defines implementation methods that are called by the
 * {@link BaseController}, which serves as the main "front-end" app
 * gateway entry point for remote clients that want to receive Handey
 * quotes.
 *
 * This class implements the abstract methods in {@link BaseService}
 * using the Project Reactor framework.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning. It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class HandeyService
       implements BaseService<List<Quote>> {
    /**
     * An in-memory {@link List} of all the quotes.
     */
    @Autowired
    private List<Quote> mQuotes;

    /**
     * @return A {@link List} of all {@link Quote} objects
     */
    @Override
    public List<Quote> getAllQuotes() {
        return mQuotes;
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
        return Flux
            // Convert List to a Flux.
            .fromIterable(quoteIds)

            // Convert Flux to a ParallelFlux.
            .parallel()

            // Run on the appropriate Scheduler.
            .runOn(parallel
                    ? Schedulers.parallel()
                    : Schedulers.single())

            // Get the Handey quote associated with the quoteId.
            .map(mQuotes::get)

            // Convert the ParallelFlux back into a Flux.
            .sequential()

            // Collect the results into a List.
            .collectList()

            // Execute a blocking call outside the current worker's
            // pool.
            .share()

            // Block until all async processing is finished.
            .block();
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
        // Convert the 'query' into a regular expression
        // that matches any query in the 'queries'.
        var regexQuery = makeAnyMatchRegex(queries);

        return Flux
            // Convert List to a Flux.
            .fromIterable(mQuotes)

            // Convert Flux to a ParallelFlux.
            .parallel()
            
            // Perform processing on appropriate Scheduler.
            .runOn(parallel
                    ? Schedulers.parallel()
                    : Schedulers.single())

            // Only keep movies who title matches any of the
            // 'queries'.
            .filter(quote ->
                    findAnyMatch(quote, regexQuery))
            
            // Convert ParallelFlux to a Flux.
            .sequential()

            // Collect the results into a List.
            .collectList()

            // Execute a blocking call outside the current worker's
            // pool.
            .share()

            // Block until all async processing is finished.
            .block();
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
    @Override
    public List<Quote> searchEx(List<String> queries,
                                Boolean parallel) {
        // Convert the 'query' into a regular expression that matches
        // all queries in 'queries'.
        var regexQuery = makeAllMatchRegex(queries);

        System.out.println("regex = " + regexQuery);
        return Flux
            // Convert List to a Flux.
            .fromIterable(mQuotes)

            // Convert Flux to a ParallelFlux.
            .parallel()
            
            // Perform processing on appropriate Scheduler.
            .runOn(parallel
                    ? Schedulers.parallel()
                    : Schedulers.single())

            // Only keep movies who title matches all the 'queries'.
            .filter(quote ->
                    findAllMatch(quote, regexQuery))
            
            // Convert ParallelFlux to a Flux.
            .sequential()

            // Collect the results into a List.
            .collectList()

            // Execute a blocking call outside the current worker's
            // pool.
            .share()

            // Block until all async processing is finished.
            .block();
     }

    /**
     * Determine if {@code quote} contain any of the {@code queries}.
     *
     * @param quote The requested quote
     * @param regexQueries The queries to search for in regular
     *                     expression form
     * @return True if there's any match, else false
     */
    private Boolean findAnyMatch(Quote quote,
                                 String regexQueries) {
        var matched = Objects
                .requireNonNull(quote.quote)
                .toLowerCase()
            // Execute the regex portion of the filter, the "(?s)"
            // enables the 'DOTALL' flag that allows the '.' character
            // to match the newline character '\n'.
            .matches("(?s)" + regexQueries);

        /*
        System.out.println("[" + Thread.currentThread() + "] "
                           + quote.quote
                           + (Boolean.TRUE.equals(matched)
                              ? " matched"
                              : " did not match"));

         */

        // Return the result.
        return matched;
    }

    /**
     * Determine if {@code quote} contain all the {@code queries}.
     *
     * @param quote The requested quote
     * @param regexQueries The queries to search for in regular
     *                     expression form
     * @return True if all {@code regexQueries} match, else false
     */
    private Boolean findAllMatch(Quote quote,
                                 String regexQueries) {
        var matched = Objects
                .requireNonNull(quote.quote)
                .toLowerCase()
            // Execute the regex portion of the filter, the "(?s)"
            // enables the 'DOTALL' flag that allows the '.' character
            // to match the newline character '\n'.
            .matches("(?s)" + regexQueries);

        /*
        System.out.println("[" + Thread.currentThread() + "] "
                           + quote.quote
                           + (Boolean.TRUE.equals(matched)
                              ? " matched"
                              : " did not match"));

         */

        // Return the result.
        return matched;
    }
}
