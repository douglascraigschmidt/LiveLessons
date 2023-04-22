package quotes;

import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import quotes.common.Options;
import quotes.common.model.Quote;
import quotes.common.model.Subscription;
import quotes.requester.QuotesProxy;
import quotes.requester.SentimentProxy;
import quotes.responder.QuotesApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static quotes.common.Constants.CHAT_GPT_SENTIMENT_ANALYSIS;
import static quotes.common.Constants.CORE_NLP_SENTIMENT_ANALYSIS;

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
        "-d", "true" // Enable debugging.
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
    @Test
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

            // Analyze the sentiment of each Hamlet Quote.
            .flatMap(quote -> mSentimentProxy
                // .getSentiment(CORE_NLP_SENTIMENT_ANALYSIS,
                .getSentiment(CHAT_GPT_SENTIMENT_ANALYSIS,
                    quote))

            // Output the Quote objects together with the results of
            // the sentiment analysis.
            .doOnNext(quote -> Options
                .debug(TAG, "The sentiment analysis of the quote\n\""
                    + quote.getQuote()
                    + "\"\nfrom \""
                    + quote.getPlay()
                    + "\" is as follows:\n'"
                    + formatSentiment(quote.getSentiment(), 70)
                    + "'"))

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
     * Adds newlines to a string to fit within a specified width.
     * Newlines are added only between words, not within words.
     *
     * @param sentiment  The {@link String} to add newlines to
     * @param lineLength The width to fit the {@link String} within
     * @return The formatted {@link String} with newlines
     */
    public static String formatSentiment(String sentiment,
                                         int lineLength) {
        // Create a string builder to store the result
        StringBuilder sb = new StringBuilder();
        // Split the input string into words using a regular expression that matches any non-word character preceded by any character
        List<String> lines = Stream
            .of(sentiment.split("(?<=\\W)"))
            // Group the words into lines based on the maximum line length
            .collect(Collectors
                .groupingBy(s -> sb.append(s).length() / lineLength))
            // Get the values of the grouping map (i.e., the words in each line)
            .values()
            // Join the words in each line into a string
            .stream()
            .map(list -> String.join("", list))
            // Collect the lines into a list
            .collect(Collectors.toList());
        // Join the lines with the system line separator and return the result
        return String.join(System.lineSeparator(), lines);
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
