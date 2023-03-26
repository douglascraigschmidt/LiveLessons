package edu.vandy.quoteservices.common;

import edu.vandy.quoteservices.client.HandeyQuoteAPI;
import edu.vandy.quoteservices.client.ZippyQuoteAPI;
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
     * @return A new instance of the {@link HandeyQuoteAPI}
     */
    @Bean
        public HandeyQuoteAPI getHandeyQuoteAPI() {
            var webClient = WebClient
                // Create a new WebClient.
                .builder()

                // Add the base URL for the GatewayApplication microservice.
                .baseUrl(GATEWAY_BASE_URL)

                // Finish initializing the WebClient.
                .build();

            // Return a new instance of HandeyQuoteAPI that makes HTTP
            // requests using the provided WebClient object.
            return HttpServiceProxyFactory
                // Create an instance of the HttpServiceProxyFactory class
                // and build a new WebClientAdapter.
                .builder(WebClientAdapter
                    .forClient(webClient))

                // Build the HttpServiceProxyFactory instance.
                .build()

                // Create a new instance of the HandeyQuoteAPI interface.
                .createClient(HandeyQuoteAPI.class);
        }

    /**
     * @return A new instance of the {@link ZippyQuoteAPI}
     */
    @Bean
    public ZippyQuoteAPI getZippyQuoteAPI() {
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

            // Create a new instance of the ZippyQuoteAPI interface.
            .createClient(ZippyQuoteAPI.class);
    }
}
