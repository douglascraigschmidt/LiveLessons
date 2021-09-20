package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import utils.Options;

/**
 * This class provides the entry point into the Spring WebMVC-based
 * version of the server.
 */
@SpringBootApplication
@PropertySource("classpath:/server/server-application.properties")
public class ServerApplication {
    /**
     * A static main() entry point is needed to run the
     * ServerApplication app.
     */
    public static void main(String[] argv) {
        // Parse the options.
        Options.instance().parseArgs(argv);

        SpringApplication
            // Launch the ServerApplication within Spring WebMVC.
            .run(ServerApplication.class, argv);
    }
}
