package quoteservices.server.handey;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import quoteservices.common.Components;
import quoteservices.common.HandeyQuote;

import java.util.List;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toList;

/**
 * This class defines implementation methods that are called by the
 * {@link HandeyController}. ...
 *
 * This class is annotated as a Spring {@code @Service}, which
 * indicates this class implements "business logic" and enables the
 * auto-detection and wiring of dependent implementation classes via
 * classpath scanning.
 */
@Service
public class HandeyService {
    /**
     * An in-memory {@link List} of all the Handey quotes.  The
     * {@code @Autowired} annotation ensures this field is initialized
     * via Spring's dependency injection facilities, where an object
     * receives other objects that it depends on, i.e., the {@link
     * List} of {@link HandeyQuote} objects from the {@link
     * Components} class.
     */
    @Autowired
    public List<HandeyQuote> mQuotes;

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link List} of {@link HandeyQuote} objects
     */
    public List<HandeyQuote> getQuote(List<Integer> quoteIds) {
        return quoteIds
            // Convert the List to a Stream.
            .stream()

            // Get the Handey quote associated with the quoteId.
            .map(quoteId -> mQuotes.get(quoteId - 1))

            // Trigger intermediate operations and collect the results
            // into a List.
            .collect(toList());
    }
}
