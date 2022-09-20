package common;

import client.PrimeCheckClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import server.PrimeCheckService;

/**
 * This class contains {@code Bean} annotated methods that can be
 * injected into client and server classes using the {@code @Autowired}
 * annotation.
 */
@Component
public class Components {
    /**
     * This factory method returns a new {@link RestTemplate}, which
     * enables a client to perform HTTP requests synchronously.
     * 
     * @return A new {@link RestTemplate}
     */
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    /**
     * This factory method returns a new {@link PrimeCheckService}.
     * 
     * @return A new {@link PrimeCheckService}
     */
    @Bean
    public PrimeCheckService getPrimeCheckService() {
        return new PrimeCheckService();
    }
}
