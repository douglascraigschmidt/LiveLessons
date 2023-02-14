package edu.vandy.quoteservices.microservices.handey;

import edu.vandy.quoteservices.common.BaseController;
import edu.vandy.quoteservices.common.BaseService;
import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

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
                    findAnyMatch(quote, queries))
            
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
     * @param queries The {@link List} of queries to match with
     * @return True if there's any match, else false
     */
    private boolean findAnyMatch(Quote quote,
                                 List<String> queries) {
        // Determine if a match occurred.
        var matched = Flux
            // Convert List to a Flux.
            .fromIterable(queries)

            // Emit a single boolean true if any of the values of this
            // Flux sequence are contained in the Quote.
            .any(query -> Objects
                 .requireNonNull(quote.quote)
                 .toLowerCase()
                 .contains(query.toLowerCase()))

            // Execute a blocking call outside the current worker's
            // pool.
            .share()

            // Block until all async processing is finished.
            .block();

        System.out.println("[" + Thread.currentThread() + "] "
                           + quote
                           + (Boolean.TRUE.equals(matched)
                              ? " matched"
                              : " did not match"));

        // Return the result.
        return matched;
    }
}
