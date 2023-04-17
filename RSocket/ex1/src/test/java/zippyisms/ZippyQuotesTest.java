package zippyisms;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import zippyisms.client.ZippyProxy;
import zippyisms.common.Options;
import zippyisms.common.model.Quote;
import zippyisms.common.model.Subscription;
import zippyisms.common.model.SubscriptionStatus;
import zippyisms.server.ZippyApplication;

import jakarta.annotation.PreDestroy;
import java.util.UUID;

/**
 * This class tests the endpoints provided by the {@link
 * ZippyApplication} microservice for each of the four interaction
 * models supported by RSocket.
 * 
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (e.g., one with
 * {@code @SpringBootApplication}) and use that to start a Spring
 * application context.
 * 
 * The {@code @ComponentScan} annotation enables auto-detection of
 * beans by a Spring container.  Java classes that are decorated with
 * stereotypes such as {@code @Component}, {@code @Configuration},
 * {@code @Service} are auto-detected by Spring.
 */
@SpringBootTest(classes = ZippyApplication.class,
        webEnvironment = SpringBootTest
                .WebEnvironment.DEFINED_PORT)
@ComponentScan("zippyisms")
public class ZippyQuotesTest {
    /**
     * Define the "command-line" options.
     */
    private static final String[] sArgv = new String[] {
        "-d", "true" // Enable debugging.
    };

    /**
     * Debugging tag used by Options.
     */
    private final String TAG = getClass()
        .getSimpleName();

    /**
     * The number of Zippy th' Pinhead quotes to process.
     */
    private final int mNUMBER_OF_INDICES = 5;

    /**
     * This object connects to the {@link ZippyProxy}.  The
     * {@code @Autowired} annotation ensures this field is initialized
     * via Spring's dependency injection facilities, where an object
     * receives other objects that it depends on (in this case, by
     * creating a {@link ZippyProxy}).
     */
    @Autowired
    private ZippyProxy mZippyProxy;

    @BeforeAll
    public static void setup() {
        Options.instance().parseArgs(sArgv);
    }

    /**
     * Subscribe and cancel requests to receive Zippy quotes.  This
     * test demonstrates the RSocket two-way async request/response
     * model, where each client request receives a confirmation
     * response from the server.
     */
    @Test
    public void testSubscribeAndCancel() {
        Options.print(TAG, ">>> Entering testSubscribeAndCancel()");

        // Create a random subscription ID.
        var subscriptionId = UUID.randomUUID();

        // Create a Mono<SubscriptionRequest>.
        Mono<Subscription> subscriptionRequest = mZippyProxy
            // Subscribe using the random subscription ID.
            .subscribe(subscriptionId)

            // Print the results as a diagnostic.
            .doOnNext(sr ->
                      Options.debug(TAG,
                                    "sending subscriptionRequest "
                                    + sr.getRequestId()
                                    + ":" + sr.getStatus()));

        // Ensure that the subscriptionRequest's status is CONFIRMED.
        StepVerifier
            .create(subscriptionRequest)
            .expectNextMatches(sr -> sr
                               .getStatus()
                               .equals(SubscriptionStatus.CONFIRMED))
            .verifyComplete();

        Mono<Subscription> subscriptionResponse = mZippyProxy
            // Perform a confirmed cancellation of the valid
            // subscription ID, which should succeed.
            .cancelConfirmed(mZippyProxy
                             .subscribe(subscriptionId));

        // Test that the subscription was successfully cancelled.
        StepVerifier
            .create(subscriptionResponse)
            .expectNextMatches(sr -> sr
                               .getStatus()
                               .equals(SubscriptionStatus.CANCELLED))
            .verifyComplete();

        subscriptionResponse = mZippyProxy
            // Try to cancel the subscription, which intentionally
            // should fail since there was no registered subscription
            // with this ID.
            .cancelConfirmed(UUID.randomUUID());

        // Test that the subscription was unsuccessfully cancelled.
        StepVerifier
            .create(subscriptionResponse)
            .expectNextMatches(sr -> sr
                               .getStatus()
                               .equals(SubscriptionStatus.ERROR))
            .verifyComplete();

        Options.print(TAG, "<<< Leaving testSubscribeAndCancel()");
    }

    /**
     * Subscribe for and receive a given number of Zippy th' Pinhead
     * quotes.  This test demonstrates the RSocket two-way async
     * request/stream model, where each client request receives a
     * {@link Flux} stream of responses from the server.
     */
    @Test
    public void testValidSubscribeForQuotes() {
        Options.print(TAG, ">>> Entering testValidSubscribeForQuotes()");

        Mono<Subscription> subscriptionRequest = mZippyProxy
            // Get a confirmed SubscriptionRequest from the server.
            .subscribe(UUID.randomUUID());

        Flux<Quote> zippyQuotes = mZippyProxy
            // Use the confirmed SubscriptionRequest to get a Flux
            // that emits ZippyQuote objects from the server.
            .getAllQuotes(subscriptionRequest)

            // Print each Zippy quote emitted by the Flux<ZippyQuote>.
            .doOnNext(m ->
                      Options.debug(TAG, "Quote: " + m.getQuote()))

            // Only emit mNUMBER_OF_QUOTES.
            .take(mNUMBER_OF_INDICES);

        // Ensure all the mNUMBER_OF_INDICES responses appear in the
        // right order.
        StepVerifier.create(zippyQuotes)
            .expectNextMatches(m -> m
                               .getQuote()
                               .equals("All of life is a blur of Republicans and meat!"))
            .expectNextMatches(m -> m
                               .getQuote()
                               .equals("..Are we having FUN yet...?"))
            .expectNextMatches(m -> m
                               .getQuote()
                               .equals("Life is a POPULARITY CONTEST!  I'm REFRESHINGLY CANDID!!"))
            .expectNextMatches(m -> m
                               .getQuote()
                               .equals("You were s'posed to laugh!"))
            .expectNextMatches(m -> m
                               .getQuote()
                               .equals("Fold, fold, FOLD!!  FOLDING many items!!"))
            .verifyComplete();

        Options.print(TAG, "<<< Leaving testValidSubscribeForQuotes()");
    }

