package edu.vandy.quoteservices.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka is a REST (Representational State Transfer)-based discovery
 * service used to locate microservices.
 *
 * Eureka can also be used for load balancing and handling fail-over
 * of middle-tier servers.
 *
 * The {@code @SpringBootApplication} annotation enables apps to use
 * autoconfiguration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @EnableEurekaServer} annotation makes a Spring Boot
 * application act as a Eureka Server.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {
    public static void main(String[] args) {
        SpringApplication
            .run(EurekaApplication.class,
                 args);
    }
}
