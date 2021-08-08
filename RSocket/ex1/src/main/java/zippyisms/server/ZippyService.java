package zippyisms.server;

import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zippyisms.common.Components;
import zippyisms.common.model.Subscription;
import zippyisms.common.model.SubscriptionStatus;
import zippyisms.common.model.ZippyQuote;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class defines methods that return zany quotes from Zippy th'
 * Pinhead.  
 *
 * The {@code @Service} annotation enables the autodetection of
 * implementation classes via classpath scanning (in this case {@link
 * ZippyQuote}).
 */
@Service
public class ZippyService {
    /**
     * An in-memory {@link List} of all the Zippy quotes.  The
     * {@code @Autowired} annotation ensures this field is initialized
     * via Spring's dependency injection facilities, where an object
     * receives other objects that it depends on, i.e., the {@link
     * List} of {@link ZippyQuote} objects from the {@link Components}
     * class.
     */
    @Autowired
    public List<ZippyQuote> mQuotes;

    /**
     * A Java {@link Set} of {@link Subscription} objects used to
     * determine whether a client has subscribed already.
     */
    private final Set<Subscription> mSubscriptions = new HashSet<>();

    /**
     * This method must be called before attempting to receive a Flux
     * stream of Zippy quotes.  It implements a two-way async RSocket
     * request/response call that sends a response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Mono} that emits the result of the {@link
     *         Subscription} request
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
        // Cancel the subscription without informing the client if
        // something goes wrong.
        subscriptionRequest
            .doOnNext(r -> {
                    // Print the subscription information as a diagnostic.
                    System.out.print("cancelSubscription::"
                                     + r.getRequestId());

                    // Check whether there's a matching request in the
                    // subscription set.
                    if (mSubscriptions.contains(r)) {
                        // Remove the request from the subscription set.
                        mSubscriptions.remove(r);

                        // Set the request status to indicate the
                        // subscription has been cancelled
                        // successfully.
                        r.setStatus(SubscriptionStatus.CANCELLED);

                        System.out.println(":"
                                           + r.getStatus()
                                           + " cancel succeeded");
                    } else {
                        // Indicate that the subscription wasn't registered.
                        r.setStatus(SubscriptionStatus.ERROR);

                        System.out.println(":"
                                           + r.getStatus()
                                           + " cancel failed");
                    }
                })

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
     *         indicating if the cancel request succeeded or failed
     */
    Mono<Subscription> cancelSubscriptionConfirmed
        (Mono<Subscription> subscriptionRequest) {
        // Try to cancel the subscription and indicate if the
        // cancellation succeeded.
        return subscriptionRequest
            .map(r -> {
                    // Print the subscription information as a diagnostic.
                    System.out.print("cancelSubscription::"
                                     + r.getRequestId());

                    // Check whether there's a matching request in the
                    // subscription set.
                    if (mSubscriptions.contains(r)) {
                        // Remove the request from the subscription
                        // set.
                        mSubscriptions.remove(r);

                        // Set the request status to indicate the
                        // subscription has been cancelled
                        // successfully.
                        r.setStatus(SubscriptionStatus.CANCELLED);

                        System.out.println(":"
                                           + r.getStatus()
                                           + " cancel succeeded");
                    } else {
                        // Indicate that the subscription wasn't
                        // registered.
                        r.setStatus(SubscriptionStatus.ERROR);

                        System.out.println(":"
                                           + r.getStatus()
                                           + " cancel failed");
                    }

                    // Return the updated subscription indicating
                    // success or failure.
                    return r;
                });
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
    Flux<ZippyQuote> getAllQuotes(Mono<Subscription> subscriptionRequest) {
        return subscriptionRequest
            .doOnNext(r ->
                      System.out.println("getAllQuotes::"
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
     * channel call where a Flux stream is sent to the server and the
     * server returns a Flux in response.
     *
     * @param quoteIds A {@link Flux} that emits the given Zippy
     *                 {@code quoteIds}
     * @return A {@link Flux} that emits the requested Zippy quotes
     *         once every second
     */
    Flux<ZippyQuote> getRandomQuotes(Flux<Integer> quoteIds) {
        return quoteIds
            // Get the Zippy th' Pinhead quote at each quote id,
            // subtracting 1 since the List is 0-based.
            .map(quoteId -> mQuotes.get(quoteId - 1))

            // Delay each emission by one second to demonstrate
            // RSocket's streaming capability back to clients.
            .delayElements(Duration.ofSeconds(1));
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
}
