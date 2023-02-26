package edu.vandy.pubsub.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static edu.vandy.pubsub.common.Constants.EndPoint.*;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET and DELETE requests via asynchronous
 * reactive programming.  These requests are mapped to methods that
 * start and stop generating random integers.
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
         * Publish a stream of {@code count} random {@link Integer}
         * objects within the range of {@code maxValue} - {@code count}
         * to {@code maxValue}.
         *
         * @param count The number of random {@link Integer} objects to generate
         * @param maxValue The max value of the random {@link Integer} objects
         * @return Return a {@link Flux} that publishes {@code count}
         *         random {@link Integer} objects
         *
         */
        @GetMapping(value = GET_START /*, produces = MediaType.APPLICATION_NDJSON_VALUE */)
        public Flux<Integer> start(Integer count,
                                   Integer maxValue,
                                   Boolean backpressureEnabled) {
            return mService
                // Forward to the service.
                .start(count,
                       maxValue,
                       backpressureEnabled);
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
