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

import static edu.vandy.quoteservices.common.Constants.Service.*;
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
        System.out.println("Entering QuoteDriver main()");

        // Record how long it takes to get the Zippy quotes.
        timeZippyQuotes(false);
        timeZippyQuotes(true);

        // Record how long it takes to get the Handey quotes.
        timeHandeyQuotes(false);
        timeHandeyQuotes(true);

        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving QuoteDriver main()");
        System.exit(1);
    }

    /**
     * Record how long it takes to get the Zippy quotes.
     *
     * @param parallel Run the queries in parallel if true, else run sequentially
     */
    private void timeZippyQuotes(boolean parallel) {
        String type = parallel ? "Parallel " : "Sequential ";

        var zippyQuotes = RunTimer
            .timeRun(() -> runQuotes(ZIPPY, parallel),
                     type + "Zippy quotes");

        // Get the Zippy quotes.
        Options.display("Printing "
                        + zippyQuotes.size()
                        + " "
                        + type
                        + "Zippy quote results:");

        // Print the Zippy quote results.
        zippyQuotes
            .forEach(zippyQuote -> System.out
                     .println("id = "
                              + zippyQuote.id
                              + " quote = "
                              + zippyQuote.quote));

        // Make a List of common Zippy words.
        var quoteList = List
            .of("yow",
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
                                   quoteList,
                                   parallel),
                     type + "Zippy searches");

        zippyQuotes
            .forEach(zippyQuote -> System.out
                     .println("id = "
                              + zippyQuote.id
                              + " quote "
                              + zippyQuote.quote));
    }

    /**
     * Record how long it takes to get the Handey quotes.
     *
     * @param parallel Run the queries in parallel if true, else run sequentially
     */
    private void timeHandeyQuotes(boolean parallel) {
        String type = parallel ? "Parallel " : "Sequential ";

        var handeyQuotes = RunTimer
            .timeRun(() -> runQuotes(HANDEY, parallel),
                     type + "Handey quotes");

        // Get the Handey quotes.
        Options.display("Printing "
                        + handeyQuotes.size()
                        + " "
                        + type
                        + "Handey quote results:");

        // Print the Handey quote results.
        handeyQuotes
            .forEach(handeyQuote -> System.out
                     .println("id = "
                              + handeyQuote.id
                              + " quote "
                              + handeyQuote.quote));

        handeyQuotes = quoteClient
            .searchQuotes(HANDEY,
                          List.of("dad"),
                          parallel);

        handeyQuotes
            .forEach(handeyQuote -> System.out
                     .println("id = "
                              + handeyQuote.id
                              + " quote "
                              + handeyQuote.quote));
    }

    /**
     * Record how long it takes to get the Berra quotes.
     *
     * @param parallel Run the queries in parallel if true, else run
     *                 sequentially
     */
    private void timeBerraQuotes(boolean parallel) {
        String type = parallel ? "Parallel " : "Sequential ";

        var berraQuotes = RunTimer
            .timeRun(() -> runQuotes(BERRA, parallel),
                     type + "Berra quotes");

        // Get the Berra quotes.
        Options.display("Printing "
                        + berraQuotes.size()
                        + " "
                        + type
                        + "Berra quote results:");

        // Print the Berra quote results.
        berraQuotes
            .forEach(berraQuote -> System.out
                     .println("id = "
                              + berraQuote.id
                              + " quote "
                              + berraQuote.quote));

        berraQuotes = quoteClient
            .searchQuotes(BERRA,
                          List.of("baseball", "game", "Little League"),
                          parallel);

        berraQuotes
            .forEach(berraQuote -> System.out
                     .println("id = "
                              + berraQuote.id
                              + " quote "
                              + berraQuote.quote));
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
            .getQuotes(quoter,
                       makeRandomIndices(sNUMBER_OF_QUOTES_REQUESTED,
                                         quotes.size()),
                       parallel);
    }
}
