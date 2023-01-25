package berraquotes.server.strategies;

import berraquotes.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This strategy uses the Java sequential streams framework to provide
 * Berra quotes.
 */
public class BQSequentialStreamStrategy
       extends BQAbstractStrategy {
    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link List} of all requested {@link Quote} objects
     */
    public List<Quote> getQuotes(List<Integer> quoteIds) {
        return quoteIds
            // Convert the List to a Stream.
            .stream()

            // Get the quote associated with the quoteId.
            .map(mQuotes::get)

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
    public List<Quote> search(String query) {
        // Locate all quotes whose 'quote' matches the 'query' and
        // return them as a List of Quote objects.

        return mQuotes
            // Convert the List to a Stream.
            .stream()

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
    public List<Quote> search(List<String> queries) {
        return mQuotes
            // Convert the List to a Stream.
            .stream()

            .filter(quote -> queries
                    // Convert the List to a Stream.
                    .stream()

                    // Locate any matches.
                    .anyMatch(query -> quote.quote().toLowerCase()
                              .contains(query.toLowerCase())))

            // Convert the Stream to a List.
            .toList();
    }
}
