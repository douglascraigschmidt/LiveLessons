package zippyisms.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import zippyisms.common.Constants;

import java.time.Duration;

import static zippyisms.common.Constants.*;

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
    public Mono<RSocketRequester> getRSocketRequester
        (@Autowired RSocketStrategies strategies) {
        // Create the SocketAcceptor that will handle the
        // client's connection request.
                var responder = RSocketMessageHandler
                    .responder(strategies,
                        new ServerConnectResponseHandler());

        // Configure the binary encoders and decoders.
        var rsocketStrategies = RSocketStrategies
            .builder()
            .encoders(encoders -> encoders
                .add(new Jackson2CborEncoder()))
            .decoders(decoders -> decoders
                .add(new Jackson2CborDecoder()))
            .build();

        return Mono
            // Return a Mono.
            .just(RSocketRequester
                // Create an RSocketRequester.
                .builder()

                // Use the binary encoder/decoder.
                .dataMimeType(MediaType.APPLICATION_CBOR)

                // Define the encoding/decoding strategies.
                .rsocketStrategies(rsocketStrategies)

                .rsocketConnector(connector -> connector
                    // Define the reconnect strategy to attempt to reconnect
                    // twice, delaying 2 seconds between reconnection attempts.
                    .reconnect(Retry
                        .fixedDelay(2,
                            Duration.ofSeconds(2)))

                    // Define the socket acceptor to receive the
                    // server's response to the client connection.
                    .acceptor(responder))

                // Set up the route to connect with the server.
                .setupRoute(SERVER_CONNECT)

                // Set up the data playload to send the server.
                .setupData(LOCAL_HOST
                    + ":"
                    + Thread.currentThread())

                // Use TCP to connect to the given port on the
                // local host.
                .tcp(LOCAL_HOST, Constants.SERVER_PORT));
    }

    /**
     * This class handles the server response to the
     * client's initialization request.
     */
    static class ServerConnectResponseHandler {
        @MessageMapping(SERVER_RESPONSE)
        public Mono<String> statusUpdate(String serverResponse) {
            System.out.println("Server response = " + serverResponse);
            return Mono
                .just("Connection complete");
        }
    }
}
