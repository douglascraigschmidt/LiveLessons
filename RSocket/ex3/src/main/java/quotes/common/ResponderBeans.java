package quotes.common;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import quotes.BuildConfig;

/**
 * This class contains {@code @Bean} methods that initialize various
 * components used by the RSocket responder.  
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., without having to write any explicit
 * code, Spring will scan the application for classes annotated with
 * {@code @Component}, instantiate them, and inject any specified
 * dependencies into them.
 */
@Component
public class ResponderBeans {
    /**
     * @return A {@link WebClient} instance with the API key and
     *         default content type as default headers.
     */
    @Bean
    WebClient getWebClient() {
        // This variable obtains the ChatGPT API key from the
        // private.properties file in the root package.
        String mChatGPTAPIKey = BuildConfig.API_KEY;

        System.out.println("API_KEY = " + mChatGPTAPIKey);

        return WebClient
            // Start building a new WebClient instance.
            .builder()

            // Add the API key as a default authorization header.
            .defaultHeader("Authorization",
                           "Bearer " + mChatGPTAPIKey)

            // Set the default content type to JSon.
            .defaultHeader("Content-Type",
                MediaType.APPLICATION_JSON_VALUE)

            // Build the WebClient instance.
            .build();
    }
}
