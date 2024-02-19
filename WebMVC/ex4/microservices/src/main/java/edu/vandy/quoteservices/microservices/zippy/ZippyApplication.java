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
 * The {@code @ComponentScan} annotation configures Spring to scan for
 * Spring components (such as {@code @Component}, {@code @Service}, or
 * {@code @Controller}) within the specified base packages. This
 * annotation is used to automatically detect and register Spring
 * beans from the specified classes' packages, allowing for easy
 * component management and autowiring.  In this case, Spring will
 * scan for components within the packages of the {@link
 * ZippyApplication} and {@link ServerBeans} classes.
 *
 * The {@code EntityScan} annotation configures the base packages used
 * for scanning entities. Specifically, it directs Spring to look for
 * JPA entities in the package containing the {@link Quote} class,
 * which is useful when entities are located in a package that is not
 * automatically scanned by Spring.
 *
 * The {@code EnableJpaRepositories} annotation enables Spring Data
 * JPA repositories. It configures the base packages used for scanning
 * Spring Data repositories. In this case, Spring is directed to scan
 * for repositories in the package containing the {@link
 * ZippyQuoteRepository} class.  This annotation is essential for
 * integrating Spring Data JPA into the application, allowing for the
 * use of repository interfaces to abstract data layer access.
 * 
 * The {@code EnableCaching} annotation enables caching in a Spring
 * application. This annotation allows Spring to detect caching
 * configurations and to intercept methods where caching behavior is
 * applied. It is a declarative way of enabling cache support, e.g.,
 * by facilitating caching operations on methods and reducing the
 * number of executions based on the cacheable results.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {
    ZippyApplication.class,
    ServerBeans.class
})
@EntityScan(basePackageClasses = {Quote.class})
@EnableJpaRepositories(basePackageClasses = {ZippyQuoteRepository.class})
@EnableCaching
public class ZippyApplication
       extends BaseApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Call BaseClass helper to build and run this application.
        run(ZippyApplication.class, args);
    }
}
