package edu.vandy.quoteservices.microservices.zippy;

import edu.vandy.quoteservices.common.BaseApplication;
import edu.vandy.quoteservices.common.ServerBeans;
import edu.vandy.quoteservices.common.Quote;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * This class provides the entry point into the Spring WebMVC-based
 * version of the Zippy quote microservice.
 *
 * The {@code @SpringBootApplication} annotation enables apps to use
 * autoconfiguration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @ComponentScan} annotation configures component scanning
 * directives for use with {@code @Configuration} classes.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {
    ZippyApplication.class,
    ServerBeans.class
})
@EnableCaching
@EntityScan(basePackageClasses = {Quote.class})
@EnableJpaRepositories(basePackageClasses = {JPAQuoteRepository.class})
public class ZippyApplication extends BaseApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Call BaseClass helper to build and run this application.
        run(ZippyApplication.class, args);
    }
}
