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
        System.out.println("WEIRD");
        return getService()
            // Forward request to the service.
            .getAllQuotes();
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@code T} containing the {@link Quote} objects
     *         associated with the requested quotes
     */
    @PostMapping(POST_QUOTES)
    T postQuotes(@RequestBody List<Integer> quoteIds) {
        return getService()
            // Forward request to the service.
            .postQuotes(quoteIds);
    }

    /**
     * Search for quotes containing any of the given {@link String}
     * {@code queries} and return a {@link T} of matches.
     *
     * @param queries The search queries
     * @return A {@code T} containing the matching {@code queries}
     */
    @PostMapping(POST_SEARCHES)
    public T search(@RequestBody List<String> queries) {
        return getService()
            // Forward to the service.
            .search(queries);
    }

    /**
     * Search for quotes containing all of the given {@link String}
     * {@code queries} and return a {@link T} of matches.
     *
     * @param queries The search queries
     * @return A {@code T} containing the matching {@code queries}
     */
    @PostMapping(POST_SEARCHES_EX)
    public T searchEx(@RequestBody List<String> queries) {
        return getService()
            // Forward to the service.
            .searchEx(queries);
    }
}