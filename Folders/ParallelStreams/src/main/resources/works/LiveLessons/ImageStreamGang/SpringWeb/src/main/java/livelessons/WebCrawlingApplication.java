package livelessons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class provides the entry point into the Spring-based version
 * of the ImageStreamGang example.
 */
@SpringBootApplication
public class WebCrawlingApplication {
    /**
     * A static main() entry point is needed to run the
     * ImageStreamGang app.
     */
    public static void main(String[] args) {
        // Launch the WebCrawlingApplication within Spring.
        SpringApplication.run(WebCrawlingApplication.class,
                              args);
    }

}
