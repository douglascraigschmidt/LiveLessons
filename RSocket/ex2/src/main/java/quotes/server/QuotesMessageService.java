package quotes.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import quotes.common.Options;
import quotes.common.model.Quote;
import quotes.common.model.Subscription;
import quotes.common.model.SubscriptionStatus;
import quotes.repository.ReactiveQuoteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class defines methods that return quotes from Zippy th'
 * Pinhead and Jack Handey.
 *
 * The {@code @Service} annotation enables the auto-detection of
 * implementation classes via classpath scanning (in this case {@link
 * Quote}).
 */
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Service
public class QuotesMessageService {
    /**
     * Debugging tag used by {@link Options}.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * The {@link ReactiveQuoteRepository} used to access the quote data.
     */
    @Autowired
    private ReactiveQuoteRepository mQuoteRepository;

    /**
     * A Java {@link Set} of {@link Subscription} objects used to
     * determine whether a client has subscribed already.  The Spring
     * RSocket implementation appears to dispatch all methods in a
     * single thread, so there's no need for synchronization.
     */
    private final Set<Subscription> mSubscriptions =
        new HashSet<>();

    /**
     * A client must call this method to subscribe before attempting
     * to receive a {@link Flux} stream of {@link Quote} objects.  
     *
     * It implements a two-way async RSocket request/response call
     * that sends a response back to the client.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return An update {@link Mono} that emits the result of the
     *         {@link Subscription} request
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
     * any errors aren't returned to the client.
     *
     * This method implements a one-way async RSocket fire-and-forget
     * call that doesn't send a response back to the client.
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
     * errors are indicated to the client.
     *
     * This method implements a two-way async RSocket request/response
     * call that sends a response back to the client.
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
            // Print the subscription information as a diagnostic and
            // return the updated subscription indicating success or
            // failure.
            .map(this::cancelSubscription);
    }

    /**
     * This helper method cancels the {@link Subscription} and returns
     * an updated {@link Subscription} indicating if the cancellation
     * succeeded or failed.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Mono} that emits a {@link Subscription}
     *         indicating if the cancel request succeeded or failed
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
     * {@link Subscription}.
     *
     * This method implements the async RSocket request/stream model,
     * where each request receives a stream of responses from the
     * server.
     *
     * @param subscription A {@link Mono} that emits a {@link
     *                            Subscription}
     * @return A {@link Flux} that emits a {@link Quote} of the type
     *         associated with a {@link Subscription} or an empty
     *         {@link Flux} otherwise.
     */
    Flux<Quote> getAllQuotes
        (Mono<Subscription> subscription) {
        return subscription
            .doOnNext(sub -> Options
                      .debug(TAG,
                             "getAllQuotes::"
                             + sub.getType()
                             + ":"
                             + sub.getStatus()
                             + ":"
                             + sub.getRequestId()))

            // Check to ensure the subscription request is registered
            // and confirmed.
            .flatMapMany(sub -> mSubscriptions
                         .contains(sub)
                         // If the request is not confirmed return a
                         // Flux that emits the list of quotes.
                         ? mQuoteRepository
                         .findAllByTypeIn(type2Int(sub))

                         // If the request is not confirmed return an
                         // empty Flux.
                         : Flux.empty());
    }

    /**
     * Convert a {@link Subscription} type into a {@link List} of
     * {@link Integer} representing the type.
     *
     * @param subscription The {@link Subscription}
     * @return A {@link List} of {@link Integer} representing the type
     */
    private static List<Integer> type2Int
    (Subscription subscription) {
        return subscription
                .getType()
                .stream()
                .map(Enum::ordinal)
                .toList();
    }
}
