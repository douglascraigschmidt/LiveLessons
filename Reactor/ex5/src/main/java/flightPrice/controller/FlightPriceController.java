package flightPrice.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Random;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET, POST, and DELETE requests via asynchronous
 * reactive programming.  These requests are mapped to methods that
 * return (1) costs of flight routes in US dollars and (2) convert
 * between US dollars and other currencies.
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
 * requests onto methods in the {@code FlightController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser) or
 * command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/flightPrice")
public class FlightPriceController {
    /**
     * The random number generator.
     */
    private static final Random sRandom = new Random();

    /**
     * Simulate a random delay between 0.5 and 4.5 seconds.
     */
    private static void randomDelay() {
        int delay = 500 + sRandom.nextInt(4000);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method simulates a microservice that finds the best price
     * in US dollars for a given flight leg.
     *
     * WebFlux maps HTTP GET requests sent to the
     * /{rootDir}/_bestPrice endpoint to this method.
     *
     * @param flightLeg A String containing the flight leg.
     * @return The best price in US dollars for this price leg.
     */
    @GetMapping("/_bestPrice")
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

    /**
     * This method simulates a microservice that finds the exchange
     * rate between a source and destination currency format.
     *
     * WebFlux maps HTTP GET requests sent to the
     * /{rootDir}/_exchangeRate endpoint to this method.
     *
     * @param sourceAndDestination A String containing source and destination currencies.
     * @return The current exchange rate.
     */
    @GetMapping("_exchangeRate")
    private Mono<Double> queryExchangeRateFor(String sourceAndDestination) {
        String[] sAndD = sourceAndDestination.split(":");

        // Delay for a random amount of time.
        randomDelay();

        // Debugging print.
        /*
        print("Rate comparision between " 
              + sAndD[0]
              + " and "
              + sAndD[1]);
        */

        // Simply return a constant.
        return Mono.just(1.20);
    }
}
