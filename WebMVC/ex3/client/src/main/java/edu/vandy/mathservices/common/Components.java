package edu.vandy.mathservices.common;

import edu.vandy.mathservices.client.GCDProxy;
import edu.vandy.mathservices.client.PrimalityProxy;
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

/**
 * This class contains factory methods.
 */
@Component
public class Components {
    /**
     * This factory method returns a new {@link RestTemplate}, which
     * enables a client to perform HTTP requests synchronously.
     *
     * @param serverBaseUrl The hostname and port number for the
     *                      microservice
     * @return A new {@link RestTemplate}
     */
    public static RestTemplate getRestTemplate(String serverBaseUrl) {
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
            .setUriTemplateHandler(new DefaultUriBuilderFactory(serverBaseUrl));

        // Return restTemplate.
        return restTemplate;
    }
}
