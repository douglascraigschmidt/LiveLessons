package microservices.exchangeRate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import utils.Options;

/**
 * This class provides the entry point into the Spring WebFlux-based
 * version of the ExchangeRate microservice.
 */
@SpringBootApplication
public class ExchangeRateMicroservice {
    /**
     * A static main() entry point is needed to run the ExchangeRate
     * microservice.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch the ExchangeRateApplication within Spring
            // WebFlux.
            .run(ExchangeRateMicroservice.class, argv);
    }
}
