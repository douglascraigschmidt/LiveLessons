package quotes.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import quotes.common.Options;
import quotes.common.model.Quote;
import quotes.common.model.Subscription;
import quotes.common.model.SubscriptionStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static quotes.common.Constants.HANDEY_QUOTES;
import static quotes.common.Constants.ZIPPY_QUOTES;
import static quotes.common.model.SubscriptionType.ZIPPY;

/**
 * This class defines methods that return quotes from Zippy th'
 * Pinhead and Jack Handey.  These methods are dispatched in a single
 * thread, so there's no need for synchronization.
 * <p>
 * The {@code @Service} annotation enables the auto-detection of
 * implementation classes via classpath scanning (in this case {@link
 * Quote}).
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class QuotesMessageService {
    /**
     * Debugging tag used by Options.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * An in-memory {@link List} of all the Zippy quotes autowired by
     * Spring's dependency injection mechanism.
     */
    @Autowired
    @Qualifier(ZIPPY_QUOTES)
    public List<Quote> mZippyQuotes;

    /**
     * An in-memory {@link List} of all the Handey quotes autowired by
     * Spring's dependency injection mechanism.
     */
    @Autowired
    @Qualifier(HANDEY_QUOTES)
    public List<Quote> mHandeyQuotes;

    /**
     * A Java {@link Set} of {@link Subscription} objects used to
     * determine whether a client has subscribed already.
     */
    private final Set<Subscription> mSubscriptions =
            new HashSet<>();

    /**
     * This method must be called before attempting to receive a
     * {@link Flux} stream of {@link Quote} objects.  It implements a
     * two-way async RSocket request/response call that sends a
     * response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return An update {@link Mono} that emits the result of the
     * {@link Subscription} request
     */
    Mono<Subscription> subscribe
    (Mono<Subscription> subscriptionRequest) {
        // Return a Mono whose status has been updated to confirm the
        // subscription request.
        return subscriptionRequest
                .doOnNext(sr -> {
                    // Set the request status to confirm the
                    // subscription.
                    sr.setStatus(SubscriptionStatus.CONFIRMED);

                    // Add this request to the set of subscriptions.
                    mSubscriptions.add(sr);

                    // Print subscription information as a diagnostic.
                    Options.debug(TAG,
                            "subscribe::"
                                    + sr.getType()
                                      + ":"
                                    + sr.getStatus()
                                    + ":"
                                    + sr.getRequestId());
                });
    }

    /**
     * Cancel a {@link Subscription} in an unconfirmed manner, i.e.,
     * any errors aren't returned to the client.  This method
     * implements a one-way async RSocket fire-and-forget call that
     * doesn't send a response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     */
    void cancelSubscriptionUnconfirmed
    (Mono<Subscription> subscriptionRequest) {
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
                // Print the subscription information as a diagnostic and
                // return the updated subscription indicating success or
                // failure.
                .map(this::cancelSubscription);
    }

    /**
     * Cancel the {@link Subscription} and indicate if the
     * cancellation succeeded or failed.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Mono} that emits a {@link Subscription}
     * indicating if the cancel request succeeded or failed
     */
    private Subscription cancelSubscription
    (Subscription subscriptionRequest) {
        // Print the subscription information as a diagnostic.
        Options.debug(TAG,
                "cancelSubscription::"
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

            Options.debug(TAG,
                    subscriptionRequest.getStatus()
                            + " cancel succeeded");
        } else {
            // Indicate that the subscription wasn't registered.
            subscriptionRequest
                    .setStatus(SubscriptionStatus.ERROR);

            Options.debug(TAG,
                    subscriptionRequest.getStatus()
                            + " cancel failed");
        }

        return subscriptionRequest;
    }

    /**
     * Get a {@link Flux} that emits quotes according to the type of
     * {@link Subscription}.  This method implements the async RSocket
     * request/stream model, where each request receives a stream of
     * responses from the server.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Flux} that emits a {@link Quote} of the
     * type associated with the {@link Subscription}
     */
    Flux<Quote> getAllQuotes
    (Mono<Subscription> subscriptionRequest) {
        return subscriptionRequest
                .doOnNext(sr -> Options
                        .debug(TAG,
                                "getAllQuotes::"
                                        + sr.getType()
                                          + ":"
                                        + sr.getStatus()
                                        + ":"
                                        + sr.getRequestId()))

                // Check to ensure the subscription request is registered
                // and confirmed.
                .flatMapMany(sr -> mSubscriptions
                        .contains(sr)
                        // If the request is not confirmed return a
                        // Flux that emits the list of quotes.
                        ? Flux.fromIterable(sr.getType() == ZIPPY
                                            ? mZippyQuotes : mHandeyQuotes)

                        // If the request is not confirmed return an
                        // empty Flux.
                        : Flux.empty());
    }
}
