package edu.vandy.berraquotes;

import edu.vandy.berraquotes.model.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * A ...
 */
@RestController
public class BerraController {
    /**
     * @@ Monte, is this field required?
     */
    @Autowired
    ApplicationContext applicationContext;

    /**
     * Spring injected {@link BerraService}.
     */
    @Autowired
    private BerraService service;

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
    @GetMapping("all-quotes")
    public List<Quote> getAllQuotes() {
        return service
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
    @GetMapping("quotes")
    List<Quote> getQuotes(@RequestParam List<Integer> quoteIds) {
        return service
            // Forward request to the service.
            .getQuotes(quoteIds);
    }
}
