package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import utils.Options;

import static common.Constants.Resources.SERVER_PROPERTIES;

/**
 * This class provides the entry point into the Spring WebMVC-based
 * version of the prime check server.
 *
 * The {@code @SpringBootApplication} annotation enables apps to use
 * auto-configuration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @PropertySource} annotation is used to provide a
 * properties file to the Spring Environment.
 */
@SpringBootApplication
@PropertySource(SERVER_PROPERTIES)
public class PrimeCheckApplication {
    /**
     * A static main() entry point is needed to run the {@link
     * PrimeCheckApplication} app.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch the ServerApplication within Spring WebMVC.
            .run(PrimeCheckApplication.class, argv);
    }
}
