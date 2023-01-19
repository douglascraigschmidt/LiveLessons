package edu.vandy.quoteservices.microservice.handey;

import edu.vandy.quoteservices.common.BaseController;
import edu.vandy.quoteservices.common.BaseService;
import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class defines implementation methods that are called by the
 * {@link BaseController}, which serves as the main "front-end" app
 * gateway entry point for remote clients that want to receive movie
 * recommendations.
 *
 * This class implements the abstract methods in {@link BaseService}
 * using the Java sequential streams framework.
 *
 * This class is annotated as a Spring {@code @Service}, which enables
 * the automatic detection and wiring of dependent implementation
 * classes via classpath scanning. It also includes its name in the
 * {@code @Service} annotation below so that it can be identified as a
 * service.
 */
@Service
public class HandeyService
    extends BaseService<List<Quote>> {
    /**
     * An in-memory {@link List} of all the quotes.
     */
    @Autowired
    private List<Quote> mQuotes;

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
    public List<Quote> getQuotes(List<Long> quoteIds) {
        return quoteIds
            // Convert the List to a Stream.
            .stream()

            // Get the Handey quote associated with the quoteId.
            .map(quoteId -> mQuotes.get(quoteId.intValue()))

            // Trigger intermediate operations and collect the results
            // into a List.
            .toList();
    }
}
