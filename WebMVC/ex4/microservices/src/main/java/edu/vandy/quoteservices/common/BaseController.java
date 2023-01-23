package edu.vandy.quoteservices.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    BaseService<T> service;

    /**
     * @return The {@link BaseService} encapsulated by the controller
     */
    public BaseService<T> getService() {
        return service;
    }

    /**
     * A request for testing Eureka connection.
     *
     * @return The application name
     */
    @GetMapping({"/", "actuator/info"})
    public ResponseEntity<String> info() {
        // Indicate the request succeeded and return the application
        // name and thread id.
        return ResponseEntity
            .ok(applicationContext.getId() 
                + " is alive and running at "
                + Thread.currentThread()
                + "\n");
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
    @GetMapping(GET_QUOTES)
    T getQuotes(@RequestParam List<Long> quoteIds,
                Boolean parallel) {
        return getService()
            // Forward request to the service.
            .getQuotes(quoteIds, parallel);
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
    @GetMapping(GET_SEARCHES)
    @Transactional(readOnly = true)
    public T search(@RequestParam List<String> queries,
                    Boolean parallel) {
        return service
            // Forward to the service.
            .search(queries, parallel);
    }
}
