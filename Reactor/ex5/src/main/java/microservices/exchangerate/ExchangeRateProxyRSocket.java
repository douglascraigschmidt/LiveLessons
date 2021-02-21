package microservices.exchangerate;

import datamodels.CurrencyConversion;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

/**
 * This class serves as a proxy to the asynchronous ExchangeRate
 * microservice that returns the current exchange that converts one
 * currency to another.
 */
public class ExchangeRateProxyRSocket {
    /**
     * The URI that denotes a remote method to determine the current
     * exchange rate asynchronously.
     */
    private final String mQueryExchangeRateURIAsync =
        "/microservices/exchangeRateRSocket/_queryForExchangeRate";

    private final Mono<RSocketRequester> rSocketRequester;

    /**
     * Constructor initializes the super class.
     */
    public ExchangeRateProxyRSocket() {
        rSocketRequester = RSocketRequester.builder()
                  .rsocketConnector(rSocketConnector -> rSocketConnector
                                    .reconnect(Retry.fixedDelay(2,
                                                                Duration.ofSeconds(2))))
                  .dataMimeType(MediaType.APPLICATION_CBOR)
                  .rsocketStrategies(RSocketStrategies.builder()
                                     .encoders(encoders ->
                                               encoders.add(new Jackson2CborEncoder()))
                                     .decoders(decoders ->
                                               decoders.add(new Jackson2CborDecoder()))
                                     .build())
                  .connectTcp("localhost", 8081);
    }

 /**
     * Finds the exchange rate for the {@code sourceAndDestination}
     * asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the
     *                  operation
     * @param currencyConversion The currency to convert from and to
     * @return A Mono containing the exchange rate.
     */
    public Mono<Double> queryForExchangeRate(Scheduler scheduler,
                                             CurrencyConversion currencyConversion) {
        // Return a mono to the exchange rate.
        return Mono
            .fromCallable(() -> rSocketRequester
                          .map(r -> r
                               .route("_queryForExchangeRate")
                               .data(currencyConversion))
                          .flatMap(r -> r.retrieveMono(Double.class)))
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Mono<Double>.
            .flatMap(Function.identity());

        /*
        // Update the cache with the latest rate.
        .flatMap(latestRate -> updateCachedRate(latestRate, currencyConversion))

        // If this computation runs for more than the configured
        // number of seconds return the last cached rate.
        .timeout(Options.instance().exchangeRateTimeout(),
        getLastCachedRate(currencyConversion));
        */
    }
}
