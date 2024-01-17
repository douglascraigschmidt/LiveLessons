package berraquotes.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

/**
 * This class provides the entry point into the Spring WebMVC-based
 * version of the BerraQuotes app server.
 * 
 * The {@code @SpringBootApplication} annotation enables apps to use
 * auto-configuration, component scan, and to define extra
 * configurations on their "application" class.
 * 
 * The {@code @ComponentScan} annotation configures component scanning
 * directives for use with {@code @Configuration} classes.
 */
@SpringBootApplication
@ComponentScan("berraquotes")
public class BerraQuotesApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Launch this application.
        SpringApplication.run(BerraQuotesApplication.class,
                        args);
    }
}
