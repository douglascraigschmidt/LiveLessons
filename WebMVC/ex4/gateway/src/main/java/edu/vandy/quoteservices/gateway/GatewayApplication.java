package edu.vandy.quoteservices.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

/**
 * @@ Monte, what does this class do?
 */
@SpringBootApplication
@Configuration
public class GatewayApplication {
    /**
     *
     */
    private static final Logger logger =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * @@ Monte, what is the field used for?
     */
    @Autowired
    private GatewayProperties props;

    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Launch this application.
        SpringApplication.run(GatewayApplication.class, args);
    }

    /**
     * Runs after application has been constructed and displays
     * the current Gateway properties that shows all gateway
     * routing paths.
     */
    @PostConstruct
    public void init() {
        logger.info(Objects.toString(props));
    }
}