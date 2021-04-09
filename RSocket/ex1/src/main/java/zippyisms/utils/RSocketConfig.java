package zippyisms.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * This class contains @Bean methods that initialize the
 * RSocketRequester used by the RSocket clients.  The
 * @Component annotation allows Spring to automatically detect custom
 * beans, i.e., without having to write any explicit code, Spring will
 * scan the application for classes annotated with @Component,
 * instantiate them, and inject any specified dependencies into them.
 */
@Component
public class RSocketConfig {
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
}
