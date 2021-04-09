package zippyisms.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import zippyisms.datamodel.ZippyQuote;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * This injectable component defines a @Bean that creates a List of
 * ZippyQuote objects retrieved from a file.  The @Component
 * annotation enables Spring to automatically detect custom beans,
 * i.e., without having to write any explicit code Spring will scan an
 * application for classes annotated with @Component, instantiate
 * them, and inject any specified dependencies into them.
 */
@Component
class ZippyQuotes {
    /**
     * @return Return the file of Zippyisms as a List of ZippyQuote
     * objects.
     */
    @Bean
    public List<ZippyQuote> getInput() {
        try {
            // Although AtomicInteger is overkill we use it to
            // simplify incrementing the ID in the stream below.
            AtomicInteger idCount = new AtomicInteger(0);

            // Convert the filename into a pathname.
            URI uri = ClassLoader.getSystemResource("zippyisms.txt").toURI();

            // Open the file and get all the bytes.
            CharSequence bytes =
                new String(Files.readAllBytes(Paths.get(uri)));

            // Return a List of ZippyQuote objects.
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
                     new ZippyQuote(idCount.incrementAndGet(),
                                    quote.stripLeading()))
                
                // Collect results into a list of ZippyQuote objects.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
