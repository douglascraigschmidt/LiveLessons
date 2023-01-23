package berraquotes.server.strategies;

import berraquotes.common.Quote;
import berraquotes.server.strategies.BQAbstractStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This strategy uses the Java parallel streams framework to provide
 * Berra quotes.
 */
public class BQParallelStreamStrategy
       extends BQAbstractStrategy {
    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link List} of all requested {@link Quote} objects
     */
    public List<Quote> getQuotes(List<Quote> quotes,
                                 List<Integer> quoteIds) {
        return quoteIds
            // Convert the List to a parallel Stream.
            .parallelStream()

            // Get the quote associated with the quoteId.
            .map(quotes::get)

            // Trigger intermediate operations and collect the results
            // into a List.
            .toList();
    }

    /**
     * Search for Berra quotes containing the given query {@link
     * String}.
     *
     * @param query The search query
     * @return A {@link List} of {@link Quote} objects containing the
     *         query
     */
    public List<Quote> search(List<Quote> quotes,
                              String query) {
        // Locate all quotes whose 'quote' matches the 'query' and
        // return them as a List of Quote objects.

        return quotes
            // Convert the List to a Stream.
            .parallelStream()

            // Locate all the matches.
            .filter(quote -> quote.quote().toLowerCase()
                    .contains(query.toLowerCase()))

            // Convert the Stream to a List.
            .toList();
    }

    /**
     * Search for quotes containing the given {@link String} queries
     * and return a {@link List} of matching {@link Quote} objects.
     *
     * @param queries The search queries
     * @return A {@code List} of quotes containing {@link Quote}
     *         objects matching the given {@code queries}
     */
    public List<Quote> search(List<Quote> quotes, List<String> queries) {
        return quotes
            // Convert the List to a Stream.
            .parallelStream()

            .filter(quote -> queries
                    .parallelStream()

                    // Locate any matches.
                    .anyMatch(query -> quote.quote().toLowerCase()
                              .contains(query.toLowerCase())))

            // Convert the Stream to a List.
            .toList();
    }
}
