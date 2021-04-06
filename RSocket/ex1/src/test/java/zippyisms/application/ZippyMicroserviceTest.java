package zippyisms.application;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
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

import java.util.Random;
import java.util.UUID;

/**
 * This class tests the endpoints provided by the Zippy th' Pinhead
 * microservice for each of the four interaction models provided by
 * RSocket.
 */
@SpringBootTest
public class ZippyMicroserviceTest {
    /**
     * The number of Zippy th' Pinhead quotes to process.
     */
    private static final int sNUMBER_OF_QUOTES = 5;

    /**
     * This object connects to the Spring controller running the
     * RSocket server and its associated endpoints.
     */
    @Autowired
    private Mono<RSocketRequester> zippyQuoteRequester;

    /**
     * Get/print a specified number of random Zippy th' Pinhead
     * quotes.  This method demonstrates a two-way RSocket
     * bi-directional channel call where a Flux stream is sent to the
     * server and the server returns a Flux in response.
     */
    @Test
    public void getRandomQuotes() {
        System.out.println("Entering getRandomQuotes()");

        // Create an array of random indices.
        Integer[] randomIndices = zippyQuoteRequester
            // Invoke the call.
            .map(r -> r
                 // Send this request to the GET_NUMBER_OF_QUOTES
                 // endpoint.
                 .route(Constants.GET_NUMBER_OF_QUOTES))

            // Convert the response to a Mono<Integer> containing the
            // number of Zippyisms.
            .flatMap(r -> r.retrieveMono(Integer.class))

            // Create an Integer array containing random indices.
            .map(numberOfZippyisms -> new Random()
                 // Create the given number of random Zippyisms
                 // whose IDs are between 1 and the total
                 // number of quotes.
                 .ints(sNUMBER_OF_QUOTES,
                       1,
                       numberOfZippyisms)

                 // Convert the IntStream into a Stream.
                 .boxed()

                 // Trigger intermediate operations and store
                 // in an array.
                 .toArray(Integer[]::new))

            // Block until we've computed the randomIndices (we only
            // use block() since the StepVerifier below requires the
            // array of random indices).
            .block();

        // Double-check that randomIndices is properly initialized.
        assert randomIndices != null;

        // Create a Flux that emits Zippy th' Pinhead quotes at the
        // random indices emitted by the randomZippyQuotes Flux.
        Flux<ZippyQuote> zippyQuotes = zippyQuoteRequester
            // Invoke the call.
            .map(r ->
                 // Send this request to the GET_QUOTE endpoint.
                 r.route(Constants.GET_QUOTE)

                 // Create a Flux that emits indices for random Zippy
                 // th' Pinhead quotes and pass that as the param.
                 .data(Flux.fromArray(randomIndices)))

            // Convert the Mono result to a Flux<ZippyQuote>
            // containing a stream of ZippyQuote objects.
            .flatMapMany(r -> r.retrieveFlux(ZippyQuote.class))

            // Print the Zippyisms emitted by the Flux<ZippyQuote>.
            .doOnNext(m ->
                      System.out.println("Quote ("
                                         + m.getQuoteId() + ") = "
                                         + m.getZippyism()));

        // Ensure the results are correct, i.e., the returned quoteIds
        // match those sent to the GET_QUOTE endpoint.
        StepVerifier.create(zippyQuotes)
            .expectNextMatches(m -> m.getQuoteId() == randomIndices[0])
            .expectNextMatches(m -> m.getQuoteId() == randomIndices[1])
            .expectNextMatches(m -> m.getQuoteId() == randomIndices[2])
            .expectNextMatches(m -> m.getQuoteId() == randomIndices[3])
            .expectNextMatches(m -> m.getQuoteId() == randomIndices[4])
            .verifyComplete();
    }

