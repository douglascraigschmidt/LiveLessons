package folders.server;

import folders.utils.Options;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

/**
 * This class provides the entry point into the Spring WebMVC-based
 * version of the Folder example.
 * 
 * The {@code @SpringBootApplication} annotation enables apps to use
 * auto-configuration, component scan, and to define extra
 * configurations on their "application" class.
 * 
 * The {@code @ComponentScan} annotation configures component scanning
 * directives for use with {@code @Configuration} classes.
 */
@SpringBootApplication
@ComponentScan("folders")
@PropertySource("classpath:/application.properties")
public class FolderApplication {
    /**
     * A static main() entry point is needed to run the
     * FolderApplication app.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.getInstance().parseArgs(argv);

        SpringApplication
            // Launch the FolderApplication within Spring WebFlux.
            .run(FolderApplication.class, argv);
    }
}
