package microservices.flightPrice.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Random;

import static utils.ReactorUtils.randomDelay;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET, POST, and DELETE requests via asynchronous
 * reactive programming.  These requests are mapped to methods that
 * return the cost of flight routes in US dollars synchronously and
 * asynchronously.
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
 * requests onto methods in the {@code FlightPriceController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser) or
 * command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/flightPrice")
public class FlightPriceController {
    /**
     * This method simulates a microservice that finds the best price
     * in US dollars for a given flight leg.
     *
     * WebFlux maps HTTP GET requests sent to the
     * /{rootDir}/_bestPrice endpoint to this method.
     *
     * @param flightLeg A String containing the flight leg.
     * @return A Mono that emits best price in US dollars for this price leg.
     */
    @GetMapping("/_bestPriceAsync")
    private Mono<Double> findBestPrice(@RequestParam String flightLeg) {
        // Delay for a random amount of time.
        randomDelay();
        
        /*
        // Debugging print.
        print("Flight leg is "
              + flightLeg);
        */

        // Simply return a constant.
        return Mono.just(888.00);
    }
}
