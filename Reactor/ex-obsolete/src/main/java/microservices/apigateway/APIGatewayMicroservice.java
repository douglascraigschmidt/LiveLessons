package microservices.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import utils.Options;

/**
 * This class provides the entry point into the Spring WebFlux-based
 * version of the APIGateway microservice, which defines the external
 * entry point into the Airline Booking App microservices.
 */
@SpringBootApplication
public class APIGatewayMicroservice {
    /**
     * A static main() entry point is needed to run the APIGateway
     * microservice.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch the APIGatewayApplication within Spring WebFlux.
            .run(APIGatewayMicroservice.class, argv);
    }
}
