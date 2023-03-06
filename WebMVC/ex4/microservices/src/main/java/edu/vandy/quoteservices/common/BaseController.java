package edu.vandy.quoteservices.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.*;

/**
 * A common controller implementation that redirects all requests to
 * custom quote services (e.g., Zippy, Handey, etc.) to perform.
 */
public abstract class BaseController<T> {
    /**
     * The central interface to provide configuration for the
     * application.  This field is read-only while the application is
     * running.
     */
    @Autowired
    ApplicationContext applicationContext;

    // The service to delegate requests.
    @Autowired
    BaseService<T> mService;

    /**
     * @return The {@link BaseService} encapsulated by the controller
     */
    public BaseService<T> getService() {
        return mService;
    }

    /**
     * @return A {@code T} containing all the quotes
     */
    @GetMapping(GET_ALL_QUOTES)
    public T getAllQuotes() {
        return getService()
            // Forward request to the service.
            .getAllQuotes();
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @param parallel Run the queries in parallel if true, else run
     *                 sequentially
     * @return A {@code T} containing the requested quotes
     */
    @PostMapping(POST_QUOTES)
    T postQuotes(@RequestBody List<Integer> quoteIds,
                 Boolean parallel) {
        return getService()
            // Forward request to the service.
            .postQuotes(quoteIds, parallel);
    }

    /**
     * Search for quotes containing the given {@link String} queries
     * and return a {@link T} of matches.
     *
     * @param queries The search queries
     * @param parallel Run the queries in parallel if true, else run
     *                 sequentially
     * @return A {@code T} containing the queries
     */
    @PostMapping(POST_SEARCHES)
    public T search(@RequestBody List<String> queries,
                    Boolean parallel) {
        System.out.println("search");

        return getService()
            // Forward to the service.
            .search(queries, parallel);
    }


    /**
     * Search for quotes containing the given {@link String} queries
     * and return a {@link List<Quote>} of matches using a custom
     * SQL query.
     *
     * @param queries The search queries
     * @return A {@code T} containing the queries
     */
    @PostMapping(POST_SEARCHES_EX)
    public T searchEx(@RequestBody List<String> queries,
                      Boolean parallel) {
        // Use a custom SQL query to find all movies whose 'id'
        // matches the List of 'queries' and return them as a List of
        // Quote objects that contain no duplicates.
        return getService()
            // Forward to the service.
            .searchEx(queries, parallel);
    }
}
