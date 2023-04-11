package primechecker.common;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import primechecker.client.PCProxyAPI;

import static primechecker.common.Constants.SERVER_BASE_URL;

/**
 * This class contains {@code Bean} annotated methods that can be
 * injected into client and server classes using the {@code @Autowired}
 * annotation.
 */
@Component
public class ClientBeans {
    /**
     * @return A new instance of the {@link PCProxyAPI}
     */
    @Bean
    public PCProxyAPI getPCProxyAPI() {
        var webClient = WebClient
            // Create a new WebClient.
            .builder()

            // Add the base URL for the server.
            .baseUrl(SERVER_BASE_URL)

            // Finish initializing the WebClient.
            .build();

        // Return a new instance of PCProxyAPI that makes HTTP
        // requests using the provided WebClient object.
        return HttpServiceProxyFactory
            // Create an instance of the HttpServiceProxyFactory class
            // and build a new WebClientAdapter.
            .builder(WebClientAdapter
                .forClient(webClient))

            // Build the HttpServiceProxyFactory instance.
            .build()

            // Create a new instance of the PCProxyAPI interface.
            .createClient(PCProxyAPI.class);
    }
}
