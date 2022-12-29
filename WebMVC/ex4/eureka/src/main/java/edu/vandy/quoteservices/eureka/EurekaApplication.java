package edu.vandy.quoteservices.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka is a REST (Representational State Transfer) based service that is
 * primarily used in the AWS cloud for locating services for the purpose of load
 * balancing and fail-over of middle-tier servers.
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
