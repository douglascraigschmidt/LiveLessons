package edu.vandy.quoteservices.common;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import static edu.vandy.quoteservices.common.Constants.GATEWAY_BASE_URL;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Component
public class ClientBeans {
    /**
     * @return A new {@link WebClient}.
     */
    @Bean
    public WebClient getWebClient() {
        return WebClient
            // Create a WebClient.
            .builder()

            // Add the partial path 
            .baseUrl(GATEWAY_BASE_URL)

            // Finish building the WebClient.
            .build();
    }
}
