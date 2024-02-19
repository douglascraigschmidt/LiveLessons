package edu.vandy.quoteservices.common;

import edu.vandy.quoteservices.client.QuoteAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static edu.vandy.quoteservices.common.Constants.GATEWAY_BASE_URL;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
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
        RestTemplate restTemplate = new RestTemplate();

        restTemplate
            // Set the base URL for the RestTemplate.
            .setUriTemplateHandler
            (new DefaultUriBuilderFactory(GATEWAY_BASE_URL));

        // Return restTemplate.
        return restTemplate;
    }

    /**
     * Create an instance of the {@link QuoteAPI} Retrofit client,
     * which is then used to making HTTP requests to the {@code
     * GatewayApplication} RESTful microservice.
     *
     * @return A {@link QuoteAPI} instance
     */
    @Bean
    public QuoteAPI getQuoteAPI() {
        return new Retrofit
            // Create a new Retrofit Builder object, which is used to
            // configure the Retrofit client.
            .Builder()

            // Set the base URL for the GatewayApplication microservice
            // the Retrofit client communicates with.
            .baseUrl(GATEWAY_BASE_URL)

            // Add a converter factory to the Retrofit client that
            // converts the raw JSON response data from the
            // GatewayApplication microservice into Java objects.
            .addConverterFactory(GsonConverterFactory.create())

            // Create the Retrofit client object with the configuration
            // settings applied in the previous steps.
            .build()

            // Create an implementation of the QuoteAPI interface, which
            // defines the endpoints and methods for interacting with the
            // GatewayApplication microservice.
            .create(QuoteAPI.class);
    }
}
