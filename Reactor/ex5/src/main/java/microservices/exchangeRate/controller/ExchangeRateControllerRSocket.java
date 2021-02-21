package microservices.exchangerate.controller;

import ch.qos.logback.classic.Level;
import datamodels.CurrencyConversion;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import static utils.ReactorUtils.randomDelay;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle RSocket requests via reactive programming.  These requests
 * are mapped to method(s) that convert between various currencies
 * asynchronously.
 *
 * In Spring's approach to building RSocket services, message
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @MessageMapping}.  These components are
 * identified by the @Controller annotation below.
 *
 * WebFlux uses the {@code @MessageMapping} annotation to map RSocket
 * requests onto methods in the {@code ExchangeRateControllerRSocket}.
 */
@Controller
public class ExchangeRateControllerRSocket {
    /**
     * This method simulates a microservice that finds the exchange
     * rate between a source and destination currency format
     * asynchronously.
     *
     * WebFlux maps RSocket requests sent to the
     * /_queryForExchangeRate endpoint to this method.
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
            // Simply return a constant for now.
            .then(Mono.just(1.20));
    }
}
