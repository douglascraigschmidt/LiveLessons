package edu.vandy.lockmanager.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * This microservice provides a distributed lock manager.
 */
@SpringBootApplication
@ComponentScan("edu.vandy.lockmanager")
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





