package quoteservices.server.handey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import quoteservices.common.Options;
import quoteservices.common.HandeyQuote;

import java.net.http.HttpResponse;
import java.util.List;

import static quoteservices.common.Constants.EndPoint.*;

/**
 * This Spring controller demonstrates how Spring WebMVC can be used
 * to handle HTTP GET requests.
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
 * requests onto methods in the {@link GCDController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser or
 * client app) or command-line utility (e.g., Curl or Postman).
 *
 * The {@code @RestController} annotation also tells a controller that
 * the object returned is automatically serialized into JSON and passed
 * back within the body of an {@link HttpResponse} object.
 */
@RestController
public class HandeyController {
    /**
     * This auto-wired field connects the {@link HandeyController} to
     * the {@link HandeyService}.
     */
    @Autowired
    HandeyService mService;

    /**
     * Get a {@link List} that contains the requested quotes.
     *
     * @param quoteIds A {@link List} containing the given random
     *                 {@code quoteIds}
     * @return An {@link List} containing the requested {@link
     *         HandeyQuote} objects
     */
    @GetMapping(GET_HANDEY_QUOTE)
    public List<HandeyQuote> getQuote
        (@RequestParam List<Integer> quoteIds) {
        Options.debug("Handey.getQuote()");

        return mService
            // Forward to the service.
            .getQuote(quoteIds);
    }
}
