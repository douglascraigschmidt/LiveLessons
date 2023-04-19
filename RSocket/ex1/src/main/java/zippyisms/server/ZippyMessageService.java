package zippyisms.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zippyisms.common.Options;
import zippyisms.common.ServerBeans;
import zippyisms.common.model.Quote;
import zippyisms.common.model.RandomRequest;
import zippyisms.common.model.Subscription;
import zippyisms.common.model.SubscriptionStatus;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class defines methods that return zany quotes from Zippy th'
 * Pinhead. These methods are dispatched in a single thread, so
 * there's no need for synchronization.
 *
 * The {@code @Service} annotation enables the auto-detection of
 * implementation classes via classpath scanning (in this case {@link
 * Quote}).
 */
@Service
public class ZippyMessageService {
    /**
     * Debugging tag used by Options.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * An in-memory {@link List} of all the Zippy quotes.  The
     * {@code @Autowired} annotation ensures this field is initialized
     * via Spring's dependency injection facilities, where an object
     * receives other objects that it depends on, i.e., the {@link
     * List} of {@link Quote} objects from the {@link ServerBeans}
     * class.
     */
    @Autowired
    private List<Quote> mQuotes;

    /**
     * A Java {@link Set} of {@link Subscription} objects used to
     * determine whether a client has subscribed already.
     */
    private final Set<Subscription> mSubscriptions =
        new HashSet<>();

    /**
     * This method must be called before attempting to receive a
     * {@link Flux} stream of Zippy quotes.  It implements a two-way
     * async RSocket request/response call that sends a response back
     * to the client.
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
                                  + sr.getRequestId()
                                  + ":"
                                  + sr.getStatus());
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
     * Get the total number of Zippy quotes.

     * @param user The {@link UserDetails} associated with the caller
     * @return A {@link Mono} that emits the total number of Zippy th'
     *         Pinhead quotes
     */
    Mono<Integer> getNumberOfQuotes(UserDetails user) {
        Options.debug(TAG,
                      "getNumberOfQuotes() initiated by \""
                      + user.getUsername()
                      + "\" in the role of "
                      + user.getAuthorities());
        return Mono
            // Return the total number of Zippy th' Pinhead quotes.
            .just(mQuotes.size());
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
    Flux<Quote> getAllQuotes
        (Mono<Subscription> subscriptionRequest) {
        return subscriptionRequest
            .doOnNext(sr -> Options
                      .debug(TAG,
                             "getAllQuotes::"
                             + sr.getRequestId()
                             + ":"
                             + sr.getStatus()))

            // Check to ensure the subscription request is registered
            // and confirmed.
            .flatMapMany(sr -> mSubscriptions
                         .contains(sr)
                         // If the request is subscribed/confirmed return
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
     * Get a {@link Flux} that emits the requested Zippy quotes if the
     * client is subscribed.
     *
     * This method implements a two-way async RSocket request/stream
     * call where a {@link RandomRequest} non-reactive type is sent
     * to the server and the server returns a {@link Flux} in
     * response.
     *
     * @param randomRequest A non-reactive {@link RandomRequest} that
     *                      contains {@link Subscription} and random
     *                      indices array
     * @return A {@link Flux} that emits the requested Zippy quotes
     *         once every second
     */
    Flux<Quote> getQuotesSubscribed
        (RandomRequest randomRequest) {
        var subscription = randomRequest
            // Get the Subcription field.
            .getSubscription();

        Options
            .debug(TAG,
                   "getQuotes::"
                   + subscription.getRequestId()
                   + ":"
                   + subscription.getStatus());

        return mSubscriptions
            // Check whether the subscription is confirmed.
            .contains(subscription)
            ? Flux
            // If it's confirmed convert the array of random
            // indices into a Flux.
            .fromArray(randomRequest.getRandomIndices())

            // Get the Zippy th' Pinhead quote at each quote id since
            // the array is 0-based.
            .map(this::getQuote)

            // Delay each emission by one second to demonstrate
            // RSocket's streaming capability back to clients.
            .delayElements(Duration.ofSeconds(1))

            : 
            // Otherwise, if the request is not confirmed return an
            // empty Flux.
            Flux.empty();
    }

    /**
     * Get a {@link Flux} that emits the requested Zippy quotes without
     * the client having to subscribe first.
     *
     * This method implements a two-way async RSocket bidirectional
     * channel call where a {@link Flux} stream is sent to the server
     * and the server returns a {@link Flux} in response.
     *
     * @param quoteIds A {@link Flux} that emits the given Zippy
     *                 {@code quoteIds}
     * @return A {@link Flux} that emits the requested Zippy quotes
     *         once every second
     */
    Flux<Quote> getQuotesUnsubscribed(Flux<Integer> quoteIds) {
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
     * @return The Zippy th' Pinhead quote associated with {@code
     *         quoteId}
     */
    private Quote getQuote(Integer quoteId) {
        // Return the quote associated with quoteId.
        return mQuotes.get(quoteId);
    }
}
