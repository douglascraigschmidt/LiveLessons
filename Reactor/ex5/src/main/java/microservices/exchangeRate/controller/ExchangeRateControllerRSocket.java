package microservices.exchangerate.controller;

import datamodels.CurrencyConversion;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static utils.ReactorUtils.randomDelay;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle RSocket requests via reactive programming.  These requests
 * are mapped to method(s) that convert between varous currencies
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
 * WebFlux uses the {@code @PostMapping} annotation to map HTTP POST
 * requests onto methods in the {@code ExchangeRateControllerAsync}.
 * POST requests invoked from any HTTP web client (e.g., a web
 * browser) or command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/exchangeRateRSocket")
public class ExchangeRateControllerRSocket {
    /**
     * This method simulates a microservice that finds the exchange
     * rate between a source and destination currency format
     * asynchronously.
     *
     * WebFlux maps RSocket requests sent to the /_exchangeRate
     * endpoint to this method.
     *
     * @param currencyConversionM A Mono that emits the currency to
     *        convert from and to
     * @return A Mono that emits the current exchange rate.
     */
    @MessageMapping("_queryForExchangeRate")
    private Mono<Double> queryForExchangeRate(Mono<CurrencyConversion> currencyConversionM) {
        // Delay for a random amount of time.
        randomDelay();

        return currencyConversionM
            // Debugging print.
            .doOnSuccess(currencyConversion -> System.out.println(currencyConversion.toString()))

            // Simply return a constant for now.
            .then(Mono.just(1.20));
    }
}
