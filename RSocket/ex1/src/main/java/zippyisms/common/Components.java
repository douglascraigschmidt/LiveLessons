package zippyisms.common;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import zippyisms.common.model.ZippyQuote;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * This class contains {@code @Bean} methods that initialize various
 * components.  In particular, it contains a {@code Bean} that creates
 * the {@link RSocketRequester} used by the RSocket clients and a
 * {@code Bean} that creates a {@link List} of {@link ZippyQuote}
 * obtained retrieved from a file.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., without having to write any explicit
 * code, Spring will scan the application for classes annotated with
 * {@code @Component}, instantiate them, and inject any specified
 * dependencies into them.
 */
@Component
public class Components {
    /**
     * This factory method returns a {@link Mono} that emits a
     * connected {@link RSocketRequester}.
     *
     * @param builder The factory that creates and RSocketRequester.
     * @return a {@link Mono} that emits a connected {@link RSocketRequester}
     */
    @Bean
    public Mono<RSocketRequester> getRSocketRequester(RSocketRequester.Builder builder) {
        return Mono
            // Return a Mono.
            .just(builder
                  // Define the reconnect strategy.
                  .rsocketConnector(rSocketConnector -> rSocketConnector
                                    .reconnect(Retry.fixedDelay(2, 
                                                                Duration.ofSeconds(2))))

                  // Use binary encoder/decoder.
                  .dataMimeType(MediaType.APPLICATION_CBOR)

                  // Define the encoding/decoding strategies.
                  .rsocketStrategies(RSocketStrategies.builder()
                                     // Configure the binary encoders
                                     // and decoders.
                                     .encoders(encoders -> 
                                               encoders.add(new Jackson2CborEncoder()))
                                     .decoders(decoders -> 
                                               decoders.add(new Jackson2CborDecoder()))
                                     .build())

                  // Establish the TCP connection to the given port.
                  .tcp("localhost", Constants.SERVER_PORT));
    }

    /**
     * @return An initialized {@link RSocketRequester.Builder} object
     */
    @Bean
    public RSocketRequester.Builder getBuilder() {
        // Return an initialized builder object.
        return RSocketRequester.builder();
    }

    /**
     * @return Return a {@link List} of {@link ZippyQuote} objects
     * that were stored in the file of Zippyisms
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
