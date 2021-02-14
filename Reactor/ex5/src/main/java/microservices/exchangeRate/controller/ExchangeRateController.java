package microservices.exchangeRate.controller;

import datamodels.CurrencyConversion;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Random;

import static utils.ReactorUtils.randomDelay;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET requests via asynchronous reactive programming.
 * These requests are mapped to methods that convert between US
 * dollars and other currencies synchronously and asynchronously.
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
 * requests onto methods in the {@code ExchangeRateController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser) or
 * command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/exchangeRate")
public class ExchangeRateController {
    /**
     * This method simulates a microservice that finds the exchange
     * rate between a source and destination currency format.
     *
     * WebFlux maps HTTP GET requests sent to the /_exchangeRateAsync
     * endpoint to this method.
     *
     * @param currencyConversion Indicates the currency to convert
     *        from and to  
     * @return A Mono that emits the current exchange rate.
     */
    @GetMapping("_exchangeRateAsync")
    private Mono<Double> queryExchangeRateForAsync(CurrencyConversion currencyConversion) {
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

    /**
     * This method simulates a microservice that finds the exchange
     * rate between a source and destination currency format.
     *
     * WebFlux maps HTTP GET requests sent to the /_exchangeRateSync
     * endpoint to this method.
     *
     * @param currencyConversion Indicates the currency to convert
     *        from and to
     * @return A Double that contains the current exchange rate.
     */
    @GetMapping("_exchangeRateSync")
    private Double queryExchangeRateForSync(CurrencyConversion currencyConversion) {
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
        return 1.20;
    }
}
