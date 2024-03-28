package edu.vandy.lockmanager.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

/**
 * This microservice provides a lock manager microservice that uses
 * Spring WebFlux to implement a distribute semaphore that can be acquired
 * and released by multiple clients concurrently.
 *
 * The {@code @SpringBootApplication} annotation is used to indicate
 * that a class is the main configuration class for a Spring Boot
 * application.  It enables auto-configuration and component scanning
 * by default, and is equivalent to using {@code @Configuration},
 * {@code @EnableAutoConfiguration}, and {@code @ComponentScan}
 * together.
 *
 * The {@code @ComponentScan} annotation is used to specify the base
 * package for component scanning in a Spring application.  It tells
 * Spring where to look for components such as controllers, services,
 * and repositories.
 */
@SpringBootApplication
@ComponentScan("edu.vandy.lockmanager")
public class LockManagerApplication {
    /**
     * The main entry point into the LockManager microservice.
     */
    public static void main(String[] args) {
        SpringApplication.run(LockManagerApplication.class,
                              args);
    }

    /**
     * Configure the use of Java virtual threads to handle all
     * incoming HTTP requests from clients.
     */
    @Bean(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors
                                       .newVirtualThreadPerTaskExecutor());
    }

    /**
     * Customize the ProtocolHandler on the TomCat Connector to use
     * Java virtual threads to handle all incoming HTTP requests.
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler
                .setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}





