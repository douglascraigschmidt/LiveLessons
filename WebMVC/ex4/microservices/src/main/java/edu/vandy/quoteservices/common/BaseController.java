package edu.vandy.quoteservices.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.GET_ALL_QUOTES;
import static edu.vandy.quoteservices.common.Constants.EndPoint.GET_QUOTES;

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
    // @@ Monte, where does this service field get set?
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
        // name.
        return ResponseEntity
            .ok(applicationContext.getId() + " is alive\n");
    }

    /**
     * @return A {@link List} of all {@link Quote} objects
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
     * @return A {@link List} of all requested {@link Quote} objects
     */
    @GetMapping(GET_QUOTES)
    T getQuotes(@RequestParam List<Integer> quoteIds) {
        return getService()
            // Forward request to the service.
            .getQuotes(quoteIds);
    }
}
