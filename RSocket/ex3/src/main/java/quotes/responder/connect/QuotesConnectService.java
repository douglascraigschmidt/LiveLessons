package quotes.responder.connect;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import quotes.common.Options;
import quotes.common.model.Quote;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import static quotes.common.Constants.RESPONDER_RESPONSE;

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
public class QuotesConnectService {
    /**
     * Debugging tag used by Options.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * A {@link ConcurrentHashMap} of connected requesters.
     */
    private final Map<String, RSocketRequester> mConnectedRequester =
        new ConcurrentHashMap<>();

    /**
     * This hook method is called when a requester connects to the
     * responder.
     *
     * @param requester The {@link RSocketRequester} that's
     *                  associated with the requester that's
     *                  connecting to the responder
     * @param requesterIdentity The UUID identity of the requester that's
     *                          connecting to the responder
     */
    public void handleConnect
        (RSocketRequester requester,
         @Payload String requesterIdentity) {
        Options.debug(TAG, "((( Entering handleConnect()");
        
        // Handle the connection setup payload and client
        // status changes.
        handleRequesterStatusChanges(requester, requesterIdentity);

        // Finalize the connection setup with the client.
        finalizeConnectionSetup(requester);
        Options.debug(TAG, "))) Leaving handleConnect()");
    }

    /**
     * Finalize the connection setup with the requester.
     *
     * @param requester The {@link RSocketRequester} that's associated
     *                  with the requester that's connecting to the
     *                  responder
     */
    private void finalizeConnectionSetup
        (RSocketRequester requester) {
        // Protocol to finalize the connection with the requester.
        requester
            // Route the response back to the initiating requester.
            .route(RESPONDER_RESPONSE)

            // Indicate that the connection has been accepted.
            .data("Connection accepted")

            // Send the response back to the requester and get its
            // acknowledgement asynchronously.
            .retrieveMono(String.class)

            // Print the client's acknowledgment.
            .doOnNext(s -> Options
                      .debug(TAG,
                             "Requester's acknowledgement = "
                             + s))

            // Initiate the response asynchronously.
            .subscribe();
    }

    /**
     * Handle changes in the requester's connection status.
     *
     * @param requester The {@link RSocketRequester} associated
     *                        with the requester that connected to the
     *                        responder
     * @param requesterIdentity The identity of the requester that
     *                       connected with the responder
     */
    private void handleRequesterStatusChanges
        (RSocketRequester requester,
         String requesterIdentity) {
        requester
            // Get the underlying RSocket to handle the connection
            // setup payload.
            .rsocket()

            // Return a Mono that terminates when the
            // requester is terminated by any reason.
            .onClose()

            // Add a behavior triggered before the Mono
            // is subscribed to.
            .doFirst(() -> {
                    Options.debug(TAG,
                                  "Received connection from requester "
                                  + requesterIdentity);
                    // Add the request to the requester Map.
                    mConnectedRequester.put(requesterIdentity,
                                          requester);
                })

            // Add behavior triggered when the Mono completes with an
            // error.
            .doOnError(error -> {
                    // Warn when channels are closed by requesters
                    Options.debug(TAG,
                                  "Connection closed from requester "
                                  + requesterIdentity);
                    // No need to remove the request since that will
                    // occur in doFinally().
                })

            // Add behavior triggering after the Mono terminates for
            // any reason, including error or cancellation.
            .doFinally(consumer -> {
                    // Remove disconnected requester from the client list
                    Options.debug(TAG,
                                  "Disconnecting requester "
                                  + requesterIdentity);
                    // Remove the request from the requester Map.
                    mConnectedRequester.remove(requesterIdentity);
                })

            // Initiate processing asynchronously.
            .subscribe();
    }

    /**
     * Cleanup the requester connections on shutdown.
     *
     * A method annotated with {@code @PreDestroy} runs only once,
     * just before Spring removes the bean from the application
     * context and destroys the bean.
     */
    @PreDestroy
    void shutdown() {
        Options.debug(TAG, "((( Entering shutdown()");

        mConnectedRequester
            // Dispose of all requesters.
            .forEach((uuid, requester) -> {
                    Options.debug(TAG,
                                  "Detaching requester "
                                  + uuid);
                    // Dispose of the requester.
                    requester
                        .rsocket()
                        .dispose();
                });

        Options.debug(TAG, "))) Leaving shutdown()");
    }
}
