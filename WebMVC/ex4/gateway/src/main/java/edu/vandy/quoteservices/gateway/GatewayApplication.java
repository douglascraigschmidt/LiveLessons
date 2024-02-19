package edu.vandy.quoteservices.gateway;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.context.annotation.Configuration;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

/**
 * Defines an API gateway that uses routes to forward requests to
 * downstream microservices.
 * 
 * The {@code @SpringBootApplication} annotation enables apps to use
 * autoconfiguration, component scan, and to define extra
 * configurations on their "application" class.
 *
 * The {@code @Configuration} annotation tells Spring container that 
 * there is one or more beans that needs to be dealt with at runtime.
 */
@SpringBootApplication
@Configuration
public class GatewayApplication {
    /**
     * Create a {@link Logger} to show {@link GatewayProperties}.
     */
    private static final Logger mLogger =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Spring {@link GatewayProperties} used to track what
     * microservices have been registered with this gateway.
     */
    @Autowired
    private GatewayProperties props;

    /**
     * The static main() entry point runs this Spring application.
     */
    public static void main(String[] args) {
        // Launch the API Gateway application.
        SpringApplication.run(GatewayApplication.class, 
                              args);
    }

    /**
     * Runs after application has been constructed and displays the
     * current {@link GatewayProperties} that shows all gateway
     * routing paths.
     */
    @PostConstruct
    public void init() {
        mLogger.info("Printing current gateway properties:");
        mLogger.info(Objects.toString(props));
    }
}
