package publisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import utils.Options;

/**
 * This class provides the entry point into the Spring WebFlux-based
 * version of the Publisher example.
 */
@SpringBootApplication
public class PublisherApplication {
    /**
     * A static main() entry point is needed to run the
     * PublisherApplication app.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch the PublisherApplication within Spring WebFlux.
            .run(PublisherApplication.class, argv);
    }
}
