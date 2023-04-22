package quotes.responder.connect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

import static quotes.common.Constants.SERVER_CONNECT;

/**
 * This controller enables RSocket clients to connect with the server
 * securely.
 *
 * Spring enables the integration of RSockets into a controller via
 * the {@code @Controller} annotation, which enables the autodetection
 * of implementation classes via classpath scanning, and the
 * {@code @MessageMapping annotation}, which maps a message onto a
 * message-handling method by matching the declared patterns to a
 * destination extracted from the message.
 *
 * Combining the {@code @Controller} annotation with the
 * {@code @ConnectMapping} annotation enables this class to declare
 * the {@code handleConnect()} method as a connection endpoint.
 */
@Controller
public class QuotesConnectController {
    /**
     * The {@link QuotesConnectService} that's associated with this
     * {@link QuotesConnectController} via Spring's dependency
     * injection facilities, where an object receives other objects
     * that it depends on (in this case, the {@link
     * QuotesConnectService}).
     */
    @Autowired
    private QuotesConnectService mService;

    /**
     * This endpoint handler is called when a client connects to the
     * server.
     *
     * @param clientRequester The {@link RSocketRequester} that's
     *                        associated with the client that's
     *                        connecting to the server
     * @param clientIdentity The identity of the client that's
     *                       connecting to the server
     */
    @ConnectMapping(SERVER_CONNECT)
    public void handleConnect
        (RSocketRequester clientRequester,
         @Payload String clientIdentity) {
        mService
            // Forward to the service.
            .handleConnect(clientRequester, clientIdentity);
    }
}
