package quotes.common;

import com.theokanning.openai.service.OpenAiService;
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
     * @return An instance of {@link OpenAiService} with the API key
     *         configured in the private.properties file
     */
    @Bean
    OpenAiService getOpenAiService() {
        // This variable obtains the OpenAI API key from the
        // private.properties file in the root package.
        return new OpenAiService(BuildConfig.API_KEY);
    }
}
