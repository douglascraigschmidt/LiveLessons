package edu.vandy.quoteservices.common;

import edu.vandy.quoteservices.client.QuoteAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import static edu.vandy.quoteservices.common.Constants.GATEWAY_BASE_URL;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Component
public class ClientBeans {
    /**
     * @return A new instance of the {@link QuoteAPI}
     */
    @Bean
    public QuoteAPI getQuoteAPI() {
        var webClient = WebClient
            // Create a new WebClient.
            .builder()

            // Add the base URL for the GatewayApplication
            // microservice.
            .baseUrl(GATEWAY_BASE_URL)

            // Finish initializing the WebClient.
            .build();

        // Return a new instance of ZippyQuoteAPI that makes HTTP
        // requests using the provided WebClient object.
        return HttpServiceProxyFactory
            // Create an instance of the HttpServiceProxyFactory class
            // and build a new WebClientAdapter.
            .builder(WebClientAdapter
                .forClient(webClient))

            // Build the HttpServiceProxyFactory instance.
            .build()

            // Create a new instance of the QuoteAPI interface.
            .createClient(QuoteAPI.class);
    }
}
