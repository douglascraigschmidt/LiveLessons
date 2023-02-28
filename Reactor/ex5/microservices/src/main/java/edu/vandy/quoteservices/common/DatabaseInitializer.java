package edu.vandy.quoteservices.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import io.r2dbc.spi.ConnectionFactory;

/**
 * This configuration bean class is created by each microservice
 * application and will create the microservice database schema
 * and also populate database tables with sample data.
 *
 * The {@code @Configuration} annotation indicates that a class
 * declares one or more {@code @Bean} methods and may be processed by
 * the Spring container to generate bean definitions and service
 * requests for those beans at runtime.
 *
 * The {@code @Value} annotation injects values into fields in
 * Spring-managed beans from application properties.
 */
@Configuration
public class DatabaseInitializer {
    /**
     * The database schema location for each microservices is defined
     * as a property in each microservice resource properties file.
     * The @Value annotation will automatically assign the property
     * value to the class member.
     */
    @Value("${spring.application.sql.schema-locations:}")
    String schema;

    /**
     * The database data location for each microservices is defined
     * as a property in each microservice resource properties file.
     * The @Value annotation will automatically assign the property value
     * to the class member.
     */
    @Value("${spring.application.sql.data-locations:}")
    String data;

    /**
     * Used to set up a r2dbc database during microservice
     * initialization and clean up a database during destruction.
     *
     * @param connectionFactory Factory method injected by Spring
     * @return A database connection initializer
     */
    @Bean
    public ConnectionFactoryInitializer initializer
        (ConnectionFactory connectionFactory) {
        System.out.println("IN CONNECTIONFACTORYINITIALIZER!!!");
        var initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        var populator = new CompositeDatabasePopulator();
        if (!schema.isBlank()) {
            populator
                .addPopulators(new ResourceDatabasePopulator
                               (new ClassPathResource(schema)));
        }
        if (!data.isBlank()) {
            populator
                .addPopulators(new ResourceDatabasePopulator
                               (new ClassPathResource(data)));
        }

        initializer.setDatabasePopulator(populator);

        return initializer;
    }
}
