package microservices.flightPrice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import utils.Options;

/**
 * This class provides the entry point into the Spring WebFlux-based
 * version of the FlightPrice microservice.
 */
@SpringBootApplication
public class FlightPriceMicroservice {
    /**
     * A static main() entry point is needed to run the
     * FlightApplication app.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch the FlightApplication within Spring WebFlux.
            .run(FlightPriceMicroservice.class, argv);
    }
}
