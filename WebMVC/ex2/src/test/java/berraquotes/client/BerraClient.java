package berraquotes.client;

import berraquotes.Quote;
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
public class BerraClient {
    /**
     * This auto-wired field connects the {@link BerraClient} to the
     * {@link BerraProxy} that performs HTTP requests synchronously.
     */
    @Autowired
    private BerraProxy mQuoteProxy;

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given
     *                 {@code quoteIds}
     * @return An {@link List} containing the requested {@link
     *         Quote} objects
     */
    public List<Quote> getQuotes(List<Integer> quoteIds) {
        return mQuoteProxy
            // Forward to the proxy.
            .getQuotes(quoteIds);
    }

    /**
     * @return An {@link List} containing all {@link
     *         Quote} objects
     */
    public List<Quote> getAllQuotes() {
        return mQuoteProxy
            // Forward to the proxy.
            .getAllQuotes();
    }

    /**
     * Get a {@link List} that contains quotes that match the {@code query}.
     *
     * @param query A {@link String} to search for
     * @return An {@link List} containing matching {@link
     *         Quote} objects
     */
    public List<Quote> searchQuotes(String query) {
        return mQuoteProxy
                // Forward to the proxy.
                .searchQuotes(query);
    }
}
