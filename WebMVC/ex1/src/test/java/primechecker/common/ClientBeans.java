package primechecker.common;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
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

        var poolConnections = System.getenv("POOL_CONNECTIONS");
        if (poolConnections != null
            && poolConnections.equals("true")) {
            PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager();

            connectionManager.setMaxTotal(100);
            connectionManager.setDefaultMaxPerRoute(20);

            RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(5000) // timeout to get connection from pool
                .setSocketTimeout(5000) // standard connection timeout
                .setConnectTimeout(5000) // standard connection timeout
                .build();

            HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig).build();

            ClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

            restTemplate = new RestTemplate(requestFactory);
        } else {
            restTemplate = new RestTemplate();
        }

        restTemplate
            // Set the base URL for the RestTemplate.
            .setUriTemplateHandler(new DefaultUriBuilderFactory(SERVER_BASE_URL));

        // Return restTemplate.
        return restTemplate;
    }
}
