package edu.vandy.lockmanager.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * This microservice provides a lock manager microservice that
 * implements a semaphore that can be acquired and released by
 * multiple clients concurrently.
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
 *
 * The {@code @EnableAsync} annotation is used to enable support for
 * asynchronous method execution in a Spring application.  When this
 * annotation is used, Spring creates a task executor that can be used
 * to execute methods asynchronously using the {@code @Async}
 * annotation.
 */
@SpringBootApplication
@ComponentScan("edu.vandy.lockmanager")
@EnableAsync
public class LockManagerApplication {
    /**
     * The main entry point into the {@link LockManagerApplication}
     * microservice.
     */
    public static void main(String[] args) {
        SpringApplication.run(LockManagerApplication.class,
                              args);
    }
}







