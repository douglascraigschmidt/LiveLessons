package edu.vandy.quoteservices.microservice.handey;

import edu.vandy.quoteservices.common.BaseApplication;
import edu.vandy.quoteservices.common.Components;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * This class provides the entry point into the Spring WebMVC-based
 * version of the Handey quote microservice.
 *
 * The {@code @SpringBootApplication} annotation enables apps to use
 * autoconfiguration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @ComponentScan} annotation configures component scanning
 * directives for use with {@code @Configuration} classes.
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = {HandeyApplication.class, Components.class})
public class HandeyApplication extends BaseApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Call BaseClass helper to build and run this application.
        run(HandeyApplication.class, args);
    }
}
