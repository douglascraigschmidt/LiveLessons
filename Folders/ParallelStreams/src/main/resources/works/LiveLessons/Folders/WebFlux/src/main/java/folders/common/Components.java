package folders.common;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import static folders.common.Constants.SERVER_BASE_URL;

/**
 * This class contains {@code Bean} annotated methods that can be
 * injected into client and server classes using the
 * {@code @Autowired} annotation.
 */
@Component
public class Components {
    /**
     * This factory method returns a new {@link WebClient}, which
     * enables a client to perform HTTP requests asynchronously.
     *
     * @return A new {@link WebClient} configured to use the {@code
     *         SERVER_BASE_URL}
     */
    @Bean
    public WebClient getWebClient() {
        // Increase the max size for the buffer transfers.
        ExchangeStrategies exchangeStrategies = ExchangeStrategies
            .builder()
            // Increase the memory size.
            .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024))
            // Build the strategy.
            .build();


        // Start building.
        return WebClient
            .builder()

            // The URL where the server is running.
            .baseUrl(SERVER_BASE_URL)

            // Increase the max buffer size.
            .exchangeStrategies(exchangeStrategies)

            // Build the webclient.
            .build();
    }
}
