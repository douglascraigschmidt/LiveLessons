package quotes.common;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

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
     * This method reads the ChatGPT API key from the
     * {@code local.properties} file in the resources
     * package.
     *
     * @return Return the API key to communicate with ChatGPT
     */
    public String getChatGPTAPIKey() {
        try {
            // Convert the filename into a pathname.
            URI uri = ClassLoader
                .getSystemResource("local.properties")
                .toURI();

            // Open the file, read all the bytes, and return it as a
            // String.
            var key = new String(Files
                .readAllBytes(Paths.get(uri)))
                .replaceAll("\\R$", "");

            System.out.println("API key = " + key);

            return key;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return A {@link WebClient} instance with the API key and
     *         default content type as default headers.
     */
    @Bean
    WebClient getWebClient() {
        return WebClient
            // Start building a new WebClient instance.
            .builder()

            // Add the API key as a default authorization header.
            .defaultHeader("Authorization",
                "Bearer " + getChatGPTAPIKey())

            // Set the default content type to JSon.
            .defaultHeader("Content-Type",
                MediaType.APPLICATION_JSON_VALUE)

            // Build the WebClient instance.
            .build();
    }
}
