package edu.vandy.quoteservices.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;

import static java.util.Collections.singletonMap;

/**
 * A static class with a single {@link #run} static method that is used by all
 * microservices to build a Spring Boot application instance and to give a
 * unique name that is used as a path component in URLs and for routing by the
 * gateway application.
 */
@Configuration
public class BaseApplication {
    /**
     * Helper method that builds Spring Boot application using the
     * passed {@link Class} parameter and also sets the application
     * name to the package name of the passed {@link Class} parameter.
     *
     * @param clazz Any microservice {@link Class} type
     * @param args  Command line arguments
     */
    public static void run(Class<?> clazz, String[] args) {
        String name = getName(clazz);
        SpringApplication app = new SpringApplicationBuilder(clazz)
            .properties(singletonMap("spring.application.name", name))
            .build();
        app.setAdditionalProfiles(name);
        app.setLazyInitialization(true);
        app.run(args);
    }

    /**
     * Gets the name of the application, which is the last part of the
     * package name.
     *
     * @param clazz Any microservice {@link Class} type
     * @return A {@link String} containing the application name, which is the
     * last part of package name
     */
    private static String getName(Class<?> clazz) {
        // Get the package name.
        String pkg = clazz.getPackage().getName();

        // Return the last part of the package name.
        return pkg.substring(pkg.lastIndexOf('.') + 1);
    }
}
