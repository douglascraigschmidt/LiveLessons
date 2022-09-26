package primechecker.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import primechecker.utils.Options;

/**
 * This class provides the entry point into the Spring WebMVC-based
 * version of the prime check app server.
 * <p>
 * The {@code @SpringBootApplication} annotation enables apps to use
 * auto-configuration, component scan, and to define extra
 * configurations on their "application" class.
 * <p>
 * The {@code @ComponentScan} annotation configures component scanning
 * directives for use with {@code @Configuration} classes.
 */
@SpringBootApplication
@ComponentScan("primechecker")
public class PrimeCheckApplication {
    /**
     * A static main() entry point is needed to run the {@link
     * PrimeCheckApplication} server app.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
                // Launch the ServerApplication within Spring WebMVC.
                .run(PrimeCheckApplication.class, argv);
    }
}