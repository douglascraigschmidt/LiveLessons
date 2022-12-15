package quoteservices.server.zippy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import quoteservices.common.Options;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * This class provides the entry point into the Spring WebMVC-based
 * version of the Zippy microservice.
 * 
 * The {@code @SpringBootApplication} annotation enables apps to use
 * autoconfiguration, component scan, and to define extra
 * configurations on their "application" class.
 * 
 * The {@code @ComponentScan} annotation configures component scanning
 * directives for use with {@code @Configuration} classes.
 */
@SpringBootApplication
@ComponentScan("quoteservices")
@PropertySources({
        @PropertySource("classpath:/zippy/zippy-microservice.properties")
})
public class ZippyApplication {
    /**
     * A static main() entry point is needed to run the {@link
     * ZippyApplication} server app.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch the ServerApplication within Spring WebMVC.
            .run(ZippyApplication.class, argv);
    }
}
