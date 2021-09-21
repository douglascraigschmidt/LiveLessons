package common;

import client.TestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the {@code @Autowired} annotation.
 */
@Component
public class Components {
    /**
     * This factory method returns a new {@link RestTemplate}, which
     * enables a synchronous client to perform HTTP requests.
     * 
     * @return A new {@link RestTemplate}
     */
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /**
     * This factory method returns a new {@link TestClient}.
     * 
     * @return A new {@link TestClient}
     */
    @Bean
    public TestClient getTestClient() {
        return new TestClient();
    }
}
