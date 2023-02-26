package edu.vandy.pubsub.common;

import org.springframework.context.annotation.Configuration;

import edu.vandy.pubsub.subscriber.PublisherAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory;

import static edu.vandy.pubsub.common.Constants.SERVER_BASE_URL;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Configuration
public class ClientBeans {
    /**
     * @return A {@link RestTemplate} instance
     */
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates a new instance of the Retrofit object with the base URL
     * for the server, and uses it to create an implementation of the
     * {@link PublisherAPI} interface. The implementation can be used
     * to make HTTP requests to the server.
     *
     * @return An implementation of the PublisherAPI interface that
     *         can be used to make HTTP requests to the server
     * @throws RuntimeException if there is an error building the
     *         Retrofit instance
     */
    @Bean
    public PublisherAPI getPublisherAPI() {
        // Create a Retrofit instance with a Reactor adapter and a
        // Gson converter.
        return new Retrofit
            // Create a new Retrofit instance.
            .Builder()

            // Provide the base URL for the server.
            .baseUrl(SERVER_BASE_URL)

            // Add a converter factory to the Retrofit client that
            // converts the raw JSON response data from the
            // GatewayApplication microservice into Java objects.
            .addConverterFactory(GsonConverterFactory.create())

            // Add a ReactorCallAdapterFactory to handle reactive calls.
            .addCallAdapterFactory(ReactorCallAdapterFactory.create())

            // Build the Retrofit instance.
            .build()

            // Use the Retrofit instance to create an implementation of the
            // PublisherAPI interface.
            .create(PublisherAPI.class);
    }
}
