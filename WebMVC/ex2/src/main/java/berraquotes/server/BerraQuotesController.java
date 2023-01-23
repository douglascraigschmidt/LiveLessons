package berraquotes.server;

import berraquotes.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
    ApplicationContext mApplicationContext;

    /**
     * Spring injected {@link BerraQuotesService}.
     */
    @Autowired
    private BerraQuotesService mService;

    /**
     * @param strategy Which implementation strategy to forward the
     *                 request to
     * @return A {@link List} of all {@link Quote} objects
     */
    @GetMapping(GET_ALL_QUOTES)
    public List<Quote> getAllQuotes(Integer strategy) {
        return mService
            // Forward request to the service.
            .getAllQuotes(strategy);
    }

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param strategy Which implementation strategy to forward the
     *                request to
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return A {@link List} of all requested {@link Quote} objects
     */
    @GetMapping(GET_QUOTES)
    List<Quote> getQuotes(Integer strategy,
                          @RequestParam List<Integer> quoteIds) {
        return mService
            // Forward request to the service.
            .getQuotes(strategy, quoteIds);
    }

    /**
     * Search for quotes containing the given query {@link String}.
     *
     * @param strategy Which implementation strategy to forward the
     *                request to
     * @param query The search query
     * @return A {@link List} of {@link Quote} objects containing the
     *         query
     */
    @GetMapping(GET_SEARCH + SEARCH_QUERY)
    public List<Quote> search(Integer strategy,
                              @PathVariable String query) {
        return mService
            // Forward to the service.
            .search(strategy, query);
    }

    /**
     * Search for quotes containing the given {@link String} queries
     * and return a {@link List} of matching {@link Quote} objects.
     *
     * @param strategy Which implementation strategy to forward the
     *                 request to
     * @param queries The search queries
     * @return A {@code List} of quotes containing {@link Quote}
     *         objects matching the given {@code queries}
     */
    @GetMapping(GET_SEARCHES)
    public List<Quote> search(Integer strategy,
                              @RequestParam List<String> queries) {
        return mService
            // Forward to the service.
            .search(strategy, queries);
    }
}
