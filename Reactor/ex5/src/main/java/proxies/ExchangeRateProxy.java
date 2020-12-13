package proxies;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.function.Function;

/**
 * This class serves as a proxy to the ExchangeRate microservice.
 */
public class ExchangeRateProxy {
    /**
     * The URI that determines the current exchange rate.
     */
    private final String mQueryExchangeRateURIM = "/microservices/exchangeRate/_exchangeRateM";

    /**
     * The WebClient provides the means to access the ExchangeRate
     * microservice.
     */
    private final WebClient mExchangeRate;

    /**
     * Host/post where the server resides.
     */
    private final String mSERVER_BASE_URL =
        "http://localhost:8081";

    /**
     * Constructor initializes the fields.
     */
    public ExchangeRateProxy() {
        mExchangeRate = WebClient
            // Start building.
            .builder()

            // The URL where the server is running.
            .baseUrl(mSERVER_BASE_URL)

            // Build the webclient.
            .build();
    }

    /**
     * Finds the exchange rate for the {@code sourceAndDestination} asynchronously.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @param sourceAndDestination The source currency and the destination currency
     * @return A Mono containing the exchane rate.
     */
    public Mono<Double> queryExchangeRateForAsync(Scheduler scheduler,
                                                  String sourceAndDestination,
                                                  Mono<Double> defaultRate) {
        // Return a mono to the exchange rate.
        return Mono
            .fromCallable(() -> mExchangeRate
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mQueryExchangeRateURIM)
                 .queryParam("sourceAndDestination", sourceAndDestination)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a Mono of Double.
            .bodyToMono(Double.class))
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Mono<Double>.
            .flatMap(Function.identity())

            // If this computation runs for more than 2 seconds
            // return the default rate.
            .timeout(Duration.ofSeconds(2), defaultRate);
    }

    /**
     * Finds the exchange rate for the {@code sourceAndDestination} synchronously.
     *
     * @param sourceAndDestination The source currency and the destination currency
     * @return A Mono containing the exchane rate.
     */
    public Mono<Double> queryExchangeRateForSync(String sourceAndDestination,
                                                 Mono<Double> defaultRate) {
        // Return a mono to the exchange rate.
        return Mono
            .fromCallable(() -> mExchangeRate
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mQueryExchangeRateURIM)
                 .queryParam("sourceAndDestination", sourceAndDestination)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a Mono of Double.
            .bodyToMono(Double.class))
            
            // De-nest the result so it's a Mono<Double>.
            .flatMap(Function.identity())

            // If this computation runs for more than 2 seconds
            // return the default rate.
            .timeout(Duration.ofSeconds(2), defaultRate);
    }
}
