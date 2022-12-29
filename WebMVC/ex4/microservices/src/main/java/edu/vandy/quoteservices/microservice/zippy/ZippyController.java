package edu.vandy.quoteservices.microservice.zippy;

import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.EndPoint.GET_ALL_QUOTES;
import static edu.vandy.quoteservices.common.Constants.EndPoint.GET_QUOTES;

/**
 * This Spring controller demonstrates how Spring WebMVC can be used
 * to handle HTTP GET requests.
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
        // Indicate the request succeeded.
        // and return the application name.
        return ResponseEntity
            .ok(applicationContext.getId() + " is alive\n");
    }

    /**
     * Returns all movie titles in the database.
     *
     * @return A list of all movie titles in the database
     */
    @GetMapping(GET_ALL_QUOTES)
    public List<Quote> getAllQuotes() {
        return mService
            // Delegate request to the service.
            .getAllQuotes();
    }

    @GetMapping(GET_QUOTES)
    List<Quote> getQuotes(@RequestParam List<Integer> quoteIds) {
        System.out.println("getQuotes()");

        return mService
            .getQuotes(quoteIds);
    }
}
