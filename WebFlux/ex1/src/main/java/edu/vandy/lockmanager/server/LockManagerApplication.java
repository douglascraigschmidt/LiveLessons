package edu.vandy.lockmanager.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("edu.vandy.lockmanager")
public class LockManagerApplication {
	/**
	 * The main entry point into the LockManager microservice.
	 */
	public static void main(String[] args) {
		SpringApplication.run(LockManagerApplication.class, args);
	}

}
