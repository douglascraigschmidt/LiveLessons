package quoteservices.client;

import org.springframework.beans.factory.annotation.Autowired;
import quoteservices.common.ZippyQuote;
import quoteservices.common.HandeyQuote;
import quoteservices.server.zippy.ZippyController;
import quoteservices.server.handey.HandeyController;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@link ZippyController} and {@link
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
     * {@link ZippyProxy} that performs HTTP requests synchronously.
     */
    @Autowired
    private ZippyProxy mZippyProxy;

    /**
     * This auto-wired field connects the {@link QuoteClient} to the
     * {@link HandeyProxy} that performs HTTP requests synchronously.
     */
    @Autowired
    private HandeyProxy mHandeyProxy;

    /**
     * Spring WebMVC maps HTTP GET requests sent to the {@code
     * GET_HANDEY_QUOTE} endpoint to this method.
     *
     * @return An {@link List} of {@link HandeyQuote} objects
     */
    public List<HandeyQuote> getHandeyQuotes(List<Integer> quoteIds) {
        return mHandeyProxy
            // Forward to the proxy.
            .getHandleQuotes(quoteIds);
    }

    /**
     * Spring WebMVC maps HTTP GET requests sent to the {@code
     * GET_ZIPPY_QUOTE} endpoint to this method.
     *
     * @return A {@link List} of {@link ZippyQuote} objects
     */
    public List<ZippyQuote> getZippyQuotes(List<Integer> quoteIds) {
        return mZippyProxy
            // Forward to the proxy.
            .getZippyQuotes(quoteIds);
    }
}
