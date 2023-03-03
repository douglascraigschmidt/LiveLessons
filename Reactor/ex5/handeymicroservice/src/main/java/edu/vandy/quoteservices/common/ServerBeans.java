package edu.vandy.quoteservices.common;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;

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
     * Return a {@link ConnectionFactory} object that represents a
     * connection to an H2 in-memory database with the name "quote".
     * The options specified in the URL string are used to configure
     * the database connection.
     *
     * @return A {@link ConnectionFactory} object representing a
     *         connection to an H2 in-memory database
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories
            .get("r2dbc:h2:mem:///quote?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    }

    /**
     * Create a new {@link DatabaseClient} instance using the given
     * {@link ConnectionFactory} and {@link ReactiveTransactionManager}.
     *
     * @param connectionFactory The {@link ConnectionFactory} used to
     *                          create connections to the database
     * @param transactionManager the reactive transaction manager used
     *                           to manage transactions
     * @return a new {@link DatabaseClient} instance
     */
    @Bean
    public DatabaseClient databaseClient
        (ConnectionFactory connectionFactory,
         ReactiveTransactionManager transactionManager) {
        return DatabaseClient.create(connectionFactory);
    }

    /**
     * Create a new instance of {@link ReactiveTransactionManager}
     * that is based on the provided {@link ConnectionFactory}.
     *
     * @param connectionFactory The {@link ConnectionFactory} to 
     *                          use for the transaction management
     * @return A new instance of {@link ReactiveTransactionManager}
     *         based on the provided {@link ConnectionFactory}
     */
    @Bean
    public ReactiveTransactionManager transactionManager
        (ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
