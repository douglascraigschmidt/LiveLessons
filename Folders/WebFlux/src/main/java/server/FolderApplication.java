package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import utils.Options;

/**
 * This class provides the entry point into the Spring WebFlux-based
 * version of the Folder example.
 */
@SpringBootApplication
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
