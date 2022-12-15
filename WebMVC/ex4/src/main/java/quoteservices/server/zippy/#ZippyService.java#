package quoteservices.server.zippy;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import quoteservices.common.Components;
import quoteservices.common.ZippyQuote;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This class defines implementation methods that are called by the
 * {@link ZippyController}. ..
 *
 * This class is annotated as a Spring {@code @Service}, which
 * indicates this class implements "business logic" and enables the
 * auto-detection and wiring of dependent implementation classes via
 * classpath scanning.
 */
@Service
public class ZippyService {
    /**
     * An in-memory {@link List} of all the Zippy quotes.  The
     * {@code @Autowired} annotation ensures this field is initialized
     * via Spring's dependency injection facilities, where an object
     * receives other objects that it depends on, i.e., the {@link
     * List} of {@link ZippyQuote} objects from the {@link Components}
     * class.
     */
    @Autowired
    public List<ZippyQuote> mQuotes;

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return An {@link List} containing the requested {@link
     *         ZippyQuote} objects
     */
    public List<ZippyQuote> getQuote(List<Integer> quoteIds) {
        return quoteIds
            // Convert the List to a Stream.
            .stream()

            // Get the Zippy quote associated with the quoteId.
            .map(quoteId -> mQuotes.get(quoteId - 1))

            // Trigger intermediate operations and collect the results
            // into a List.
            .collect(toList());
    }
}
