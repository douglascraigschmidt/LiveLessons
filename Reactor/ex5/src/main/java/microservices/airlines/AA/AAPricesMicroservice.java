package microservices.airlines.AA;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import utils.Options;

/**
 * This class provides the entry point into the Spring WebFlux-based
 * version of the American Airlines (AA) microservice.
 */
@SpringBootApplication
public class AAPricesMicroservice {
    /**
     * A static main() entry point is needed to run the AA
     * microservice.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch this microservice within Spring WebFlux.
            .run(AAPricesMicroservice.class, argv);
    }
}
