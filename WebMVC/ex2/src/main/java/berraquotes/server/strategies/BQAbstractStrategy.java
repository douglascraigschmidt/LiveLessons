package berraquotes.server.strategies;

import berraquotes.common.Quote;

import java.util.List;

import berraquotes.common.ServerBeans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This abstract class defines methods used by various Berra quote
 * implementation strategies.
 */
public abstract class BQAbstractStrategy {
    /**
     * Initialize the List of {link Quote} objects.
     */
    List<Quote> mQuotes = ServerBeans.getQuotes();

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
     * @return A {@link List} of all requested {@link Quote} objects
     */
    public abstract List<Quote> getQuotes(List<Integer> quoteIds);

    /**
     * Search for Berra quotes containing the given query {@link
     * String}.
     *
     * @param query The search query
     * @return A {@link List} of {@link Quote} objects containing the
     *         query
     */
    public abstract List<Quote> search(String query);

    /**
     * Search for quotes containing the given {@link String} queries
     * and return a {@link List} of matching {@link Quote} objects.
     *
     * @param queries The search queries
     * @return A {@code List} of quotes containing {@link Quote}
     *         objects matching the given {@code queries}
     */
    public abstract List<Quote> search(List<String> queries);
}
