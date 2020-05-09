import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
    public static void main(String[] args) {
        SpringApplication
            // Launch the FolderApplication within Spring WebFlux.
            .run(FolderApplication.class, args);
    }
}
