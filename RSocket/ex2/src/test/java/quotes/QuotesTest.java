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
import quotes.utils.BackpressureSubscriber;

import jakarta.annotation.PreDestroy;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

import static quotes.common.model.SubscriptionType.HANDEY;
import static quotes.common.model.SubscriptionType.ZIPPY;

/**
 * This class tests the endpoints provided by the {@link
 * QuotesApplication} microservice to receive {@link Quote} objects
 * containing phrases from Zippy th' Pinhead and Jack Handey.
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

    /**
     * Initialize the {@link Options} singleton.
     */
    @BeforeAll
    public static void setup() {
        Options.instance().parseArgs(sArgv);
    }

    /**
     * Subscribe for and receive a given number of Zippy th' Pinhead
     * and Jack Handey quotes.  This test uses a {@link ParallelFlux}
     * to demonstrate the RSocket two-way async request/stream model,
     * where each client request receives a {@link Flux} stream of
     * responses from the server.
     */
    @Test
    public void testGetAllQuotesInParallelWithBackpressure() {
        System.out.println(">>> Entering testGetAllQuotesInParallelWithBackpressure()");

        // Make backpressure-aware Subscribers that block the caller
        // for both ZIPPY and HANDEY quotes.
        Flux
            // Create a Flux that emits to Quote objects to
            // BackpressureSubscriber objects.
            .just(makeQuoteSubscriber(List.of(ZIPPY),
                                      Options.instance()
                                      .totalNumberOfQuotes()),
                  makeQuoteSubscriber(List.of(HANDEY),
                                      Options.instance()
                                      .totalNumberOfQuotes()))

            // Convert the Flux to a ParallelFlux.
            .parallel()

            // Run on a Scheduler that supports blocking.
            .runOn(Schedulers.boundedElastic())

            // Wait for the Subscribers to complete.
            .map(BackpressureSubscriber::await)

            // Convert the ParallelFlux to a Flux.
            .sequential()

            // Block until all processing is done.
            .blockLast();

        System.out.println("<<< Leaving testGetAllQuotesInParallelWithBackpressure()");
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
            // Create a Flux that emits to a BackpressureSubscriber.
            .just(makeQuoteSubscriber
                  (List.of(ZIPPY, HANDEY),
                   Options.instance().totalNumberOfQuotes() * 10))

            // Wait for the Subscriber to complete.
            .map(BackpressureSubscriber::await)

            // Block until all processing is done.
            .blockLast();

        System.out.println("<<< Leaving testGetAllQuotesWithBackpressure()");
    }

    /**
     * This factory method creates a {@link BackpressureSubscriber}
     * that blocks until the processing is complete.
     *
     * @param type The type of quote to request (e.g., ZIPPY, HANDEY,
     *             etc.)
     * @param totalNumberOfQuotes The number of {@link Quote} objects
     *                            to process
     * @return A {@link BackpressureSubscriber} that blocks until the
     *         processing is complete
     */
    private BackpressureSubscriber<Quote> makeQuoteSubscriber
        (List<SubscriptionType> type,
         long totalNumberOfQuotes) {
        // Create a backpressure-aware subscriber that blocks until
        // the processing is complete.
        BackpressureSubscriber<Quote> backpressureSubscriber =
            new BackpressureSubscriber<>
            (quote ->
             // Print each quote emitted by the Flux<Quote>.
             Options.debug(TAG, int2String(quote) + quote.getQuote()),

             // Print an error message.
             throwable -> Options.debug(TAG, "failure: " + throwable),

             // Print a completion message.
             () -> Options.debug(TAG, "completed"),

             // The number of requests to process before requesting
             // the next batch.
             Options.instance().requestSize());

        // Get a confirmed Subscription from the server.
        Mono<Subscription> subscription = mQuotesProxy
            .subscribe(UUID.randomUUID(), type);

        mQuotesProxy
            // Use the confirmed Subscription to get a Flux that emits
            // Quote objects from the server.
            .getAllQuotes(subscriptionRequest)

            // Limit the number of quotes to be handled.
            .take(totalNumberOfQuotes)

            // Cancel the Subscription when all processing is
            // complete.
            .doFinally(signalType -> mQuotesProxy
                       .cancelUnconfirmed(subscriptionRequest)
                       .subscribe())

            // Subscribe to the BackpressSubscriber, which enforces
            // flow control.
            .subscribe(backpressureSubscriber);

        // Return the BackpressureSubscriber.
        return backpressureSubscriber;
    }

    /**
     * Map a {@link Quote} object to a string that indicates the type
     * of quote.
     *
     * @param quote A {@link Quote} object
     * @return A string that indicates the type of {@link Quote}
     */
    private static String int2String(Quote quote) {
        return quote.getType() == ZIPPY.ordinal()
            ? "[Zippy] "
            : "[Handey] ";
    }

    /**
     * Close the connection to the Quotes server.
     */
    @PreDestroy
    public void closeConnection() {
        Options.print(TAG, ">>> Entering closeConnection()");

        // Instruct the server to shut down.
        mQuotesProxy.closeConnection();
        Options.print(TAG, "<<< Leaving closeConnection()");
    }
}
