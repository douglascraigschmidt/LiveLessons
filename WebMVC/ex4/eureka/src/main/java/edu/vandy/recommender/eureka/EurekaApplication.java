package edu.vandy.recommender.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka is a REST (Representational State Transfer) based service
 * that is primarily used in the AWS cloud for locating services for
 * the purpose of load balancing and fail-over of middle-tier servers.
 *
 * The {@code @SpringBootApplication} annotation enables apps to use
 * autoconfiguration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @EnableEurekaServer} annotation activates the Eureka
 * server-side discovery service.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {
    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Launch the Eureka server-side discovery service
        // application.
        SpringApplication
            .run(EurekaApplication.class,
                 args);
    }
}
