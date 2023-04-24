package quotes;

import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import quotes.common.Options;
import quotes.common.model.Quote;
import quotes.common.model.RandomRequest;
import quotes.common.model.Subscription;
import quotes.requester.QuotesProxy;
import quotes.requester.SentimentProxy;
import quotes.responder.QuotesApplication;
import quotes.utils.SentimentUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static quotes.common.Constants.CHAT_GPT_SENTIMENT_ANALYSIS;

/**
 * This class implements a test program that obtains famous quotes
 * from works of Shakespeare and then uses machine learning models to
 * analyze these quotes for their "sentiment".
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
@SpringBootTest(classes = QuotesApplication.class,
    webEnvironment = SpringBootTest
        .WebEnvironment.DEFINED_PORT)
@ComponentScan("quotes")
public class QuotesTest {
    /**
     * Define the "command-line" options.
     */
    private static final String[] sArgv = new String[]{
        "-d", "false" // Enable debugging.
    };

    /**
     * Debugging tag used by Options.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * This field connects to the {@link QuotesProxy} using
     * Spring's dependency injection mechanism.
     */
    @Autowired
    private QuotesProxy mQuotesProxy;

    /**
     * This field connects to the {@link SentimentProxy} using
     * Spring's dependency injection mechanism.
     */
    @Autowired
    private SentimentProxy mSentimentProxy;

    /**
     * The number of random Shakespeare quotes to process.
     */
    private final int mNUMBER_OF_INDICES = 2;

    /**
     * Initialize the {@link Options} singleton.
     */
    @BeforeAll
    public static void setup() {
        Options.instance().parseArgs(sArgv);
    }

    /**
     * Subscribe for and receive famous quotes from Shakespeare
     * play(s) and analyzes them for their '"sentiment".  This test
     * demonstrates the RSocket two-way async request/stream
     * interaction model (where each requester message receives a
     * {@link Flux} stream of {@link Quote} objects from the
     * responder) and the RSocket two-way request/response interaction
     * model (where a requester message receives a single {@link
     * Quote} object containing the sentiment analysis.
     */
    @SuppressWarnings("CallingSubscribeInNonBlockingScope")
    // @Test
    public void testAnalyzeAllQuotes() {
        Options.debug(TAG, ">>> Entering testAnalyzeAllQuotes()");

        // Get a confirmed Subscription for famous "Hamlet" quotes
        // from the responder.
        Mono<Subscription> subscription = mQuotesProxy
            .subscribe(UUID.randomUUID(), "Hamlet");

        mQuotesProxy
            // Use the confirmed Subscription to get a Flux that emits
            // Hamlet-related Quote objects from the responder.
            .getAllQuotes(subscription)

            // Deal with ChatGPT rate limiting..
            .take(Options.instance().totalNumberOfQuotes())

            // Analyze the sentiment of each Shakespeare Quote.
            .flatMap(quote -> mSentimentProxy
                // .getSentiment(CORE_NLP_SENTIMENT_ANALYSIS,
                .getSentiment(CHAT_GPT_SENTIMENT_ANALYSIS,
                    quote))

            // Output the Quote objects together with the results of
            // the sentiment analysis.
            .doOnNext(SentimentUtils::displaySentimentAnalysis)

            // Cancel the Subscription when all processing is
            // complete.
            .doFinally(signalType -> mQuotesProxy
                .cancelUnconfirmed(subscription)
                .subscribe())

            // Wait until all processing is done.
            .blockLast();

        Options.debug(TAG, "<<< Leaving testGetAllQuotesWithBackpressure()");
    }

    /**
     * Get/print/test that a given number of random Shakespeare quotes
     * are received, requiring the client to subscribe first.
     * 
     * This method demonstrates the RSocket two-way async
     * request/stream channel model where a non-reactive {@link
     * RandomRequest} object is sent to the server and the server
     * returns a {@link Flux} in response.
     */
    @SuppressWarnings("CallingSubscribeInNonBlockingScope")
    @Test
    public void testGetRandomQuotesSubscribed() {
        Options.print(TAG,
                      ">>> Entering testGetRandomQuotesSubscribed()");

        var randomIndicesM = mQuotesProxy
            // Make random indices needed for the test.
            .makeRandomIndices(mNUMBER_OF_INDICES);

        // Get the array emitted by the randomIndices().
        var randomIndices = randomIndicesM
            // Block until we get it.
            .block();

        // Get a confirmed Subscription from the server.
        var subscription = mQuotesProxy
            // Subscribe using a random ID.
            .subscribe(UUID.randomUUID(),
                       "")

            // Print the results as a diagnostic.
            .doOnNext(sr ->
                      Options.debug(TAG, "subscribe-returned::"
                                    + sr.requestId()
                                    + ":" + sr.status()))

            // Wait for the Subscription.
            .block();

        // Get a Flux that emits random Shakespeare quotes from the
        // responder.
        var bardQuotes = mQuotesProxy
            // Create a Flux that emits Shakespeare quotes at the
            // random indices emitted by the bardQuotes Flux.
            .getRandomQuotesSubscribed
            (new RandomRequest(subscription,
                               randomIndices))

            // Analyze the sentiment of each Shakespeare Quote.
            .flatMap(quote -> mSentimentProxy
                     .getSentiment(CHAT_GPT_SENTIMENT_ANALYSIS,
                                   quote))

            // Output the Quote objects together with the results of
            // the sentiment analysis.
            .doOnNext(SentimentUtils::displaySentimentAnalysis)

            // Cancel the Subscription when all processing is
            // complete.
            .doFinally(signalType -> mQuotesProxy
                .cancelUnconfirmed(Mono
                    .just(subscription))
                .subscribe())

            // Wait until all processing is done.
            .blockLast();

        Options.print(TAG,
                      "<<< Leaving testGetRandomQuotesSubscribed()");
    }


    /**
     * Close the connection to the quotes responder.
     */
    @PreDestroy
    public void closeConnection() {
        Options.print(TAG, ">>> Entering closeConnection()");

        // Instruct the responder to shut down.
        mQuotesProxy.closeConnection();
        Options.print(TAG, "<<< Leaving closeConnection()");
    }


}
