package zippyisms.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import zippyisms.datamodel.Subscription;
import zippyisms.datamodel.SubscriptionStatus;
import zippyisms.datamodel.ZippyQuote;
import zippyisms.utils.Constants;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

/**
 * This class provides a client that send messages to the endpoints
 * provided by the ZippyApplication microservice to demonstrate each
 * of the four interaction models supported by RSocket.
 */
public class ZippyMicroserviceClient {
    /**
     * This factory method returns an array of random indicates that's
     * used to generate random Zippy th' Pinhead quotes.
     *
     * @param zippyQuoteRequester Connection to the RSocket
     * @param numberOfIndices The number of random indices to generate
     * @return An array of random indices.
     */
    static Integer[] makeRandomIndices(Mono<RSocketRequester> zippyQuoteRequester,
                                       int numberOfIndices) {
        // Return an array of random indicates.
        return zippyQuoteRequester
            // Send the message.
            .map(r -> r
                 // Send this request to the GET_NUMBER_OF_QUOTES
                 // endpoint.
                 .route(Constants.GET_NUMBER_OF_QUOTES))

            // Convert the response to a Mono<Integer> containing the
            // number of Zippyisms.
            .flatMap(r -> r.retrieveMono(Integer.class))

            // Create an Integer array containing random indices.
            .map(numberOfZippyisms -> new Random()
                 // Create the given number of random Zippyisms whose
                 // IDs are between 1 and the total number of quotes.
                 .ints(numberOfIndices,
                       1,
                       numberOfZippyisms)

                 // Convert the IntStream into a Stream.
                 .boxed()

                 // Trigger intermediate operations and store in an
                 // array.
                 .toArray(Integer[]::new))

            // Block until we've computed the randomIndices.
            .block();
    }

    /**
     * This method returns a Flux that emits random Zippy th' Pinhead
     * quotes.
     *
     * @param zippyQuoteRequester Connection to the RSocket server
     * @param randomIndices An array of random indices used to request the associated Zippyism
     * @return A Flux that emits random Zippyisms
     */
    static Flux<ZippyQuote> getRandomQuotes(Mono<RSocketRequester> zippyQuoteRequester,
                                            Integer[] randomIndices) {
        // Return a Flux that emits random Zippyisms.
        return zippyQuoteRequester
            // Send the message.
            .map(r ->
                 // Send this request to the GET_QUOTE endpoint.
                 r.route(Constants.GET_RANDOM_QUOTES)

                 // Create a Flux that emits indices for random Zippy
                 // th' Pinhead quotes once a second and pass that as
                 // the param.
                 .data(Flux
                       .fromArray(randomIndices)
                       .delayElements(Duration.ofSeconds(1))))

            // Convert the Mono result to a Flux<ZippyQuote>
            // containing a stream of ZippyQuote objects.
            .flatMapMany(r -> r.retrieveFlux(ZippyQuote.class));
    }

    /**
     * A factory method that creates and returns a confirmed {@link Subscription}.
     *
     * @param zippyQuoteRequester Connection to the RSocket server
     * @param uuid A unique ID to identify the subscription
     * @return A confirmed {@link Subscription}
     */
    static Mono<Subscription> subscribe(Mono<RSocketRequester> zippyQuoteRequester, UUID uuid) {
        return zippyQuoteRequester
            // Send the message.
            .map(r -> r
                // Send this request to the SUBSCRIBE endpoint.
                .route(Constants.SUBSCRIBE)

                // Create a new Subscription with a random
                // subscription Id and pass it to the param.
                .data(new Subscription(uuid)))

            // Convert the response to a Mono<SubscriptionRequest>.
            .flatMap(r -> r.retrieveMono(Subscription.class))

            // Turn this Mono into a hot source and cache last emitted
            // signals for further subscribers.
            .cache();
    }

