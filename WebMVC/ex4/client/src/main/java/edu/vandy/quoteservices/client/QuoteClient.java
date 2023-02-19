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
     * Spring WebMVC maps HTTP GET requests sent to the {@code
     * GET_ALL_QUOTES} endpoint to this method.
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
     * Spring WebMVC maps HTTP GET requests sent to the {@code
     * GET_QUOTES} endpoint to this method.
     *
     * @param routename The service that will perform the request
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @param parallel Run the queries in parallel if true, else run
     *                 sequentially
     * @return An {@link List} containing the requested {@link
     *         Quote} objects
     */
    public List<Quote> getQuotes(String routename,
                                 List<Integer> quoteIds,
                                 Boolean parallel) {
        return mQuoteProxy
            // Forward to the proxy.
            .getQuotes(routename,
                       quoteIds,
                       parallel);
    }

    /**
     * Spring WebMVC maps HTTP GET requests sent to the {@code
     * SEARCH_QUOTES} endpoint to this method.
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
}
