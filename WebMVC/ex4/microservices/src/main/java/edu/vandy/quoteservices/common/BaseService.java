package edu.vandy.quoteservices.common;

import java.util.List;

/**
 * This abstract class defines the methods that are called by the
 * {@link BaseController}, which serves as the main "front-end" app
 * gateway entry point for remote clients that want to receive quotes.
 *
 * This abstract class is used to collate common functionality of all
 * Service implementations and to enable a common interface for the
 * {@link BaseController} to delegate to.
 *
 * Any class that extends this abstract class should be annotated as a
 * Spring {@code @Service}, which enables the automatic detection and
 * wiring of dependent implementation classes via classpath scanning.
 */
public abstract class BaseService<T> {
    /**
     * @return A {@link List} of all {@link Quote} objects
     */
    public abstract T getAllQuotes();

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link List} of all requested {@link Quote} objects
     */
    public abstract T getQuotes(List<Long> quoteIds);
}
