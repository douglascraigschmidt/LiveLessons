package zippyisms.client;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import zippyisms.common.Constants;

import java.time.Duration;

@Component
public class ClientBeans {
    /**
     * This factory method returns a {@link Mono} that emits a
     * connected {@link RSocketRequester}.
     *
     * @return A {@link Mono} that emits a connected {@link
     * RSocketRequester}
     */
    @Bean
    public Mono<RSocketRequester> getRSocketRequester() {
        return Mono
            // Return a Mono.
            .just(RSocketRequester
                // Create an RSocketRequester.
                .builder()

                // Use the binary encoder/decoder.
                .dataMimeType(MediaType.APPLICATION_CBOR)

                // Define the reconnect strategy to attempt to reconnect
                // twice, delaying 2 seconds between reconnection attempts.
                .rsocketConnector(rSocketConnector -> rSocketConnector
                    .reconnect(Retry.fixedDelay(2, Duration
                        .ofSeconds(2))))

                // Define the encoding/decoding strategies.
                .rsocketStrategies(RSocketStrategies.builder()
                    // Configure the binary encoders
                    // and decoders.
                    .encoders(encoders -> encoders
                        .add(new Jackson2CborEncoder()))
                    .decoders(decoders -> decoders
                        .add(new Jackson2CborDecoder()))
                    .build())

                // Use TCP to connect to the given port.
                .tcp("localhost", Constants.SERVER_PORT));
    }
}
