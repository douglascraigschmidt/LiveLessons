package quotes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import quotes.common.Options;
import quotes.common.model.SubscriptionType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import quotes.client.QuotesProxy;
import quotes.common.model.Quote;
import quotes.common.model.Subscription;
import quotes.server.QuotesApplication;
import quotes.utils.BlockingSubscriber;

import jakarta.annotation.PreDestroy;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

import static quotes.common.model.SubscriptionType.HANDEY;
import static quotes.common.model.SubscriptionType.ZIPPY;

/**
 * This class tests the endpoints provided by the {@link
 * QuotesApplication} microservice for each of the four interaction
 * models supported by RSocket.
 * <p>
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (e.g., one with
 * {@code @SpringBootApplication}) and use that to start a Spring
 * application context.
 * <p>
 * The {@code @ComponentScan} annotation enables auto-detection of
 * beans by a Spring container.  Java classes that are decorated with
 * stereotypes such as {@code @Component}, {@code @Configuration},
 * {@code @Service} are auto-detected by Spring.
 */
@SuppressWarnings("CallingSubscribeInNonBlockingScope")
@SpringBootTest(classes = QuotesApplication.class,
    webEnvironment = SpringBootTest
        .WebEnvironment.DEFINED_PORT)
@ComponentScan("quotes")
public class QuotesTest {
    /**
     * Define the "command-line" options.
     */
    private static final String[] sArgv = new String[]{
        "-d", "true" // Enable debugging.
    };

    /**
     * Debugging tag used by Options.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * This object connects to the {@link QuotesProxy}.  The
     * {@code @Autowired} annotation ensures this field is initialized
     * via Spring's dependency injection facilities, where an object
     * receives other objects that it depends on (in this case, by
     * creating a {@link QuotesProxy}).
     */
    @Autowired
    private QuotesProxy mQuotesProxy;

    @BeforeAll
    public static void setup() {
        Options.instance().parseArgs(sArgv);
    }

    /**
     * Subscribe for and receive a given number of Zippy th' Pinhead
     * and Jack Handey quotes.  This test demonstrates the RSocket
     * two-way async request/stream model, where each client request
     * receives a {@link Flux} stream of responses from the server.
     */
    @Test
    public void testGetAllQuotesWithBackpressure() {
        System.out.println(">>> Entering testGetAllQuotesWithBackpressure()");

        // Make backpressure-aware Subscribers that block the caller
        // for both ZIPPY and HANDEY quotes.
        Flux
            // Create a Flux that emits to BlockingSubscribers.
            .just(makeQuoteSubscriber(ZIPPY),
                makeQuoteSubscriber(HANDEY))

            // Convert the Flux to a ParallelFlux.
            .parallel()

            // Run on a Scheduler that supports blocking.
            .runOn(Schedulers.boundedElastic())

            // Wait for the Subscribers to complete.
            .map(BlockingSubscriber::await)

            // Convert the ParallelFlux to a Flux.
            .sequential()

            // Block until all processing is done.
            .blockLast();

        System.out.println("<<< Leaving testGetAllQuotesWithBackpressure()");
    }

    /**
     * This factory method creates a backpressure-aware Subscriber
     * that blocks until the processing is complete.
     *
     * @param type The type of quote to request (e.g., ZIPPY, HANDEY, etc.)
     * @return A {@link BlockingSubscriber} that blocks
     * until the processing is complete
     */
    private BlockingSubscriber<Quote> makeQuoteSubscriber
    (SubscriptionType type) {
        // Create a backpressure-aware subscriber that
        // blocks until the processing is complete.
        BlockingSubscriber<Quote> blockingSubscriber =
            new BlockingSubscriber<>
                (quote ->
                    // Print each quote emitted by the Flux<Quote>.
                    Options.debug(TAG, type + ": " + quote.getQuote()),
                    throwable -> Options.debug(TAG, "failure: " + throwable),
                    () -> Options.debug(TAG, "completed"),
                    Options.instance().requestSize());

        // Get a confirmed SubscriptionRequest from the server.
        Mono<Subscription> subscriptionRequest = mQuotesProxy
            .subscribe(UUID.randomUUID(), type);

        mQuotesProxy
            // Use the confirmed SubscriptionRequest to get a Flux
            // that emits ZippyQuote objects from the server.
            .getAllQuotes(subscriptionRequest)

            // Limit the number of quotes to be handled.
            .take(Options.instance().totalNumberOfQuotes())

            // Cancel the Subscription when all the processing is
            // complete.
            .doFinally(signalType -> mQuotesProxy
                .cancelUnconfirmed(subscriptionRequest)
                .subscribe())

            // Subscribe to the blockingSubscriber, which enforces
            // flow control.
            .subscribe(blockingSubscriber);

        // Return the blockingSubscriber.
        return blockingSubscriber;
    }

    /**
     * Close the connection to the Zippy th' Pinhead quotes server.
     */
    @PreDestroy
    public void closeConnection() {
        Options.print(TAG, ">>> Entering closeConnection()");

        // Instruct the server to shut down.
        mQuotesProxy.closeConnection();
        Options.print(TAG, "<<< Leaving closeConnection()");
    }
}
