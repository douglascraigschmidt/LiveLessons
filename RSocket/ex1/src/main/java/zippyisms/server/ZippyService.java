package zippyisms.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zippyisms.common.ServerBeans;
import zippyisms.common.model.Subscription;
import zippyisms.common.model.SubscriptionStatus;
import zippyisms.common.model.Quote;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;

import static zippyisms.common.Constants.SERVER_RESPONSE;

/**
 * This class defines methods that return zany quotes from Zippy th'
 * Pinhead.
 *
 * The {@code @Service} annotation enables the autodetection of
 * implementation classes via classpath scanning (in this case {@link
 * Quote}).
 */
@SuppressWarnings("DataFlowIssue")
@Service
public class ZippyService {
    /**
     * A {@link List} of connected clients.
     */
    private final List<RSocketRequester> mConnectedClients =
        new ArrayList<>();

    /**
     * An in-memory {@link List} of all the Zippy quotes.  The
     * {@code @Autowired} annotation ensures this field is initialized
     * via Spring's dependency injection facilities, where an object
     * receives other objects that it depends on, i.e., the {@link
     * List} of {@link Quote} objects from the {@link ServerBeans}
     * class.
     */
    @Autowired
    public List<Quote> mQuotes;

    /**
     * A Java {@link Set} of {@link Subscription} objects used to
     * determine whether a client has subscribed already.
     */
    private final Set<Subscription> mSubscriptions =
        new HashSet<>();

    /**
     * This hook method is called when a client connects
     * to the server.
     *
     * @param requester The {@link RSocketRequester} that's
     *                  associated with the client that's
     *                  connecting to the server.
     * @param clientIdentity The identity of the client that's
     *                       connecting to the server.
     */
    public void handleConnect
        (RSocketRequester requester,
         @Payload String clientIdentity) {
        // Handle the connection setup payload and client
        // status changes.
        handleClientStatusChanges(requester, clientIdentity);

        // Finalize the connection setup with the client.
        finalizeConnectionSetup(requester);
    }

    /**
     * Finalize the connection setup with the client.
     *
     * @param requester The {@link RSocketRequester} that's associated
     *                  with the client that's connecting to the server
     */
    private void finalizeConnectionSetup
        (RSocketRequester requester) {
        // Protocol to finalize the connection with the client.
        requester
            // Route the response back to the initiating client.
            .route(SERVER_RESPONSE)

            // Indicate that the connection has been accepted.
            .data("Connection accepted")

            // Send the response back to the client and
            // get its acknowledgement.
            .retrieveMono(String.class)

            // Print the client's acknowledgment.
            .doOnNext(s -> System.out
                .println("Client's acknowledgement = "
                    + s))

            // Initiate the response.
            .subscribe();
    }

    /**
     * Handle changes in the client's connection status.
     *
     * @param requester The {@link RSocketRequester} associated
     *                  with the client that connected to the server
     * @param clientIdentity The identity of the client that
     *                       connected with the server
     */
    private void handleClientStatusChanges
        (RSocketRequester requester,
         String clientIdentity) {
        requester
            // Handle the connection setup payload
            .rsocket()

            // Return a Mono that terminates when the
            // instance is terminated by any reason.
            .onClose()

            // Add a behavior triggered before the Mono
            // is subscribed to.
            .doFirst(() -> {
                System.out.println("Received connection from client "
                    + clientIdentity);
                // Add the client to the client List.
                mConnectedClients.add(requester);
            })

            // Add behavior triggered when the Mono completes with an error.
            .doOnError(error -> {
                // Warn when channels are closed by clients
                System.out.println("Connection closed from client "
                    + clientIdentity);
                mConnectedClients.remove(requester);
            })

            // Add behavior triggering after the Mono terminates for any reason,
            // including cancellation.
            .doFinally(consumer -> {
                // Remove disconnected clients from the client list
                System.out.println("Disconnecting client "
                    + clientIdentity);
                // Remove the client from the client List.
                mConnectedClients.remove(requester);
            })

            // Initiate processing.
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
        System.out.println("Detaching all remaining clients...");

        mConnectedClients
            // Dispose of all clients.
            .forEach(requester -> requester
                .rsocket()
                .dispose());

        System.out.println("Server shutting down.");
    }

    /**
     * This method must be called before attempting to receive a
     * {@link Flux} stream of Zippy quotes.  It implements a two-way
     * async RSocket request/response call that sends a response back
     * to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return An update {@link Mono} that emits the result of the {@link
     * Subscription} request
     */
    Mono<Subscription> subscribe(Mono<Subscription> subscriptionRequest) {
        // Return a Mono whose status has been updated to confirm the
        // subscription request.
        return subscriptionRequest
            .doOnNext(r -> {
                // Set the request status to confirm the subscription.
                r.setStatus(SubscriptionStatus.CONFIRMED);

                // Add this request to the set of subscriptions.
                mSubscriptions.add(r);

                // Print subscription information as a diagnostic.
                System.out.println("subscribe::"
                    + r.getRequestId()
                    + ":"
                    + r.getStatus());
            });
    }

