package berraquotes.client;

import berraquotes.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@code BerraQuoteController}
 * microservice to request Yogi Berra quotes.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class BerraQuotesClient {
    /**
     * This auto-wired field connects the {@link BerraQuotesClient} to
     * the {@link BerraQuotesProxy} that performs HTTP requests
     * synchronously.
     */
    @Autowired
    private BerraQuotesProxy mQuoteProxy;

    /**
     * @param strategy The quote checking strategy to use
     * @return An {@link List} containing all {@link
     *         Quote} objects
     */
    public List<Quote> getAllQuotes(int strategy) {
        return mQuoteProxy
            // Forward to the proxy.
            .getAllQuotes(strategy);
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param strategy The quote checking strategy to use
     * @param quoteIds A {@link List} containing the given
     *                 {@code quoteIds}
     * @return An {@link List} containing the requested {@link
     *         Quote} objects
     */
    public List<Quote> getQuotes(int strategy, List<Integer> quoteIds) {
        return mQuoteProxy
            // Forward to the proxy.
            .getQuotes(strategy, quoteIds);
    }

    /**
     * Get a {@link List} that contains quotes that match the
     * {@code query}.
     *
     * @param strategy The quote checking strategy to use
     * @param query A {@link String} to search for
     * @return An {@link List} containing matching {@link
     *         Quote} objects
     */
    public List<Quote> searchQuotes(int strategy, String query) {
        return mQuoteProxy
            // Forward to the proxy.
            .searchQuotes(strategy, query);
    }

    /**
     * Get a {@link List} that contains quotes that match the
     * {@code queries}.
     *
     * @param strategy The quote checking strategy to use
     * @param queries A {@link List} of {@link String} queries to
     *        search for
     * @return An {@link List} containing matching {@link
     *         Quote} objects
     */
    public List<Quote> searchQuotes(int strategy, List<String> queries) {
        return mQuoteProxy
            // Forward to the proxy.
            .searchQuotes(strategy, queries);
    }
}
