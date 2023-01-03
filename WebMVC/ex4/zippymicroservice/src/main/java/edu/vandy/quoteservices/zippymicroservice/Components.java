package edu.vandy.quoteservices.zippymicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import edu.vandy.quoteservices.zippymicroservice.model.Quote;

/**
 * This class contains a {@code Bean} annotation that can be injected into
 * classes using the Spring {@code @Autowired} annotation.
 */
// @@ Monte, are both of the following annotations necessary?
@Component
@Configuration
@PropertySource("classpath:application.yml")
public class Components {
    /**
     * Separator used in each dataset line entry.
     */
    private static final String SPLITTER = " ";

    @Autowired
    ZippyRepository repository;

    @Bean
    public Void vectorMap(
        @Value("${app.dataset}") final String dataset
    ) {
        System.out.println("zippy.getInput() " + dataset);

        try {
            // Although AtomicInteger is overkill we use it to
            // simplify incrementing the ID in the stream below.
            AtomicInteger idCount = new AtomicInteger(0);

            // Convert the filename into a pathname.
            URI uri = ClassLoader
                .getSystemResource(dataset)
                .toURI();

            // Open the file and get all the bytes.
            CharSequence bytes =
                new String(Files.readAllBytes(Paths.get(uri)));

            // Get a List of Zippy objects.
            var list = Pattern
                // Compile splitter into a regular expression (regex).
                .compile("@")

                // Use the regex to split the file into a stream of
                // strings.
                .splitAsStream(bytes)

                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Create a new ZippyQuote.
                .map(quote ->
                     // new Quote(String.valueOf(idCount.incrementAndGet()),
                     new Quote(idCount.incrementAndGet(),
                               quote.stripLeading()))
                
                // Collect results into a list of ZippyQuote objects.
                .toList();

            repository.saveAll(list);

            System.out.println("DATABASE: successfully loaded " 
                               + list.size() 
                               + " quotes.");

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Constructs a {@link Map} Bean that loads the cosine vector map from
     * resources.
     */

    public void insertWithQuery(Quote movie) {
        repository.save(movie);
    }
}
