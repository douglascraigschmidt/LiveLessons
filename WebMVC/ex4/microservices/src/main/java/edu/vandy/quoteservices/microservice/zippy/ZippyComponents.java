package edu.vandy.quoteservices.microservice.zippy;

import edu.vandy.quoteservices.common.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static edu.vandy.quoteservices.common.Constants.ZIPPY_QUOTES;

/**
 * This class contains a {@code Bean} annotation that can be injected
 * into classes using the Spring {@code @Autowired} annotation.
 */
@Component
@Configuration
@PropertySource("classpath:application.yml")
public class ZippyComponents {
    @Autowired
    private ZippyRepository mRepository;

    /**
     * @return Return a {@link Map} of {@link Quote} objects
     *         that were stored in the file zippy-quotes.txt
     */
    @Bean
    public Map<Integer, String> quoteMap
        (@Value("${app.dataset}") final String filePath) {
        System.out.println("zippy.getInput() " + filePath);

        try {
            // Although AtomicInteger is overkill we use it to
            // simplify incrementing the ID in the stream below.
            AtomicInteger idCount = new AtomicInteger(0);

            // Convert the filename into a pathname.
            URI uri = ClassLoader
                .getSystemResource(filePath)
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
                     new Quote(idCount.incrementAndGet(),
                               quote.stripLeading()))
                
                // Collect results into a list of ZippyQuote objects.
                .toList();

            mRepository.saveAll(list);

            System.out.println("DATABASE: successfully loaded " 
                               + list.size() 
                               + " quotes.");

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
