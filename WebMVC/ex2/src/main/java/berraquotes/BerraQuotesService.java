package berraquotes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * An in-memory {@link List} of all the quotes.
     */
    @Autowired
    List<Quote> mQuotes;

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
    public List<Quote> getQuotes(List<Integer> quoteIds) {
        return quoteIds
            // Convert the List to a Stream.
            .stream()

            // Get the Handey quote associated with the quoteId.
            .map(quoteId -> mQuotes.get(quoteId - 1))

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
}
