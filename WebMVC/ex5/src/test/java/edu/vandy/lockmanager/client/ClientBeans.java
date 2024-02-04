package edu.vandy.lockmanager.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

import static edu.vandy.lockmanager.common.Constants.LOCK_MANAGER_SERVER_BASE_URL;

/**
 * Defines one or more {@code @Bean} factory methods used to
 * autowire various components used in this app.
 */
@Configuration
public class ClientBeans {
    /**
     * Set the timeout duration for blocking reads to be 60 seconds.
     */
    private final long sTIMEOUT_DURATION = 60;

    /**
     * @return A new instance of the {@link LockAPI} that is used with
     *         synchronous WebMVC clients
     */
    @Bean
    public LockAPI getLockAPI() {
        var webClient = WebClient
            // Create a new WebClient.
            .builder()

            // Add the base URL for the LockApplication microservice.
            .baseUrl(LOCK_MANAGER_SERVER_BASE_URL)

            // Finish initializing the WebClient.
            .build();

        // Return a new instance of LockAPI that makes HTTP
        // requests using the provided WebClient object.
        return HttpServiceProxyFactory
            // Create an instance of the HttpServiceProxyFactory class
            // and build a new WebClientAdapter.
            .builder(WebClientAdapter
                .forClient(webClient))

            // Set the block timeout in case the semaphore is
            // highly contested.
            .blockTimeout(Duration
                .ofSeconds(sTIMEOUT_DURATION))

            // Build the HttpServiceProxyFactory instance.
            .build()

            // Create a new instance of the LockAPI interface.
            .createClient(LockAPI.class);
    }
}
