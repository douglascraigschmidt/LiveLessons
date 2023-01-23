package berraquotes.server;

import berraquotes.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.http.HttpResponse;
import java.util.List;

import static berraquotes.common.Constants.EndPoint.*;

/**
 * This Spring controller demonstrates how WebMVC can be used to
 * handle HTTP GET requests.  These requests are mapped to endpoint
 * handler methods that return quotes from Yogi Berra.
 *
 * In Spring's approach to building RESTful web services, HTTP
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @GetMapping}, {@code @PostMapping}, {@code @PutMapping} and
 * {@code @DeleteMapping}, which correspond to the HTTP GET, POST,
 * PUT, and DELETE calls, respectively.  These components are
 * identified by the {@code @RestController} annotation below.
 *
 * WebMVC uses the {@code @GetMapping} annotation to map HTTP GET
 * requests onto methods in the {@link BerraQuotesController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser or
 * client app) or command-line utility (e.g., Curl or Postman).
 *
 * The {@code @RestController} annotation also tells a controller that
 * the object returned is automatically serialized into JSON and
 * passed back within the body of an {@link HttpResponse} object.
 */
@RestController
public class BerraQuotesController {
    /**
     * Defines a central read-only context.
     */
    @Autowired
    ApplicationContext applicationContext;

    /**
     * Spring injected {@link BerraQuotesService}.
     */
    @Autowired
    private BerraQuotesService service;

    /**
     * @return A {@link List} of all {@link Quote} objects
     */
    @GetMapping(GET_ALL_QUOTES)
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
    @GetMapping(GET_QUOTES)
    List<Quote> getQuotes(@RequestParam List<Integer> quoteIds) {
        return service
            // Forward request to the service.
            .getQuotes(quoteIds);
    }

    /**
     * Search for quotes containing the given query {@link String}.
     *
     * @param query The search query
     * @return A {@link List} of {@link Quote} objects containing the
     *         query
     */
    @GetMapping(GET_SEARCH + SEARCH_QUERY)
    public List<Quote> search(@PathVariable String query) {
        return service
            // Forward to the service.
            .search(query);
    }
}
