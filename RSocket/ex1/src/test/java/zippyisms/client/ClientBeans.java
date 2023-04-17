package zippyisms.client;

import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
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
import zippyisms.common.Options;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION;
import static zippyisms.common.Constants.*;

/**
 * The class defines a {@code @Bean} that returns a {@link Mono}
 * emitting an {@link RSocketRequester} connected to the server.
 */
@Component
public class ClientBeans {
    /**
     * Debugging tag used by Options.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * Create faux "credentials" for the user.
     */
    UsernamePasswordMetadata mCredentials =
        new UsernamePasswordMetadata
        ("d.schmidt@vanderbilt.edu",
         "you-shall-not-pass");

    /**
     * Indicate the MIME type of the credentials.
     */
    MimeType mMimeType = MimeTypeUtils
        .parseMimeType(MESSAGE_RSOCKET_AUTHENTICATION
                       .getString());

    /**
     * Configure various encoders and decoders.
     */
    RSocketStrategies mRsocketStrategies = RSocketStrategies
        .builder()
        // Binary encoder.
        .encoders(encoders -> encoders
                  .add(new Jackson2CborEncoder()))
        // Binary decoder.
        .decoders(decoders -> decoders
                  .add(new Jackson2CborDecoder()))
        // Authentication encoder.
        .encoder(new SimpleAuthenticationEncoder())
        .build();

    /**
     * Create the SocketAcceptor that will handle the server's
     * response to the client's connection request.
     */
    SocketAcceptor mResponder = RSocketMessageHandler
        .responder(mRsocketStrategies,
                   new ServerConnectResponseHandler());

    /**
     * Define the RSocketConnector strategies.
     */
    Consumer<RSocketConnector> connectorStrategies =
        connector -> connector
        // Define the reconnection strategy to attempt to
        // reconnect twice, delaying 2 seconds between
        // reconnection attempts.
        .reconnect(Retry
                   .fixedDelay(2,
                               Duration.ofSeconds(2)))

        // Define the socket acceptor to receive the server's
        // response to the client connection.
        .acceptor(mResponder);

    /**
     * This factory method returns a {@link Mono} that emits an
     * {@link RSocketRequester} connected to the server.
     *
     * @return A {@link Mono} that emits an {@link
     *         RSocketRequester} connected to the server
     */
    @Bean
    public Mono<RSocketRequester> getRSocketRequester() {
        return Mono
            // Return a Mono that emits an initialized
            // RSocketRequester.
            .just(RSocketRequester
                  // Create a builder for an RSocketRequester.
                  .builder()

                  // Use the binary encoder/decoder.
                  .dataMimeType(MediaType.APPLICATION_CBOR)

                  // Configure the encoding/decoding strategies.
                  .rsocketStrategies(mRsocketStrategies)

                  // Configure the connector strategies.
                  .rsocketConnector(connectorStrategies::accept)

                  // Set up the route to connect with the server.
                  .setupRoute(SERVER_CONNECT)

                  // Set up the data playload sent the server.
                  .setupData(UUID.randomUUID().toString())

                  // Set up the metadata containing the credentials.
                  .setupMetadata(mCredentials, mMimeType)

                  // Use TCP to connect to the given port on the local
                  // host.
                  .tcp(LOCAL_HOST, Constants.SERVER_PORT));
    }

    /**
     * This class handles the server response to the client's initial
     * connection request.
     */
    class ServerConnectResponseHandler {
        /**
         * This method takes in a server response as a parameter and
         * returns a {@link Mono} that emits an acknowledgement back
         * to the server.
         *
         * @param serverResponse A {@link String} representing the
         *                       server response
         * @return A {@link Mono} emitting a single {@link String}
         *         "Connection complete"
         */
        @MessageMapping(SERVER_RESPONSE)
        public Mono<String> statusUpdate
            (String serverResponse) {
            Options.debug(TAG,
                          "Server response = "
                          + serverResponse);
            return Mono
                // Acknowledgement sent back to the server.
                .just("Connection complete");
        }
    }
}
