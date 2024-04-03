package primechecker.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import primechecker.common.Options;

import java.util.concurrent.Executors;

/**
 * This class provides the entry point into the Spring WebMVC-based
 * version of the prime check app server.  It also uses Java virtual
 * threads to process all client requests.
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
            // Launch the ServerApplication within Spring WebFlux.
            .run(PCServerApplication.class, argv);
    }
}