    /**
     * Perform a confirmed cancellation on a {@link Subscription}.
     *
     * @param zippyQuoteRequester Connection to the RSocket server
     * @param subscriptionRequest A {@link Subscription} object that should be valid
     * @return A {@link Subscription} object with the status of the cancellation,
     *         which is either CANCELLED if there was a matching subscription
     *         or ERROR if there was no matching subscription.
     */
    static Mono<Subscription> cancelConfirmed(Mono<RSocketRequester> zippyQuoteRequester, 
                                              Mono<Subscription> subscriptionRequest) {
        return zippyQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            // Send the message.
            .map(tuple -> tuple
                 // Send this request to the CANCEL_CONFIRMED
                 // endpoint.
                 .getT1().route(Constants.CANCEL_CONFIRMED)

                 // Pass the SubscriptionRequest as the param.
                 .data(tuple.getT2()))

            // Convert the response to a Mono<SubscriptionRequest>.
            .flatMap(r -> r.retrieveMono(Subscription.class));
    }

    /**
     * Perform a confirmed cancellation on a {@link Subscription}.
     *
     * @param zippyQuoteRequester Connection to the RSocket server
     * @param uuid A unique ID that should identify a previous subscription.
     * @return A {@link Subscription} object with the status of the cancellation,
     *         which is either CANCELLED if there was a matching subscription
     *         or ERROR if there was no matching subscription.
     */
    static Mono<Subscription> cancelConfirmed(Mono<RSocketRequester> zippyQuoteRequester,
                                              UUID uuid) {
        return zippyQuoteRequester
            .map(r -> r
                 // Send this request to the CANCEL_CONFIRMED endpoint.
                 .route(Constants.CANCEL_CONFIRMED)

                 // Create a new Subscription that hasn't been
                 // subscribed and pass it as the param, which should
                 // fail.
                 .data(new Subscription(uuid)))

            // Convert the response to a Mono<SubscriptionRequest>.
            .flatMap(r -> r.retrieveMono(Subscription.class));
    }

    /**
     * Perform an unconfirmed cancellation on a {@link Subscription},
     * i.e., no value is returned indicating whether the {@link
     * Subscription} was cancelled.  Only use this method if the
     * {@link Subscription} is known to be valid.
     *
     * @param zippyQuoteRequester Connection to the RSocket server
     * @param subscriptionRequest A {@link Subscription} object that should be valid
     */
    static Mono<Void> cancelUnconfirmed(Mono<RSocketRequester> zippyQuoteRequester,
                                        Mono<Subscription> subscriptionRequest) {
        // Perform an unconfirmed cancellation the subscription
        // (should succeed, but we don't get a confirmation).
        return zippyQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            // Send the message.
            .map(r -> r
                // Send this request to the CANCEL_UNCONFIRMED
                // endpoint.
                .getT1().route(Constants.CANCEL_UNCONFIRMED)

                // Perform an unconfirmed cancellation of the subscription.
                 .data(r.getT2()))

            .flatMap(RSocketRequester.RetrieveSpec::send);
    }

    /**
     * This method returns a {@link Flux} that emits all Zippy th'
     * Pinhead quotes if the {@code subscriptionRequest} is valid,
     * otherwise it returns an {@link Flux} that emits an IllegalAccessException.
     *
     * @param zippyQuoteRequester Connection to the RSocket server
     * @param subscriptionRequest A {@link Subscription} object
     * @return A {@link Flux} that emits all Zippy th' Pinhead quotes
     *         if the {@code subscriptionRequest} is valid, otherwise
     *         it returns an {@link Flux} that emits an
     *         IllegalAccessException.
     */
    static Flux<ZippyQuote> getAllQuotes(Mono<RSocketRequester> zippyQuoteRequester,
                                         Mono<Subscription> subscriptionRequest) {
        return zippyQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            // Send the message.
            .map(tuple -> tuple
                 // Send this request to the GET_QUOTES endpoint.
                 .getT1().route(Constants.GET_ALL_QUOTES)

                 // Pass the SubscriptionRequest as the param.
                 .data(tuple.getT2()))

            // Conver the Mono response to a Flux<ZippyQuote>
            // containing a stream of ZippyQuote objects.
            .flatMapMany(r -> r.retrieveFlux(ZippyQuote.class))

            // Return an error exception if the subscription was
            // cancelled.
            .switchIfEmpty(Flux
                           .error(new IllegalAccessException("Subscription cancelled")));
    }
}
