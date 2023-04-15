package zippyisms.common;

import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import zippyisms.common.model.Quote;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * This class contains {@code @Bean} methods that initialize various
 * components used by the RSocket client and server.  It contains a
 * {@code Bean} that creates the {@link RSocketRequester} used by the
 * RSocket clients and another {@code Bean} that creates a {@link
 * List} of {@link Quote} obtained retrieved from a file.
 * <p>
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., without having to write any explicit
 * code, Spring will scan the application for classes annotated with
 * {@code @Component}, instantiate them, and inject any specified
 * dependencies into them.
 */
@Component
public class ServerBeans {
    /**
     * @return Return a {@link List} of {@link Quote} objects
     * that were stored in the file of Zippyisms
     */
    @Bean
    public List<Quote> getInput() {
        try {
            // Although AtomicInteger is overkill we use it to
            // simplify incrementing the ID in the stream below.
            AtomicInteger idCount = new AtomicInteger(0);

            // Convert the filename into a pathname.
            URI uri = ClassLoader
                .getSystemResource("zippyisms.txt")
                .toURI();

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
                    new Quote(idCount.getAndIncrement(),
                        quote.stripLeading()))

                // Collect results into a list of ZippyQuote objects.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