    /**
     * Subscribe and cancel requests to receive Zippyisms.  This
     * method demonstrates a two-way async RSocket request/response
     * call that subscribes to retrieve a stream of Zippy t' Pinhead
     * quotes.  It also method demonstrates a one-way RSocket
     * fire-and-forget call that does not return a response.
     */
    @Test
    public void subscribeAndCancel() {
        System.out.println("Entering subscribeAndCancel()");

        // Create a random subscription id.
        Subscription request = new Subscription(UUID.randomUUID());

        // Create a Mono<SubscriptionRequest>.
        Mono<Subscription> subscriptionRequest = zippyQuoteRequester
            // Invoke the call.
            .map(r -> r
                 // Send this request to the SUBSCRIBE endpoint.
                 .route(Constants.SUBSCRIBE)

                 // Pass the SubscriptionRequest as the param.
                 .data(request))

            // Convert the response to a Mono<SubscriptionRequest>.
            .flatMap(r -> r.retrieveMono(Subscription.class))

            // Print the results.
            .doOnNext(r ->
                      System.out.println(r.getRequestId()
                                         + ":" + r.getStatus()));

        // Ensure that the subscriptionRequest's status is CONFIRMED.
        StepVerifier
            .create(subscriptionRequest)
            .expectNextMatches(r -> r
                               .getStatus()
                               .equals(SubscriptionStatus.CONFIRMED))
            .verifyComplete();

        // Set the status to CONFIRMED, which will succeed.
        request.setStatus(SubscriptionStatus.CONFIRMED);

        // Cancel the subscription (should succeed).
        Mono<Subscription> mono = zippyQuoteRequester
            .map(r -> r
                 // Send this request to the CANCEL_CONFIRMED endpoint.
                 .route(Constants.CANCEL_CONFIRMED)

                 // Pass the SubscriptionRequest as the param.
                 .data(request))

            // Convert the response to a Mono<SubscriptionRequest>.
            .flatMap(r -> r.retrieveMono(Subscription.class));

        // Test that the subscription was successfully cancelled.
        StepVerifier
            .create(mono)
            .expectNextMatches(r -> r
                               .getStatus()
                               .equals(SubscriptionStatus.CANCELLED))
            .verifyComplete();

        // Set the status to CANCELLED, which will fail.
        request.setStatus(SubscriptionStatus.CANCELLED);

        // Cancel the subscription (should fail).
        mono = zippyQuoteRequester
            .map(r -> r
                 // Send this request to the CANCEL_CONFIRMED endpoint.
                 .route(Constants.CANCEL_CONFIRMED)

                 // Pass the SubscriptionRequest as the param.
                 .data(request))

            // Convert the response to a Mono<SubscriptionRequest>.
            .flatMap(r -> r.retrieveMono(Subscription.class));

        // Test that the subscription was unsuccessfully cancelled.
        StepVerifier
            .create(mono)
            .expectNextMatches(r -> r
                               .getStatus()
                               .equals(SubscriptionStatus.ERROR))
            .verifyComplete();

        // Cancel the subscription (should fail).
        mono = zippyQuoteRequester
            .map(r -> r
                 // Send this request to the CANCEL_UNCONFIRMED
                 // endpoint.
                 .route(Constants.CANCEL_UNCONFIRMED)

                 // Pass the SubscriptionRequest as the param.
                 .data(request))

            // Convert the response to a Mono<SubscriptionRequest>.
            .flatMap(r -> r.retrieveMono(Subscription.class));

        // Test that mono completed (which is all we can do for an
        // unconfirmed cancel).
        StepVerifier
            .create(mono)
            .verifyComplete();
    }

    /**
     * Receive sNUMBER_OF_QUOTES of Zippy th' Pinhead quotes.  This
     * method demonstrates the async RSocket request/stream model,
     * where each request receives a Flux stream of responses from the
     * server.
     */
    @Test
    public void getQuotes() {
        System.out.println("Entering getQuotes()");

        // Get a confirmed SubscriptionRequest from the server.
        Mono<Subscription> subscriptionRequest = zippyQuoteRequester
            .map(r -> r
                 // Send this request to the SUBSCRIBE endpoint.
                 .route(Constants.SUBSCRIBE)

                 // Create a random subscription id and pass it as the
                 // param.
                 .data(new Subscription(UUID.randomUUID())))

            // Convert the response to a Mono<SubscriptionRequest>.
            .flatMap(r -> r.retrieveMono(Subscription.class));

        // Get a Flux that emits ZippyQuote objects from the server.
        Flux<ZippyQuote> zippyQuotes = zippyQuoteRequester
            // Wait for both Monos to emit one element and combine
            // these elements once into a Tuple2 object.
            .zipWith(subscriptionRequest)

            // Invoke the call.
            .map(tuple -> tuple
                 // Send this request to the GET_QUOTES endpoint.
                 .getT1().route(Constants.GET_QUOTES)

                 // Pass the SubscriptionRequest as the param.
                 .data(tuple.getT2()))

            // Conver the Mono response to a Flux<ZippyQuote>
            // containing a stream of ZippyQuote objects.
            .flatMapMany(r -> r.retrieveFlux(ZippyQuote.class))

            // Print each Zippyism emitted by the Flux<ZippyQuote>.
            .doOnNext(m -> System.out.println("Quote: " + m.getZippyism()))

            .take(sNUMBER_OF_QUOTES);

        // Ensure the first five results come in the right order.
        StepVerifier.create(zippyQuotes)
            .expectNextMatches(m -> m.getZippyism().equals("All of life is a blur of Republicans and meat!"))
            .expectNextMatches(m -> m.getZippyism().equals("..Are we having FUN yet...?"))
            .expectNextMatches(m -> m.getZippyism().equals("Life is a POPULARITY CONTEST!  I'm REFRESHINGLY CANDID!!"))
            .expectNextMatches(m -> m.getZippyism().equals("You were s'posed to laugh!"))
            .expectNextMatches(m -> m.getZippyism().equals("Fold, fold, FOLD!!  FOLDING many items!!"))
            .verifyComplete();
    }
}
