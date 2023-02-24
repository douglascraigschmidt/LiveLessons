package edu.vandy.pubsub.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static edu.vandy.pubsub.common.Constants.EndPoint.*;

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
    public class PublisherController {
        /**
         * The publisher that performs the HTTP requests.
         */
        @Autowired
        PublisherService mService;

        /**
         * This method initializes the publisher.
         *
         * WebFlux maps HTTP POST requests sent to the /_create endpoint to
         * this method.
         *
         * @param count The number of {@link Integer} objects to generate
         * @param maxValue The maximum value of the generated {@link Integer} objects
         * @return An empty {@link Mono}.
         */
        @PostMapping(POST_CREATE)
        public Mono<Void> create(@RequestParam int count,
                                 @RequestParam int maxValue) {
            // Create a new publisher.
            mService.create(count, maxValue);

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
         * @param backpressureEnabled True if backpressure is enabled, else false
         * @return A {@link Flux} stream of random {@link Integer} objects
         */
        @GetMapping(GET_START)
        public Flux<Integer> start(@RequestParam Boolean backpressureEnabled) {
            // Forward to the service.
            return mService.start(backpressureEnabled);
        }

        /**
         * This method stops publishing the flux stream of random
         * integers.
         *
         * WebFlux maps HTTP DELETE requests sent to the /_stop endpoint
         * to this method.
         *
         * @return An empty {@link Mono}.
         */
        @DeleteMapping(DELETE_STOP)
        public Mono<Void> stop() {
            // Forward to the service.
            return mService.stop();
        }
    }
