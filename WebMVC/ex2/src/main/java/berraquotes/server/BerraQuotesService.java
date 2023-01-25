package berraquotes.server;

import berraquotes.common.Quote;
import berraquotes.server.strategies.*;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This class defines implementation methods that are called by the
 * {@link BerraQuotesController} to return quotes from Yogi Berra.
 *
 * This class implements the abstract methods in {@link
 * BerraQuotesService} using the Java sequential streams framework.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning.  It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class BerraQuotesService {
    /**
     * This array contains concrete strategies whose methods are
     * implemented to provide Berra quotes.  The order of these
     * strategies matter in this array.
     */
    BQAbstractStrategy[] mStrategy = {
            new BQStructuredConcurrencyStrategy(),
            new BQParallelStreamStrategy(),
            new BQParallelStreamRegexStrategy(),
            new BQSequentialStreamStrategy()
    };

    /**
     * @param strategy Which implementation strategy to forward the
     *                 request to
     * @return A {@link List} of all {@link Quote} objects
     */
    public List<Quote> getAllQuotes(Integer strategy) {
        return mStrategy[strategy]
            .getAllQuotes();
    }

    /**
     * Search for Berra quotes containing the given query {@link
     * String}.
     *
     * @param strategy Which implementation strategy to forward the
     *                 request to
     * @param query The search query
     * @return A {@link List} of {@link Quote} objects containing the
     *         query
     */
    public List<Quote> search(Integer strategy,
                              String query) {
        return mStrategy[strategy]
            .search(URLDecoder.decode(query,
                                     StandardCharsets.UTF_8));
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param strategy Which implementation strategy to forward the
     *                 request to
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link List} of all requested {@link Quote} objects
     */
    public List<Quote> getQuotes(Integer strategy,
                                 List<Integer> quoteIds) {
        return mStrategy[strategy]
            .getQuotes(quoteIds);
    }

    /**
     * Search for quotes containing the given {@link String} queries
     * and return a {@link List} of matching {@link Quote} objects.
     *
     * @param strategy Which implementation strategy to forward the
     *                 request to
     * @param queries The search queries
     * @return A {@code List} of quotes containing {@link Quote}
     *         objects matching the given {@code queries}
     */
    public List<Quote> search(Integer strategy,
                              List<String> queries) {
        return mStrategy[strategy]
            .search(queries);
    }
}
