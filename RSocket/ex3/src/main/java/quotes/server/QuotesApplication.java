package quotes.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import quotes.common.model.Quote;
import quotes.repository.ReactiveQuoteRepository;

/**
 * Provides the context for running the QuotesApplication, which is a
 * reactive microservice that provides Zippy th' Pinhead and Jack Handey
 * quotes to clients using all four RSocket interaction models.
 *
 * The {@code @SpringBootApplication} annotation enables apps to use
 * auto-configuration, component scan, and to define extra
 * configuration on their "application" class.  
 *
 * The {@code @ComponentScan} annotation enables auto-detection of
 * beans by a Spring container.  Java classes that are decorated with
 * stereotypes such as {@code @Component}, {@code @Configuration}, and
 * {@code @Service}, are auto-detected by Spring.
 */
@SpringBootApplication
@ComponentScan("quotes")
@EntityScan(basePackageClasses =
    {Quote.class})
@EnableR2dbcRepositories(basePackageClasses =
    {ReactiveQuoteRepository.class})
public class QuotesApplication {
    public static void main(String[] args) {
        // Run the QuotesApplication microservice.
        SpringApplication.run(QuotesApplication.class,
            args);
    }

    // @Bean
    public CommandLineRunner demo(ReactiveQuoteRepository repository) {
        return args -> {
            repository.findAll()
                    .subscribe(it -> System.out.println(it.getQuote()));
        };
    }

}
