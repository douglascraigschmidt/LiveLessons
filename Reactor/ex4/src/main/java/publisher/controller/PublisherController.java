package publisher.controller;

import org.springframework.web.bind.annotation.*;
import publisher.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET, POST, and DELETE requests via asynchronous
 * reactive programming.  These requests are mapped to methods that
 * create, start, and stop generating random integers.
 *
 * In Spring's approach to building RESTful web services, HTTP
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @GetMapping}, {@code @PostMapping}, {@code @PutMapping} and
 * {@code @DeleteMapping}, which correspond to the HTTP GET, POST,
 * PUT, and DELETE calls, respectively.  These components are
 * identified by the @RestController annotation below.
 *
 * WebFlux uses the {@code @GetMapping} annotation to map HTTP GET
 * requests onto methods in the {@code PublisherController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser) or
 * command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/publisher")
public class PublisherController {
    /**
     * The publisher that performs the HTTP requests.
     */
    Publisher mPublisher;

    /**
     * This method initializes the publisher.
     *
     * WebFlux maps HTTP POST requests sent to the /_create endpoint to
     * this method.
     *
     * @return An empty mono.
     */
    @PostMapping("/_create")
    public Mono<Void> createPublisher(@RequestParam int count,
                                      @RequestParam int maxValue) {
        // Create a new publisher.
        mPublisher = new Publisher(count, maxValue);

        // Return an empty mono.
        return Mono.empty();
    }

    /**
     * This method starts publishing the flux stream of random
     * integers.
     *
     * WebFlux maps HTTP GET requests sent to the /_start endpoint to
     * this method.
     *
     * @return A flux stream of random integers.
     */
    @GetMapping("/_start")
    public Flux<Integer> startPublishing(@RequestParam Boolean backpressureEnabled) {
        // Forward to the publish() method.
        return mPublisher.publish(backpressureEnabled);
    }

    /**
     * This method stops publishing the flux stream of random
     * integers.
     *
     * WebFlux maps HTTP DELETE requests sent to the /_stop endpoint
     * to this method.
     *
     * @return An empty mono.
     */
    @DeleteMapping("/_stop")
    public Mono<Void> stopPublishing() {
        // Forward to the dispose() method.
        return mPublisher.dispose();
    }
}
