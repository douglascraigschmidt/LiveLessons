package edu.vandy.quoteservices;

import edu.vandy.quoteservices.client.QuoteClient;
import edu.vandy.quoteservices.common.Options;
import edu.vandy.quoteservices.common.Quote;
import edu.vandy.quoteservices.utils.RunTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

import static edu.vandy.quoteservices.common.Constants.Service.HANDEY;
import static edu.vandy.quoteservices.common.Constants.Service.ZIPPY;
import static edu.vandy.quoteservices.utils.RandomUtils.makeRandomIndices;

/**
 * This program demonstrates the ability to use Spring WebMVC features
 * so a client (i.e., {@link QuoteClient}) can interact with various
 * microservices (i.e., {@code ZippyController} and {@code
 * HandeyController}) via an API {@code Gateway}.
 *
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (a {@code @SpringBootApplication}, e.g.,
 * {@code ZippyApplication}) and use that to start a Spring
 * application context to serve as the target of the tests.
 *
 * The {@code @SpringBootConfiguration} annotation indicates that a
 * class provides a Spring Boot application {@code @Configuration}.
 */
@SpringBootApplication
public class QuoteDriver
    implements CommandLineRunner {
    /**
     * Number of quotes requested.
     */
    private final static int sNUMBER_OF_QUOTES_REQUESTED = 10;

    /**
     * This object connects {@link QuoteDriver} to the {@code
     * QuoteClient}.  The {@code @Autowired} annotation ensures this
     * field is initialized via Spring dependency injection, where an
     * object receives another object it depends on (e.g., by creating
     * a {@link QuoteClient}).
     */
    @Autowired
    private QuoteClient quoteClient;

    /**
     * The main entry point into the Spring applicaition.
     */
    public static void main(String[] args) {
        // Process any command-line arguments.
        Options.instance().parseArgs(args);

        // Run the Spring application.
        SpringApplication.run(QuoteDriver.class, args);
    }

    /**
     * Spring Boot automatically calls this method after the
     * application context has been loaded to exercise the
     * {@code ZippyController} and {@code HandeyController}
     * microservices.
     */
    @Override
    public void run(String... args) {
        System.out.println("Entering QuoteDriver run()");

        // Measure the impact of caching.
        timeZippyQuotesCaching();

        /*
        // Record how long it takes to get the Zippy quotes.
        timeZippyQuotes(false);
        timeZippyQuotes(true);

        // Record how long it takes to get the Handey quotes.
        timeHandeyQuotes(false);
        timeHandeyQuotes(true);

         */

        // Print the results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving QuoteDriver run()");
        System.exit(0);
    }

    /**
     * Perform operations to demonstrate the benefits of
     * server-side caching.
     */
    private void timeZippyQuotesCaching() {
        // Get a List that contains all Quote objects.
        var quotes = RunTimer
            .timeRun(() -> quoteClient
                    .getAllQuotes(ZIPPY),
                "first call to getAllQuotes()");

        // Call again and measure impact of caching.
        quotes = RunTimer
            .timeRun(() -> quoteClient
                    .getAllQuotes(ZIPPY),
                "second call to getAllQuotes()");

        // Get a random Integer that's within the
        // range of the number of Zippy quotes.
        var randomQuoteId =
            makeRandomIndices(1,
                quotes.size());

        // Get a Quote for the given quoteId.
        RunTimer
            .timeRun(() -> quoteClient
                    .getQuote(ZIPPY,
                        randomQuoteId.get(0)),
                "first call to getQuote()");

        // Call again and measure impact of caching.
        RunTimer
            .timeRun(() -> quoteClient
                    .getQuote(ZIPPY,
                        randomQuoteId.get(0)),
                "second call to getQuote()");
    }

    /**
     * Record how long it takes to get the Zippy quotes.
     *
     * @param parallel Run the queries in parallel if true,
     *                 else run sequentially
     */
    private void timeZippyQuotes(boolean parallel) {
        String type = parallel ? "Parallel " : "Sequential ";

        var zippyQuotes = RunTimer
            .timeRun(() -> runQuotes(ZIPPY, parallel),
                type + "Zippy quotes");

        // Print the Zippy quote results.
        Options.display(type + "Zippy quotes"
            + (zippyQuotes.size() == 10
            ? " successfully"
            : " unsuccessfully")
            + " received "
            + zippyQuotes.size()
            + " of 10 expected results");

        // Make a List of common Zippy words that are used to search
        // for any matches.
        var quoteOrList = List
            .of("yow",
                "yet",
                "pinhead",
                "waffle",
                "laund",
                "school",
                "fun",
                "light",
                "presiden",
                "vase");

        zippyQuotes = RunTimer
            .timeRun(() -> quoteClient
                    .searchQuotes(ZIPPY,
                        quoteOrList,
                        parallel),
                type + "Zippy searches");

        Options.display(type + "Zippy searches"
            + (zippyQuotes.size() == 114
            ? " successfully"
            : " unsuccessfully")
            + " received "
            + zippyQuotes.size()
            + " of 114 expected results");

        // Make a List of common Zippy words that are used to search
        // for all matches.
        var quoteAndList = List
            .of("yow",
                "yet");

        zippyQuotes = RunTimer
            .timeRun(() -> quoteClient
                    .searchQuotesEx(ZIPPY,
                        quoteAndList,
                        parallel),
                "Zippy searches (extended)");

        Options.display("Zippy searches (extended)"
            + (zippyQuotes.size() == 6
            ? " successfully"
            : " unsuccessfully")
            + " received "
            + zippyQuotes.size()
            + " of 6 expected results");
    }

    /**
     * Record how long it takes to get the Handey quotes.
     *
     * @param parallel Run the queries in parallel if true, else run
     *                 sequentially
     */
    private void timeHandeyQuotes(boolean parallel) {
        String type = parallel ? "Parallel " : "Sequential ";

        var handeyQuotes = RunTimer
            .timeRun(() -> runQuotes(HANDEY, parallel),
                type + "Handey quotes");

        // Print the Handey quote results.
        Options.display(type + "Handey quotes"
            + (handeyQuotes.size() == 10
            ? " successfully"
            : " unsuccessfully")
            + " received "
            + handeyQuotes.size()
            + " of 10 expected results");

        // Make a List of common Handey words.
        var quoteList = List
            .of("Dad",
                "man",
                "gold",
                "stuff",
                "poor",
                "cry");

        handeyQuotes = RunTimer
            .timeRun(() -> quoteClient
                    .searchQuotes(HANDEY,
                        quoteList,
                        parallel),
                type + "Handey searches");

        // Print the Handey quote results.
        Options.display(type + "Handey searches"
            + (handeyQuotes.size() == 14
            ? " successfully"
            : " unsuccessfully")
            + " received "
            + handeyQuotes.size()
            + " of 14 expected results");

        // Make a List of common Handey words that are used to search
        // for all matches.
        var quoteAndList = List
            .of("man",
                "that");

        handeyQuotes = RunTimer
            .timeRun(() -> quoteClient
                    .searchQuotesEx(HANDEY,
                        quoteAndList,
                        parallel),
                "Handey searches (extended)");

        Options.display("Handey searches (extended)"
            + (handeyQuotes.size() == 1
            ? " successfully"
            : " unsuccessfully")
            + " received "
            + handeyQuotes.size()
            + " of 1 expected results");
    }

    /**
     * Factors out common code for calling each microservice.
     */
    private List<Quote> runQuotes(String quoter,
                                  boolean parallel) {
        // List holding all Quote objects.
        var quotes = quoteClient
            .getAllQuotes(quoter);

        // Return the selected quotes.
        return quoteClient
            .postQuotes(quoter,
                makeRandomIndices(sNUMBER_OF_QUOTES_REQUESTED,
                    quotes.size()),
                parallel);
    }
}
