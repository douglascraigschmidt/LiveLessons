package edu.vandy.mathservices.microservices.gcd;

import edu.vandy.mathservices.common.GCDResult;
import edu.vandy.mathservices.common.Options;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;
import java.util.List;

import static edu.vandy.mathservices.common.Constants.EndPoint.COMPUTE_GCD_LIST;

/**
 * This Spring controller demonstrates how WebMVC can be used to
 * handle HTTP GET requests via the Java structured concurrency
 * framework.  These requests are mapped to endpoint handler methods
 * that determine the Greatest Common Divisor (GCD) of large random
 * {@link Integer} objects.
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
public class GCDController {
    /**
     * This auto-wired field connects the {@link GCDController}
     * to the {@link GCDService}.
     */
    @Autowired
    GCDService mService;

    /**
     * Compute the GCD of the {@code integers} param.
     *
     * @param integers The {@link List} of {@link Integer} objects
     *                 upon which to compute the GCD
     * @return A {@link List} of {@link GCDResult} objects
     */
    @GetMapping(COMPUTE_GCD_LIST)
    public List<GCDResult> computeGCDs
        (@RequestParam List<Integer> integers) {
        Options.debug("computeGCDs()");

        return mService
                // Forward to the service.
                .computeGCDs(integers);
    }
}
