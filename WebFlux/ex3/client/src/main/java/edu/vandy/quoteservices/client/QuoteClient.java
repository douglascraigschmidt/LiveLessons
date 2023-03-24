package edu.vandy.quoteservices.client;

import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.Service.HANDEY;

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
     * {@link HandeyQuoteAPI} that exchanges HTTP requests with the
     * {@code HandeyApplication} microservice asynchronously.
     */
    @Autowired
    private HandeyQuoteAPI mHandeyQuoteAPI;

    /**
     * This auto-wired field connects the {@link QuoteClient} to the
     * {@link ZippyQuoteAPI} that exchanges HTTP requests with the
     * {@code ZippyApplication} microservice asynchronously.
     */
    @Autowired
    private ZippyQuoteAPI mZippyQuoteAPI;

    /**
     * Get a {@link Flux} that emits the requested quotes.
     *
     * @param routename The service that will perform the request
     * @return A {@link Flux} that emits all the {@link Quote}
     *         objects
     */
    public Flux<Quote> getAllQuotes(String routename) {
        if (routename.equals(HANDEY))
            return mHandeyQuoteAPI
            // Forward to the Handey proxy.
            .getAllQuotes();
        else
            return mZippyQuoteAPI
            // Forward to the Zippy proxy.
            .getAllQuotes();
    }

    /**
     * Get a {@link List} that emits the requested quotes.
     *
     * @param routename The service that will perform the request
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link Flux} that emits the requested {@link Quote}
     *         objects
     */
    public Flux<Quote> postQuotes(String routename,
                                  List<Integer> quoteIds) {
        if (routename.equals(HANDEY))
            return mHandeyQuoteAPI
                // Forward to the Handey proxy.
                .postQuotes(quoteIds);
        else
            return mZippyQuoteAPI
                // Forward to the Zippy proxy.
                .postQuotes(quoteIds);
    }

    /**
     * Search for quotes containing any {@code queries} in the given
     * {@link List} of {@code queries}.
     *
     * @param routename The service that will perform the request
     * @param queries The {@link List} of queries to search for
     * @return A {@link Flux} that emits {@link Quote} objects that
     *         match any {@code queries}
     */
    public Flux<Quote> searchQuotes(String routename,
                                    List<String> queries) {
        if (routename.equals(HANDEY))
            return mHandeyQuoteAPI
                // Forward to the Handey proxy.
                .search(queries);
        else
            return mZippyQuoteAPI
                // Forward to the Zippy proxy.
                .search(queries);
    }

    /**
     * Search for quotes containing all {@code queries} in the given
     * {@link List} of {@code queries} on the Zippy microservice
     * using a custom SQL method.
     *
     * @param queries The {@link List} of queries to search for
     * @return A {@link Flux} that emits {@link Quote} objects that
     *         match with all the {@code queries}
     */
    public Flux<Quote> searchQuotesEx(String routename,
                                      List<String> queries) {
        if (routename.equals(HANDEY))
            return mHandeyQuoteAPI
                // Forward to the Handey proxy.
                .searchEx(queries);
        else
            return mZippyQuoteAPI
                // Forward to Zippy proxy.
                .searchEx(queries);
        
    }
}
