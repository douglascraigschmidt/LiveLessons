package edu.vandy.quoteservices.microservice;

import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.*;

/**
 * This Spring controller demonstrates how Spring WebFlux can be used
 * to handle HTTP GET and POST requests asynchronously.
 *
 * The {@code @RestController} annotation is a specialization of
 * {@code @Component} and is automatically detected through classpath
 * scanning.  It adds the {@code @Controller} and
 * {@code @ResponseBody} annotations. It also converts responses to
 * JSON or XML.
 */
@RestController
public class ZippyController {
    /**
     * The central interface to provide configuration for the
     * application.  This field is read-only while the application is
     * running.
     */
    @Autowired
    ApplicationContext applicationContext;

    // The service to delegate requests.
    @Autowired
    ZippyService mService;

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
     * @return A {@link Flux< Quote >} containing all the quotes
     */
    @GetMapping(GET_ALL_QUOTES)
    public Flux<Quote> getAllQuotes() {
        System.out.println("NORMAL");
        return mService
            // Forward request to the service.
            .getAllQuotes();
    }

    /**
     * Get a {@link Flux<Quote>} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link Flux<Quote>} containing the {@link Quote} objects
     *         associated with the requested quotes
     */
    @PostMapping(POST_QUOTES)
    Flux<Quote> postQuotes(@RequestBody List<Integer> quoteIds) {
        return mService
            // Forward request to the service.
            .postQuotes(quoteIds);
    }

    /**
     * Search for quotes containing any of the given {@link String}
     * {@code queries} and return a {@link Flux<Quote>} of matches.
     *
     * @param queries The search queries
     * @return A {@link Flux<Quote>} containing the matching {@code queries}
     */
    @PostMapping(POST_SEARCHES)
    public Flux<Quote> search(@RequestBody List<String> queries) {
        return mService
            // Forward to the service.
            .search(queries);
    }

    /**
     * Search for quotes containing all of the given {@link String}
     * {@code queries} and return a {@link Flux<Quote>} of matches.
     *
     * @param queries The search queries
     * @return A {@link Flux<Quote>} containing the matching {@code queries}
     */
    @PostMapping(POST_SEARCHES_EX)
    public Flux<Quote> searchEx(@RequestBody List<String> queries) {
        return mService
            // Forward to the service.
            .searchEx(queries);
    }
}
