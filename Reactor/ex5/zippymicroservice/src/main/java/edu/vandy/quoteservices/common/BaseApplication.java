package edu.vandy.quoteservices.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;

import java.util.concurrent.Executors;

import static java.util.Collections.singletonMap;

/**
 * A static class with a single {@link #run} static method that is used by all
 * microservices to build a Spring Boot application instance and to give a
 * unique name that is used as a path component in URLs and for routing by the
 * gateway application.
 */
@Configuration
@PropertySource(
    value = "classpath:/application.yml",
    factory = YamlPropertySourceFactory.class)
public class BaseApplication {
    /**
     * Helper method that builds a Spring Boot application using the
     * passed {@link Class} parameter and also sets the application
     * name to the package name of the passed {@link Class} parameter.
     *
     * @param clazz Any microservice {@link Class} type
     * @param args  Command line arguments
     */
    public static void run(Class<?> clazz, String[] args) {
        var name = getName(clazz);
        var app = new SpringApplicationBuilder(clazz)
            .properties(singletonMap("spring.application.name", name))
            .build();
        app.setAdditionalProfiles(name);
        // app.setLazyInitialization(true);
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

    /**
     * Configure the use of Java virtual threads to handle all
     * incoming HTTP requests.
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors
                .newVirtualThreadPerTaskExecutor());
    }

    /**
     * Customize the ProtocolHandler on the TomCat Connector to
     * use Java virtual threads to handle all incoming HTTP requests.
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler
                    .setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
