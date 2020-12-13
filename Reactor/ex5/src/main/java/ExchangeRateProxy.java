import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;

/**
 * This class serves as a proxy to the ExchangeRate microservice.
 */
class ExchangeRateProxy {
    /**
     * The URI that determines the current exchange rate.
     */
    private final String mQueryExchangeRateURI = "/exchangeRate/_exchangeRate";

    /**
     * The WebClient provides the means to access the flight
     * micro-service.
     */
    private final WebClient mFlight;

    /**
     * Host/post where the server resides.
     */
    private final String mSERVER_BASE_URL =
        "http://localhost:8081";

    /**
     * Constructor initializes the fields to initialize a flight
     * that emits a stream of random numbers.
     */
    public ExchangeRateProxy() {
        mFlight = WebClient
            // Start building.
            .builder()

            // The URL where the server is running.
            .baseUrl(mSERVER_BASE_URL)

            // Build the webclient.
            .build();
    }

    /**
     * 
     *
     * @param scheduler
     * @param sourceAndDestination
     * @param defaultRate
     * @return
     */
    public Mono<Double> queryExchangeRateFor(Scheduler scheduler,
                                             String sourceAndDestination,
                                             Mono<Double> defaultRate) {
        // Return a mono to the exchange rate.
        return mFlight
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mQueryExchangeRateURI)
                 .queryParam("sourceAndDestination", sourceAndDestination)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a Mono of doubles.
            .bodyToMono(Double.class)
            
            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // If this computation runs for more than 2 seconds
            // return the default rate.
            .timeout(Duration.ofSeconds(2), defaultRate);
    }
}
