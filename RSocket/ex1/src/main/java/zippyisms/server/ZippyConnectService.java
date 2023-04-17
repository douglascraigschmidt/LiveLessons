package zippyisms.server;

import jakarta.annotation.PreDestroy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import zippyisms.common.Options;
import zippyisms.common.model.Quote;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

import static zippyisms.common.Constants.SERVER_RESPONSE;

/**
 * This class defines methods that establish a connection with a
 * client.
 *
 * The {@code @Service} annotation enables the auto-detection of
 * implementation classes via classpath scanning (in this case {@link
 * Quote}).
 */
@SuppressWarnings("DataFlowIssue")
@Service
public class ZippyConnectService {
    /**
     * Debugging tag used by Options.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * A {@link ConcurrentHashMap} of connected clients.
     */
    private final Map<String, RSocketRequester> mConnectedClients =
        new ConcurrentHashMap<>();

    /**
     * This hook method is called when a client connects to the
     * server.
     *
     * @param clientRequester The {@link RSocketRequester} that's
     *                        associated with the client that's
     *                        connecting to the server
     * @param clientIdentity The UUID identity of the client that's
     *                       connecting to the server
     */
    public void handleConnect
        (RSocketRequester clientRequester,
         @Payload String clientIdentity) {
        Options.debug(TAG, "((( Entering handleConnect()");
        
        // Handle the connection setup payload and client
        // status changes.
        handleClientStatusChanges(clientRequester, clientIdentity);

        // Finalize the connection setup with the client.
        finalizeConnectionSetup(clientRequester);
        Options.debug(TAG, "))) Leaving handleConnect()");
    }

    /**
     * Finalize the connection setup with the client.
     *
     * @param clientRequester The {@link RSocketRequester} that's associated
     *                        with the client that's connecting to the server
     */
    private void finalizeConnectionSetup
        (RSocketRequester clientRequester) {
        // Protocol to finalize the connection with the client.
        clientRequester
            // Route the response back to the initiating client.
            .route(SERVER_RESPONSE)

            // Indicate that the connection has been accepted.
            .data("Connection accepted")

            // Send the response back to the client and get its
            // acknowledgement asynchronously.
            .retrieveMono(String.class)

            // Print the client's acknowledgment.
            .doOnNext(s -> Options
                      .debug(TAG,
                             "Client's acknowledgement = "
                             + s))

            // Initiate the response asynchronously.
            .subscribe();
    }

    /**
     * Handle changes in the client's connection status.
     *
     * @param clientRequester The {@link RSocketRequester} associated
     *                        with the client that connected to the server
     * @param clientIdentity The identity of the client that
     *                       connected with the server
     */
    private void handleClientStatusChanges
        (RSocketRequester clientRequester,
         String clientIdentity) {
        clientRequester
            // Get the underlying RSocket to handle the connection
            // setup payload.
            .rsocket()

            // Return a Mono that terminates when the
            // client is terminated by any reason.
            .onClose()

            // Add a behavior triggered before the Mono
            // is subscribed to.
            .doFirst(() -> {
                    Options.debug(TAG,
                                  "Received connection from client "
                                  + clientIdentity);
                    // Add the client to the client List.
                    mConnectedClients.put(clientIdentity,
                                          clientRequester);
                })

            // Add behavior triggered when the Mono completes with an
            // error.
            .doOnError(error -> {
                    // Warn when channels are closed by clients
                    Options.debug(TAG,
                                  "Connection closed from client "
                                  + clientIdentity);
                    // No need to remove the client since that will
                    // occur in doFinally().
                })

            // Add behavior triggering after the Mono terminates for
            // any reason, including error or cancellation.
            .doFinally(consumer -> {
                    // Remove disconnected clients from the client list
                    Options.debug(TAG,
                                  "Disconnecting client "
                                  + clientIdentity);
                    // Remove the client from the client List.
                    mConnectedClients.remove(clientIdentity);
                })

            // Initiate processing asynchronously.
            .subscribe();
    }

    /**
     * Cleanup the client connections on shutdown.
     *
     * A method annotated with {@code @PreDestroy} runs only once,
     * just before Spring removes the bean from the application
     * context and destroys the bean.
     */
    @PreDestroy
    void shutdown() {
        Options.debug(TAG, "((( Entering shutdown()");

        mConnectedClients
            // Dispose of all clients.
            .forEach((uuid, requester) -> {
                    Options.debug(TAG,
                                  "Detaching client "
                                  + uuid);
                    // Dispose of the client.
                    requester
                        .rsocket()
                        .dispose();
                });

        Options.debug(TAG, "))) Leaving shutdown()");
    }
}
