package edu.vandy.quoteservices.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

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
     * @return Return a {@link List} of {@link Quote} objects
     * that stored in a file in the project resources.
     */
    @Bean
    public List<Quote> loadQuotes(
        @Value("${app.dataset}") final String filePath
    ) {
        try {
            // Used to increment the count of id's in the
            // lambda expression below.
            AtomicInteger idCount = new AtomicInteger(0);

            // Convert the filename into a pathname.
            URI uri = ClassLoader
                .getSystemResource(filePath)
                .toURI();

            // Open the file and get all the bytes.
            CharSequence bytes =
                new String(Files.readAllBytes(Paths.get(uri)));

            // Return a List of ZippyQuote objects.

            // Return the quotes.
            return Pattern
                // Compile splitter into a regular expression (regex).
                .compile("@")

                // Use the regex to split the file into a stream of
                // strings.
                .splitAsStream(bytes)

                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Create a new ZippyQuote.
                .map(quote ->
                         new Quote(idCount.getAndIncrement(),
                                 quote.stripLeading()))

                // Collect results into a list of ZippyQuote objects.
                .toList();
        }
        catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}