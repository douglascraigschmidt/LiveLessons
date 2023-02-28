package edu.vandy.quoteservices.common;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Configuration
@PropertySource(
    value = "classpath:/application.yml",
    factory = YamlPropertySourceFactory.class)
public class ServerBeans {
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
        System.out.println("In ConnectionFactoryInitializer!!!");
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