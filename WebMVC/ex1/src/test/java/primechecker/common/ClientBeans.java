package primechecker.common;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import static primechecker.common.Constants.SERVER_BASE_URL;

/**
 * This class contains {@code Bean} annotated methods that can be
 * injected into client and server classes using the {@code @Autowired}
 * annotation.
 */
@Component
public class ClientBeans {
    /**
     * This factory method returns a new {@link RestTemplate}, which
     * enables a client to perform HTTP requests synchronously.
     *
     * @return A new {@link RestTemplate}
     */
    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate;

        // Determine if a connection factory should be used.
        if (!Options.instance().connectionPool()) {
            // Create a RestTemplate that doesn't use a connection
            // pool.
            restTemplate = new RestTemplate();
        } else {
            // Create a connection pool factory.
            var requestFactory = getConnectionPoolFactory();

            // Initialize a RestTemplate with the custom request
            // factory.
            restTemplate = new RestTemplate(requestFactory);
        }

        restTemplate
            // Set the base URL for the RestTemplate.
            .setUriTemplateHandler(new DefaultUriBuilderFactory(SERVER_BASE_URL));

        // Return restTemplate.
        return restTemplate;
    }

    /**
     * @return A {@link HttpComponentsClientHttpRequestFactory} that
     *         creates a connection pool
     */
    private static HttpComponentsClientHttpRequestFactory 
        getConnectionPoolFactory() {
        var connectionManager =
            new PoolingHttpClientConnectionManager();

        // Set the maximum number of total open connections to 100.
        connectionManager.setMaxTotal(100);

        // Set the maximum number of concurrent connections per
        // route, which is 20 by default.
        connectionManager.setDefaultMaxPerRoute(20);

        // Configure request parameters for the HttpClient.
        var requestConfig = RequestConfig
            .custom()
            // 5 second timeout to get a connection from the pool.
            .setConnectionRequestTimeout(5000) 

            // 5 second timeout for waiting for data or, put
            // differently, a maximum period inactivity between two
            // consecutive data packets.
            .setSocketTimeout(5000) 

            // 5 second timeout to establish the connection with the
            // remote host.
            .setConnectTimeout(5000) 
            .build();

        // Build the HttpClient with the pooling connection manager
        // and request configuration.
        var httpClient = HttpClientBuilder
            .create()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();

        // Use the HttpClient to create a Spring
        // ClientHttpRequestFactory.
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
