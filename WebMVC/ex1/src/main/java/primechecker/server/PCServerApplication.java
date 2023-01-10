package primechecker.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import primechecker.common.Options;

/**
 * This class provides the entry point into the Spring WebMVC-based
 * version of the prime check app server.
 * 
 * The {@code @SpringBootApplication} annotation enables apps to use
 * auto-configuration, component scan, and to define extra
 * configurations on their "application" class.
 * 
 * The {@code @ComponentScan} annotation configures component scanning
 * directives for use with {@code @Configuration} classes.
 */
@SpringBootApplication
@ComponentScan("primechecker")
public class PCServerApplication {
    /**
     * A static main() entry point is needed to run the {@link
     * PCServerApplication} server app.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch the ServerApplication within Spring WebMVC.
            .run(PCServerApplication.class, argv);
    }
}
