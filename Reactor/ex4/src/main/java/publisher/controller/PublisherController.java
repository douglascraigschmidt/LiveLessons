package publisher.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import publisher.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET requests via asynchronous reactive programming.
 * These GET requests are mapped to a method that generates random
 * numbers.
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
 * requests onto methods in the {@code FolderController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser)
 * or command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/publisher")
public class PublisherController {
    /**
     *
     */
    Publisher mPublisher;

    /**
     * This method...
     *
     * WebFlux maps HTTP GET requests sent to the /_create
     * endpoint to this method.
     *
     * @return ...
     */
    @GetMapping("/_create")
    public Flux<Integer> startPublishing() {
        mPublisher = new Publisher();

        return mPublisher.publish();
    }

    /**
     * This method...
     *
     * WebFlux maps HTTP DELETE requests sent to the /_delete
     * endpoint to this method.
     *
     * @return ...
     */
    @DeleteMapping("/_delete")
    public Mono<Void> stopPublishing() {
        return mPublisher.dispose();
    }
}
