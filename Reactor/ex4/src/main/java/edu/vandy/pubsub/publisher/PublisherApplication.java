package edu.vandy.pubsub.publisher;

import edu.vandy.pubsub.common.Options;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class provides the entry point into the Spring WebFlux-based
 * version of the Publisher example.
 */
@SpringBootApplication
@ComponentScan("edu.vandy.pubsub")
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
