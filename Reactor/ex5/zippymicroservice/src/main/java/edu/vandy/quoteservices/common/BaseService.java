package edu.vandy.quoteservices.common;

import java.util.List;

/**
 * This interface defines the methods that are called by the
 * {@link BaseController}, which serves as the main "front-end" app
 * gateway entry point for remote clients that want to receive quotes.
 *
 * This interface is used to collate common functionality of all
 * Service implementations and to enable a common interface for the
 * {@link BaseController} to delegate to.
 *
 * Any class that implements this interface should be annotated as a
 * Spring {@code @Service}, which enables the automatic detection and
 * wiring of dependent implementation classes via classpath scanning.
 */
public interface BaseService<T> {
    /**
     * @return A {@link T} of all {@link Quote} objects
     */
    T getAllQuotes();

    /**
     * Get a {@link T} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@code T} containing the {@link Quote} objects
     *         associated with the requested quotes
     */
    T postQuotes(List<Integer> quoteIds);

    /**
     * Search for quotes containing any of the given {@link String}
     * queries and return a {@link T} of matches.
     *
     * @param queries The search queries
     * @return A {@code T} of quotes containing the given {@code
     *         queries}
     */
    T search(List<String> queries);

    /**
     * Search for quotes containing all of the given {@link String}
     * {@code queries} and return a {@link T} of matches.
     *
     * @param queries The search queries
     * @return A {@code T} containing the matching {@code queries}
     */
    T searchEx(List<String> queries);
}