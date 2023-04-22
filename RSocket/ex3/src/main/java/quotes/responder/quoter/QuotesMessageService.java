package quotes.responder.quoter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import quotes.common.Options;
import quotes.common.model.Quote;
import quotes.common.model.RandomRequest;
import quotes.common.model.Subscription;
import quotes.common.model.SubscriptionStatus;
import quotes.repository.ReactiveQuoteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class defines methods that return quotes from Shakespeare.
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
     * The {@link ReactiveQuoteRepository} used to access the quote
     * data.
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
     * Get the total number of Shakespeare quotes.
     *
     * @param user The {@link UserDetails} associated with the caller
     * @return A {@link Mono} that emits the total number of
     *         Shakespeare quotes
     */
    Mono<Long> getNumberOfQuotes(UserDetails user) {
        Options.debug(TAG,
                      "getNumberOfQuotes() initiated by \""
                      + user.getUsername()
                      + "\" in the role of "
                      + user.getAuthorities());
        return mQuoteRepository
            // Return the total number of Shakespeare quotes.
            .count();
    }

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
            .map(sr -> {
                    // Create a confirmation response.
                    var subscriptionResponse =
                        new Subscription(sr.requestId(),
                                         SubscriptionStatus.CONFIRMED,
                                         sr.play());

                    // Add this request to the set of subscriptions.
                    mSubscriptions.add(subscriptionResponse);

                    // Print subscription information as a diagnostic.
                    Options.debug(TAG,
                                  "subscribe::"
                                  + subscriptionResponse.play()
                                  + ":"
                                  + subscriptionResponse.status()
                                  + ":"
                                  + subscriptionResponse.requestId());

                // Return the response.
                return subscriptionResponse;
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
     * @param subscription A {@link Mono} that emits a {@link
     *                     Subscription} 
     * @return A {@link Mono} that emits a {@link Subscription}
     *         indicating if the cancel request succeeded or failed
     */
    private Subscription cancelSubscription
        (Subscription subscription) {
        // Print the subscription information as a diagnostic.
        Options.debug(TAG,
                      "cancelSubscription::"
                      + subscription.requestId()
                      + ":"
                      + subscription.status());

        Subscription subscriptionResponse;

        // Check whether there's a matching request in the
        // subscription set.
        if (mSubscriptions.contains(subscription)) {
            // Remove the request from the subscription set.
            mSubscriptions.remove(subscription);

            // Set the request status to indicate the subscription has
            // been cancelled successfully.
            subscriptionResponse =
                new Subscription(subscription.requestId(),
                                 SubscriptionStatus.CANCELLED,
                                 subscription.play());

            Options.debug(TAG,
                          subscriptionResponse.requestId()
                          + ":"
                          + subscriptionResponse.status()
                          + " cancel succeeded");
        } else {
            subscriptionResponse =
                new Subscription(subscription.requestId(),
                                 SubscriptionStatus.ERROR,
                                 subscription.play());

            Options.debug(TAG,
                          subscriptionResponse.requestId()
                          + ":"
                          + subscriptionResponse.status()
                          + " cancel failed");
        }

        // Return the subscription response.
        return subscriptionResponse;
    }

    /**
     * Get a {@link Flux} that emits quotes according to the type of
     * {@link Subscription}.
     *
     * This method implements the async RSocket request/stream model,
     * where each request receives a stream of responses from the
     * server.
     *
     * @param subscriptionRequest A {@link Mono} that emits a {@link
     *                            Subscription} request
     * @return A {@link Flux} that emits a {@link Quote} of the type
     *         associated with a {@link Subscription} or an empty
     *         {@link Flux} otherwise.
     */
    Flux<Quote> getAllQuotes
        (Mono<Subscription> subscriptionRequest) {
        return subscriptionRequest
            .doOnNext(sr -> Options
                      .debug(TAG,
                             "getAllQuotes::"
                             + sr.play()
                             + ":"
                             + sr.status()
                             + ":"
                             + sr.requestId()))

            // Check to ensure the subscription request is registered
            // and confirmed.
            .flatMapMany(sr -> mSubscriptions
                         .contains(sr)
                         // If the request is confirmed get a Flux
                         // from the R2DBC database that emits all the
                         // Quote objects matching the given play.
                         ? mQuoteRepository
                         .findAllByPlay(sr.play())

                         // If the request is not confirmed return an
                         // empty Flux.
                         : Flux.empty());
    }

    /**
     * Get a {@link Flux} that emits the requested Shakespeare quotes
     * if the client is subscribed.
     *
     * This method implements a two-way async RSocket request/stream
     * call where a {@link RandomRequest} non-reactive type is sent to
     * the server and the server returns a {@link Flux} in response.
     *
     * @param randomRequest A non-reactive {@link RandomRequest} that
     *                      contains {@link Subscription} and random
     *                      indices array
     * @return A {@link Flux} that emits the requested Shakespeare
     *         quotes
     */
    Flux<Quote> getQuotesSubscribed
        (RandomRequest randomRequest) {
        var subscription = randomRequest
            // Get the Subcription field.
            .subscription();

        Options
            .debug(TAG,
                   "getQuotes::"
                   + subscription.requestId()
                   + ":"
                   + subscription.status());

        return mSubscriptions
            // Check whether the subscription is confirmed.
            .contains(subscription)
            // If the request is confirmed get a Flux from the R2DBC
            // database that emits Quote objects from the specified
            // random quoteIds.
            ? mQuoteRepository
            .findAllByIdIn(List.of(randomRequest.randomIndices()))

            :
            // Otherwise, if the request is not confirmed return an
            // empty Flux.
            Flux.empty();
    }
}