    /**
     * Try to subscribe for and receive Zippy th' Pinhead quotes,
     * which intentionally fails because the {@link Subscription} has
     * been cancelled.  It also demonstrates the RSocket one-way async
     * fire-and-forget model that does not return a response.
     */
    @Test
    public void testInvalidSubscribeForAllQuotes() {
        Options.print(TAG, ">>> Entering testInvalidSubscribeForQuotes()");

        // Get a confirmed SubscriptionRequest from the server.
        Mono<Subscription> subscriptionRequest = mZippyProxy
            // Subscribe using a random ID.
            .subscribe(UUID.randomUUID())

            // Print the results as a diagnostic.
            .doOnNext(sr ->
                      Options.debug(TAG, "subscribe-returned::"
                                    + sr.getRequestId()
                                    + ":" + sr.getStatus()));

        // Ensure that the subscriptionRequest's status is CONFIRMED.
        StepVerifier
            .create(subscriptionRequest)
            .expectNextMatches(sr -> sr
                               .getStatus()
                               .equals(SubscriptionStatus.CONFIRMED))
            .verifyComplete();

        Mono<Void> mono = mZippyProxy
            // Perform a one-way unconfirmed cancellation of
            // subscriptionRequest.
            .cancelUnconfirmed(subscriptionRequest);

        // Test that the mono completes, which is the best we can do
        // since no useful value is returned from a one-way message.
        StepVerifier
            .create(mono)
            .verifyComplete();

        Flux<Quote> zippyQuotes = mZippyProxy
            // Attempt to get all the Zippy th' Pinhead quotes, which
            // should intentionally fail since the subscriptionRequest
            // was cancelled.
            .getAllQuotes(subscriptionRequest);

        // Ensure the Flux completes with an IllegalAccessException
        // since we passed a cancelled Subscription.
        StepVerifier
            .create(zippyQuotes)
            .expectError(IllegalAccessException.class)
            .verify();

        Options.print(TAG, "<<< Leaving testInvalidSubscribeForQuotes()");
    }

    /**
     * Get/print/test that a given number of random Zippy th' Pinhead
     * quotes are received.  This method demonstrates the RSocket
     * two-way async bidirectional channel model where a {@link Flux}
     * stream is sent to the server and the server returns a {@link
     * Flux} in response.
     */
    @Test
    public void testGetRandomQuotes() {
        Options.print(TAG, ">>> Entering testGetRandomQuotes()");

        var randomIndices = mZippyProxy
            // Make random indices needed for the test.
            .makeRandomIndices(mNUMBER_OF_INDICES)

            // Turn this Mono into a hot source and cache last emitted
            // signals for later subscribers.
            .cache();

        // Get a Flux that emits Zippy th' Pinhead quotes from
        // the server.
        var zippyQuotes = mZippyProxy
            // Create a Flux that emits Zippy th' Pinhead quotes at
            // the random indices emitted by the randomZippyQuotes
            // Flux.
            .getRandomQuotes(randomIndices)

            // Print the Zippyisms emitted by the Flux<ZippyQuote>.
            .doOnNext(quote -> Options
                      .debug(TAG,
                             "Quote ("
                             + quote.getQuoteId() + ") = "
                             + quote.getQuote()));

        // Get the random indices emitted by the randomIndices().
        var ri = randomIndices.block();

        assert ri != null;

        // Ensure the results are correct, i.e., the returned quoteIds
        // match those sent to the GET_RANDOM_QUOTES endpoint.
        StepVerifier
            .create(zippyQuotes)
            .expectNextMatches(m -> m
                               .getQuoteId() == ri[0])
            .expectNextMatches(m -> m
                               .getQuoteId() == ri[1])
            .expectNextMatches(m -> m
                               .getQuoteId() == ri[2])
            .expectNextMatches(m -> m
                               .getQuoteId() == ri[3])
            .expectNextMatches(m -> m
                               .getQuoteId() == ri[4])
            .verifyComplete();

        Options.print(TAG, "<<< Leaving testGetRandomQuotes()");
    }

    /**
     * Close the connection to the Zippy th' Pinhead quotes server.
     */
    @PreDestroy
    public void closeConnection() {
        Options.print(TAG, ">>> Entering closeConnection()");

        // Instruct the server to shut down.
        mZippyProxy.closeConnection();
        Options.print(TAG, "<<< Leaving closeConnection()");
    }
}
