package quoteservices.common;

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

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

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

    /**
     * @return Return a {@link List} of {@link ZippyQuote} objects
     *         that were stored in the file zippy-quotes.txt
     */
    @Bean
    public List<ZippyQuote> getZippyInput() {
        try {
            // Although AtomicInteger is overkill we use it to
            // simplify incrementing the ID in the stream below.
            AtomicInteger idCount = new AtomicInteger(0);

            // Convert the filename into a pathname.
            URI uri = ClassLoader
                .getSystemResource("zippy/zippy-quotes.txt")
                .toURI();

            // Open the file and get all the bytes.
            CharSequence bytes =
                new String(Files.readAllBytes(Paths.get(uri)));

            // Return a List of ZippyQuote objects.
            return Pattern
                // Compile splitter into a regular expression (regex).
                .compile("@")

                // Use the regex to split the file into a stream of
                // strings.
                .splitAsStream(bytes)

                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Create a new ZippyQuote.
                .map(quote ->
                     new ZippyQuote(idCount.incrementAndGet(),
                                    quote.stripLeading()))
                
                // Collect results into a list of ZippyQuote objects.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return Return a {@link List} of {@link HandeyQuote} objects
     *         that were stored in the file deep-thought-quotes.txt
     */
    @Bean
    public List<HandeyQuote> getHandeyInput() {
        try {
            // Although AtomicInteger is overkill we use it to
            // simplify incrementing the ID in the stream below.
            AtomicInteger idCount = new AtomicInteger(0);

            // Convert the filename into a pathname.
            URI uri = ClassLoader
                .getSystemResource("handey/deep-thought-quotes.txt")
                .toURI();

            // Open the file and get all the bytes.
            CharSequence bytes =
                new String(Files.readAllBytes(Paths.get(uri)));

            // Return a List of HandeyQuote objects.
            return Pattern
                // Compile splitter into a regular expression (regex).
                .compile("@")

                // Use the regex to split the file into a stream of
                // strings.
                .splitAsStream(bytes)

                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Create a new HandeyQuote.
                .map(quote ->
                     new HandeyQuote(idCount.incrementAndGet(),
                                     quote.stripLeading()))
                
                // Collect results into a list of ZippyQuote objects.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
