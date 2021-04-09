package zippyisms.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import zippyisms.datamodel.Subscription;
import zippyisms.datamodel.SubscriptionStatus;
import zippyisms.datamodel.ZippyQuote;

import java.util.UUID;

import static zippyisms.application.ZippyMicroserviceClient.*;

/**
 * This class tests the endpoints provided by the ZippyApplication
 * microservice for each of the four interaction models supported by
 * RSocket.  The @SpringBootTest annotation tells Spring to look for a
 * main configuration class (e.g., one with @SpringBootApplication)
 * and use that to start a Spring application context.
 */
@SpringBootTest
public class ZippyMicroserviceTest {
    /**
     * The number of Zippy th' Pinhead quotes to process.
     */
    private static final int sNUMBER_OF_INDICES = 5;

    /**
     * This object connects to the Spring controller running the
     * RSocket server and its associated endpoints.  The
     *
     * @Autowired annotation marks this field to be initialized via
     * Spring's dependency injection facilities, where an object
     * receives other objects that it depends on (in this case, by
     * creating a connected RSocketRequester).
     */
    @Autowired
    private Mono<RSocketRequester> zippyQuoteRequester;

    /**
     * Get/print/test that specified number of random Zippy th'
     * Pinhead quotes are received.  This method demonstrates a
     * two-way RSocket bi-directional channel call where a Flux stream
     * is sent to the server and the server returns a Flux in
     * response.
     */
    @Test
    public void testGetRandomQuotes() {
        System.out.println("Entering testGetRandomQuotes()");

        // Make random indices needed for the test.
        Integer[] randomIndices = makeRandomIndices(zippyQuoteRequester,
                                                    sNUMBER_OF_INDICES);

        // Create a Flux that emits Zippy th' Pinhead quotes at the
        // random indices emitted by the randomZippyQuotes Flux.
        Flux<ZippyQuote> zippyQuotes = getRandomQuotes(zippyQuoteRequester,
                                                       randomIndices)

            // Print the Zippyisms emitted by the Flux<ZippyQuote>.
            .doOnNext(m -> System.out.println("Quote ("
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
    public void testSubscribeAndCancel() {
        System.out.println("Entering testSubscribeAndCancel()");

        // Create a Mono<SubscriptionRequest>.
        Mono<Subscription> subscriptionRequest =
            // Subscribe using a random ID.
            subscribe(zippyQuoteRequester,
                      UUID.randomUUID())

            // Print the results as a diagnostic.
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

        // Perform a confirmed cancellation of the subscription
        // (should succeed).
        Mono<Subscription> mono = cancelConfirmed(zippyQuoteRequester,
                                                  subscriptionRequest);

        // Test that the subscription was successfully cancelled.
        StepVerifier
            .create(mono)
            .expectNextMatches(r -> r
                               .getStatus()
                               .equals(SubscriptionStatus.CANCELLED))
            .verifyComplete();

        // Try to cancel the subscription (will intentionally fail).
        mono = cancelConfirmed(zippyQuoteRequester,
                               UUID.randomUUID());

        // Test that the subscription was unsuccessfully cancelled.
        StepVerifier
            .create(mono)
            .expectNextMatches(r -> r
                               .getStatus()
                               .equals(SubscriptionStatus.ERROR))
            .verifyComplete();
    }

    /**
     * Subscribe for and receive sNUMBER_OF_QUOTES of Zippy th'
     * Pinhead quotes.  This method demonstrates the async RSocket
     * request/stream model, where each request receives a Flux stream
     * of responses from the server.
     */
    @Test
    public void testValidSubscribeForQuotes() {
        System.out.println("Entering testValidSubscribeForQuotes()");

        // Get a confirmed SubscriptionRequest from the server.
        Mono<Subscription> subscriptionRequest = subscribe(zippyQuoteRequester,
                                                           UUID.randomUUID());

        // Use the confirmed SubscriptionRequest to get a Flux that
        // emits ZippyQuote objects from the server.
        Flux<ZippyQuote> zippyQuotes = getAllQuotes(zippyQuoteRequester,
                                                    subscriptionRequest)

            // Print each Zippyism emitted by the Flux<ZippyQuote>.
            .doOnNext(m -> System.out.println("Quote: " + m.getZippyism()))

            // Only emit sNUMBER_OF_QUOTES.
            .take(sNUMBER_OF_INDICES);

        // Ensure the first five results come in the right order.
        StepVerifier.create(zippyQuotes)
            .expectNextMatches(m -> m
                               .getZippyism()
                               .equals("All of life is a blur of Republicans and meat!"))
            .expectNextMatches(m -> m
                               .getZippyism()
                               .equals("..Are we having FUN yet...?"))
            .expectNextMatches(m -> m
                               .getZippyism()
                               .equals("Life is a POPULARITY CONTEST!  I'm REFRESHINGLY CANDID!!"))
            .expectNextMatches(m -> m
                               .getZippyism()
                               .equals("You were s'posed to laugh!"))
            .expectNextMatches(m -> m
                               .getZippyism()
                               .equals("Fold, fold, FOLD!!  FOLDING many items!!"))
            .verifyComplete();
    }

    /**
     * Try to subscribe for and receive sNUMBER_OF_QUOTES of Zippy th'
     * Pinhead quotes, which should fail because the
     * SubscriptionRequest has been cancelled.
     */
    @Test
    public void testInvalidSubscribeForQuotes() {
        System.out.println("Entering testInvalidSubscribeForQuotes()");

        // Get a confirmed SubscriptionRequest from the server.
        Mono<Subscription> subscriptionRequest =
            // Subscribe using a random ID.
            subscribe(zippyQuoteRequester,
                      UUID.randomUUID())

            // Print the results as a diagnostic.
            .doOnNext(r ->
                      System.out.println("subscribe-returned::"
                                         + r.getRequestId()
                                         + ":" + r.getStatus()));

        // Ensure that the subscriptionRequest's status is CONFIRMED.
        StepVerifier
            .create(subscriptionRequest)
            .expectNextMatches(r -> r
                               .getStatus()
                               .equals(SubscriptionStatus.CONFIRMED))
            .verifyComplete();

        // Perform an unconfirmed cancellation of subscriptionRequest.
        Mono<Void> mono = cancelUnconfirmed(zippyQuoteRequester,
                                            subscriptionRequest);

        // Test that the mono completes, which is the best we can do
        // since there's no useful return value.
        StepVerifier
            .create(mono)
            .verifyComplete();

        // Attempt to get all the Zippy th' Pinhead quotes, which will
        // fail since the the subscriptionRequest was cancelled.
        Flux<ZippyQuote> zippyQuotes = getAllQuotes(zippyQuoteRequester,
                                                    subscriptionRequest);

        // Ensure the Flux completes with an error since we passed a
        // cancelled Subscription.
        StepVerifier.create(zippyQuotes)
            .expectError(IllegalAccessException.class)
            .verify();
    }
}
