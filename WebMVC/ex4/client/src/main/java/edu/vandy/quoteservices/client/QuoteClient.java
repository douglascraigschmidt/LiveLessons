package edu.vandy.quoteservices.client;

import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@code ZippyController} and {@code
 * HandeyController} web services to request random quotes.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class QuoteClient {
    /**
     * This auto-wired field connects the {@link QuoteClient} to the
     * {@link QuoteClient} that performs HTTP requests synchronously.
     */
    @Autowired
    private QuoteProxy mQuoteProxy;

    /**
     * Get a {@link List} containing the requested quotes.
     *
     * @param routename The service that will perform the request
     * @return An {@link List} containing all the {@link Quote}
     *         objects
     */
    public List<Quote> getAllQuotes(String routename) {
        return mQuoteProxy
            // Forward to the proxy.
            .getAllQuotes(routename);
    }

    /**
     * Get a {@link Quote} corresponding to the given id.
     *
     * @param quoteId An {@link Integer} containing the given
     *                 {@code quoteId}
     * @return A {@link Quote} containing the requested {@code quoteId}
     */
    public Quote getQuote(String routename,
                          Integer quoteId) {
        return mQuoteProxy
            // Forward to the proxy.
            .getQuote(routename, quoteId);
    }

    /**
     * Get a {@link List} containing the requested quotes.
     *
     * @param routename The service that will perform the request
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @param parallel Run the queries in parallel if true, else run
     *                 sequentially
     * @return An {@link List} containing the requested {@link
     *         Quote} objects
     */
    public List<Quote> postQuotes(String routename,
                                  List<Integer> quoteIds,
                                  Boolean parallel) {
        return mQuoteProxy
            // Forward to the proxy.
            .postQuotes(routename,
                       quoteIds,
                       parallel);
    }

    /**
     * Search for quotes containing any {@code queries} in the
     * given {@link List} of {@code queries}.
     *
     * @param routename The service that will perform the request
     * @param queries The {@link List} of queries to search for
     * @param parallel Run the queries in parallel if true, else run
     *                 sequentially
     * @return An {@link List} of {@link Quote} objects that
     *         contain any {@code queries}
     */
    public List<Quote> searchQuotes(String routename,
                                    List<String> queries,
                                    Boolean parallel) {
        return mQuoteProxy
            // Forward to the proxy.
            .search(routename, queries, parallel);
    }

    /**
     * Search for quotes containing all {@code queries} in the given
     * {@link List} of {@code queries} on the Zippy microservice
     * using a custom SQL method.
     *
     * @param route The microservice that performs the request
     * @param queries The {@link List} of queries to search for
     *  @param parallel Run the queries in parallel if true, else run
     *                  sequentially
     * @return An {@link List} of {@link Quote} objects that
     *         contain all {@code queries}
     */
    public List<Quote> searchQuotesEx(String route,
                                      List<String> queries,
                                      Boolean parallel) {
        return mQuoteProxy
            // Forward to the proxy.
            .searchEx(route,
                      queries,
                      parallel);
    }
}
