package zippyisms.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.Executors;

/**
 * Provides the context for running the ZippyApplication, which is a
 * reactive microservice that provides random Zippy th' Pinhead quotes
 * to clients using all four RSocket interaction models.
 *
 * The {@code @SpringBootApplication} annotation enables apps to use
 * auto-configuration, component scan, and to define extra
 * configuration on their "application" class.  
 *
 * The {@code @ComponentScan} annotation enables auto-detection of
 * beans by a Spring container.  Java classes that are decorated with
 * stereotypes such as {@code @Component}, {@code @Configuration}, and
 * {@code @Service}, are auto-detected by Spring.
 */
@SpringBootApplication
@ComponentScan("zippyisms")
public class ZippyApplication {
    public static void main(String[] args) {
        // Run the ZippyApplication microservice.
        SpringApplication.run(ZippyApplication.class,
            args);
    }
}
