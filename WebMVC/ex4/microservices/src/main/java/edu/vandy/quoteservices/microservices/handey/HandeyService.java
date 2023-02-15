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
       extends BaseService<List<Quote>> {
    /**
     * An in-memory {@link List} of all the quotes.
     */
    @Autowired
    private List<Quote> mQuotes;

    /**
     * @return A {@link List} of all {@link Quote} objects
     */
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
    public List<Quote> getQuotes(List<Integer> quoteIds,
                                 Boolean parallel) {
        var numberOfThreads = parallel
            ? Runtime.getRuntime().availableProcessors()
            : 1;

        return Flux
            // Convert List to a Flux.
            .fromIterable(quoteIds)

            // Convert Flux to a ParallelFlux.
            .parallel(numberOfThreads)

            // Run on the parallel Scheduler.
            .runOn(Schedulers.parallel())

            // Get the Handey quote associated with the quoteId.
            .map(mQuotes::get)

            // Convert the ParallelFlux back into a Flux.
            .sequential()

            // Collect the results into a List.
            .collectList()

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
    public List<Quote> search(List<String> queries,
                              Boolean parallel) {
        // Convert the 'query' into a regular expression.
        var regexQuery = makeRegex(queries);

        // Determine the appropriate Scheduler.
        var scheduler = parallel
            ? Schedulers.parallel()
            : Schedulers.single();

        return Flux
            // Convert List to a Flux.
            .fromIterable(mQuotes)

            // Convert Flux to a ParallelFlux.
            .parallel()
            
            // Perform processing on 'scheduler'.
            .runOn(scheduler)

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
     * Convert the {@link List} of {@link String} objects containing
     * the queries into a single regular expression {@link String}.
     *
     * @param queries The {@link List} of queries
     * @return A {@link String} that encodes the {@code queries} in
     *         regular expression form
     */
    private static String makeRegex(List<String> queries) {
        // Combine the 'queries' List into a lowercase String and
        // convert into a regex of style
        // (.*{query_1}.*)|(.*{query_2}.*)...(.*{query_n}.*)
        var result = queries
            // toString() returns the values as a comma-separated
            // string enclosed in square brackets.
            .toString()

            // Lowercase for matching purposes.
            .toLowerCase()

            // Start of regex.
            .replace("[", "(.*")

            // Separators between queries previous operations added in
            // a space with each comma.
            .replace(", ", ".*)|(.*")

            // End of regex.
            .replace("]", ".*)");

        System.out.println("regexQueries = " + result);
        return result;
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
            // Execute the regex portion of the filter.
            .matches(regexQueries);

        System.out.println("[" + Thread.currentThread() + "] "
                           + quote
                           + (Boolean.TRUE.equals(matched)
                              ? " matched"
                              : " did not match"));

        // Return the result.
        return matched;
    }

}
