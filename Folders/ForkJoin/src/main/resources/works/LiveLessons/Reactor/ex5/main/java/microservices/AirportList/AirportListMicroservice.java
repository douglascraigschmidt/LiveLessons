package microservices.airports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import utils.Options;

/**
 * This class provides the entry point into the Spring WebFlux-based
 * version of the AirportList microservice.
 */
@SpringBootApplication
public class AirportListMicroservice {
    /**
     * A static main() entry point is needed to run the AirportList
     * microservice.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch this microservice within Spring WebFlux.
            .run(AirportListMicroservice.class, argv);
    }
}
