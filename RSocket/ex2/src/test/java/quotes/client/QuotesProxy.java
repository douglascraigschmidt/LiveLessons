package quotes.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import quotes.common.model.SubscriptionType;
import quotes.server.QuotesApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import quotes.common.model.Subscription;
import quotes.common.model.Quote;
import reactor.util.function.Tuple2;

import java.util.UUID;
import java.util.List;

import static quotes.common.Constants.*;

/**
 * This class provides a client whose methods can be used to send
 * messages to endpoints provided by the {@link QuotesApplication}
 * microservice.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class QuotesProxy {
    /**
     * This object connects to the Spring controller running the
     * RSocket server and its associated endpoints.
     *
     * The {@code @Autowired} annotation marks this field to be
     * initialized via Spring's dependency injection facilities, where
     * an object receives other objects that it depends on (in this
     * case, by creating a connected {@link RSocketRequester}).
     */
    @Autowired
    private Mono<RSocketRequester> mQuoteRequester;

    /**
     * A factory method that creates and returns a confirmed {@link
     * Subscription}.
     *
     * @param uuid  A unique ID to identify the subscription
     * @param type The type of the subscription
     * @return A {@link Mono} that emits a confirmed
     *         {@link Subscription}
     */
    public Mono<Subscription> subscribe(UUID uuid,
                                        SubscriptionType type) {
        return mQuoteRequester
            // Initialize the request that will be sent to the server.
            .map(r -> r
                // Set the metadata to indicate the request is for
                // the server's SUBSCRIBE endpoint.
                .route(SUBSCRIBE)

                // Create a new Subscription with the given
                // subscription ID and pass it as the data param.
                .data(new Subscription(uuid,
                                            List.of(type))))

            // Perform a two-way call using the metadata and data and
            // then convert the response to a Mono that emits the
            // resulting Subscription.
            .flatMap(r -> r
                .retrieveMono(Subscription.class))

            // Convert this Mono into a hot source, which caches the
            // emitted signals for future subscribers.
            .cache();
    }

    /**
     * A factory method that creates and returns a confirmed {@link
     * Subscription}.
     *
     * @param uuid  A unique ID to identify the subscription
     * @param typeList A {@link List} of {@link SubscriptionType}
     *                 objects
     * @return A {@link Mono} that emits a confirmed
     *         {@link Subscription}
     */
    public Mono<Subscription> subscribe
        (UUID uuid,
         List<SubscriptionType> typeList) {
        return mQuoteRequester
                // Initialize the request to send to the server.
                .map(r -> r
                        // Set the metadata to indicate the request is
                        // for the server's SUBSCRIBE endpoint.
                        .route(SUBSCRIBE)

                        // Create a new Subscription with the given
                        // subscription ID and pass it as the data
                        // param.
                        .data(new Subscription(uuid,
                                                    typeList)))

                // Perform a two-way call using the metadata and data
                // and then convert the response to a Mono that emits
                // the resulting Subscription.
                .flatMap(r -> r
                        .retrieveMono(Subscription.class))

                // Convert this Mono into a hot source, which caches
                // the emitted signals for future subscribers.
                .cache();
    }

    /**
     * Perform an unconfirmed cancellation on a {@link Subscription},
     * i.e., no value is returned indicating whether the {@link
     * Subscription} was cancelled.  Only use this method if the
     * {@link Subscription} is known to be valid.
     *
     * @param subscriptionRequest A {@link Subscription} object that
     *                            should be valid
     * @return A {@link Mono} that emits a {@link Void} object
     */
    public Mono<Void> cancelUnconfirmed
        (Mono<Subscription> subscriptionRequest) {
        // Perform an unconfirmed cancellation on the subscription,
        // which returns no confirmation.
        return mQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            // Initialize the request that will be sent to the server.
            .map(r -> r.getT1()
                // Set the metadata to indicate the request is for
                // the server's CANCEL_UNCONFIRMED endpoint.
                .route(CANCEL_UNCONFIRMED)

                // Set the subscriptionRequest as the data param.
                .data(r.getT2()))

            // Perform a fire-and-forget call using the metadata and
            // data and return a Mono<Void>.
            .flatMap(RSocketRequester.RetrieveSpec::send);
    }

    /**
     * This method returns a {@link Flux} that emits all Zippy th'
     * Pinhead and Jack Handey quotes until complete if the {@code
     * subscriptionRequest} is valid, otherwise it returns an {@link
     * Flux} that emits an {@link IllegalAccessException}.
     *
     * @param subscriptionRequest A {@link Subscription} object
     * @return A {@link Flux} that emits all {@link Quote} objects if
     *         the {@code subscriptionRequest} is valid, otherwise it
     *         returns an {@link Flux} that emits an {@link
     *         IllegalAccessException}
     */
    public Flux<Quote> getAllQuotes
    (Mono<Subscription> subscriptionRequest) {
        return mQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            // Use the Tuple2 metadata and data to pass a message to the
            // server and then convert the response to a Flux that
            // emits a stream of Quote objects.
            .flatMapMany(QuotesProxy::getAllQuotes)

            // Return an error exception if the subscription was
            // cancelled.
            .switchIfEmpty(Flux
                .error(new IllegalAccessException
                    ("Subscription was cancelled")));
    }

    /**
     * This method returns a {@link Flux} that emits all the {@link
     * Quote} objects associated with a {@link Subscription}.
     *
     * @param tuple A {@link Tuple2} that contains the {@link
     *              RSocketRequester} and the {@link Subscription}
     * @return A {@link Flux} that emits all {@link Quote} objects if
     *         the {@code subscriptionRequest} is valid, otherwise it
     *         returns an {@link Flux} that emits an {@link
     *         IllegalAccessException}
     */
    private static Flux<Quote> getAllQuotes
        (Tuple2<RSocketRequester,
                        Subscription> tuple) {
        // Extract the RSocketRequester from the tuple.
        var requester = tuple.getT1();

        // Extract the Subscription from the tuple.
        var subscription = tuple.getT2();

        return Mono
            // Initialize the request that will be sent to the server.
            .just(requester
                  // Set the metadata to indicate the request is for
                  // the server's GET_ALL_QUOTES endpoint.
                  .route(GET_ALL_QUOTES)

                  // Set the subscriptionRequest as the data param.
                  .data(subscription))

            // Use the metadata and data to pass a message to the
            // server and then convert the response to a Flux that
            // emits a stream of Quote objects.
            .flatMapMany(r -> r
                         .retrieveFlux(Quote.class));
    }

    /**
     * Close the connection with the server.
     */
    public void closeConnection() {
        mQuoteRequester.block().rsocket().dispose();
    }
}