    /**
     * Cancel a {@link Subscription} in an unconfirmed manner, i.e.,
     * any errors are not returned to the client.  This method
     * implements a one-way async RSocket fire-and-forget call that
     * does not send a response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     */
    void cancelSubscriptionUnconfirmed(Mono<Subscription> subscriptionRequest) {
        subscriptionRequest
            // Cancel the subscription without informing the client if
            // something goes wrong.
            .doOnNext(this::cancelSubscription)

            // Initiate the cancellation, which is necessary since no
            // response is sent back to the client.
            .subscribe();
    }

    /**
     * Cancel a {@link Subscription} in a confirmed manner, i.e., any
     * errors are indicated to the client.  This method implements a
     * two-way async RSocket request/response call that sends a
     * response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Mono} that emits a {@link Subscription}
     * indicating if the cancel request succeeded or failed
     */
    Mono<Subscription> cancelSubscriptionConfirmed
    (Mono<Subscription> subscriptionRequest) {
        // Try to cancel the subscription and indicate if the
        // cancellation succeeded.
        return subscriptionRequest
            // Print the subscription information as a diagnostic
            // and return the updated subscription indicating
            // success or failure.
            .map(this::cancelSubscription);
    }

    /**
     * Get a {@link Flux} that emits Zippy quotes once a second.  This
     * method implements the async RSocket request/stream model, where
     * each request receives a stream of responses from the server.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Flux} that emits Zippy quote every second
     */
    Flux<Quote> getAllQuotes(Mono<Subscription> subscriptionRequest) {
        return subscriptionRequest
            .doOnNext(r -> System.out
                .println("getAllQuotes::"
                    + r.getRequestId()
                    + ":"
                    + r.getStatus()))

            // Check to ensure the subscription request is registered
            // and confirmed.
            .flatMapMany(r -> mSubscriptions
                .contains(r)
                // If request is subscribed/confirmed return
                // a Flux that emits the list of quotes.
                ? Flux.fromIterable(mQuotes)

                // If the request is not confirmed return an
                // empty Flux.
                : Flux.empty())

            // Delay each emission by one second to demonstrate the
            // streaming capability to clients.
            .delayElements(Duration.ofSeconds(1));
    }

    /**
     * Get a {@link Flux} that emits the requested Zippy quotes.  This
     * method implements a two-way async RSocket bi-directional
     * channel call where a {@link Flux} stream is sent to the server
     * and the server returns a {@link Flux} in response.
     *
     * @param quoteIds A {@link Flux} that emits the given Zippy
     *                 {@code quoteIds}
     * @return A {@link Flux} that emits the requested Zippy quotes
     * once every second
     */
    Flux<Quote> getQuotes(Flux<Integer> quoteIds) {
        return quoteIds
            // Get the Zippy th' Pinhead quote at each quote id
            // since the List is 0-based.
            .map(this::getQuote)

            // Delay each emission by one second to demonstrate
            // RSocket's streaming capability back to clients.
            .delayElements(Duration.ofSeconds(1));
    }

    /**
     * Get the {@link Quote} associated with {@code quoteId}.
     *
     * @param quoteId The given Zippy th' Pinhead quote id
     * @return The Zippy th' Pinhead quote associated with {@code quoteId}
     */
    private Quote getQuote(Integer quoteId) {
        return mQuotes.get(quoteId);
    }

    /**
     * @return A {@link Mono} that emits the total number of Zippy th'
     * Pinhead quotes
     */
    Mono<Integer> getNumberOfQuotes() {
        return Mono
            // Return the total number of Zippy th' Pinhead quotes.
            .just(mQuotes.size());
    }

    /**
     * Cancel the {@link Subscription} and indicate if the cancellation
     * succeeded or failed.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Mono} that emits a {@link Subscription}
     * indicating if the cancel request succeeded or failed
     */
    private Subscription cancelSubscription
    (Subscription subscriptionRequest) {
        // Print the subscription information as a diagnostic.
        System.out.print("cancelSubscription::"
            + subscriptionRequest.getRequestId());

        // Check whether there's a matching request in the
        // subscription set.
        if (mSubscriptions.contains(subscriptionRequest)) {
            // Remove the request from the subscription set.
            mSubscriptions.remove(subscriptionRequest);

            // Set the request status to indicate the
            // subscription has been cancelled
            // successfully.
            subscriptionRequest
                .setStatus(SubscriptionStatus.CANCELLED);

            System.out.println(":"
                + subscriptionRequest.getStatus()
                + " cancel succeeded");
        } else {
            // Indicate that the subscription wasn't registered.
            subscriptionRequest
                .setStatus(SubscriptionStatus.ERROR);

            System.out.println(":"
                + subscriptionRequest.getStatus()
                + " cancel failed");
        }
        return subscriptionRequest;
    }
}
