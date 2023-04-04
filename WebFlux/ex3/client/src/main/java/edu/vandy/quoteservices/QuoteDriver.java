package edu.vandy.quoteservices;

import edu.vandy.quoteservices.client.QuoteClient;
import edu.vandy.quoteservices.common.Options;
import edu.vandy.quoteservices.common.Quote;
import edu.vandy.quoteservices.utils.RunTimer;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static edu.vandy.quoteservices.common.Constants.Service.HANDEY;
import static edu.vandy.quoteservices.common.Constants.Service.ZIPPY;
import static edu.vandy.quoteservices.utils.RandomUtils.makeRandom;

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
    private final static int sQUOTES_REQUESTED = 10;

    /**
     * This object connects {@link QuoteDriver} to the {@code
     * QuoteClient}.  The {@code @Autowired} annotation ensures this
     * field is initialized via Spring dependency injection, where an
     * object receives another object it depends on (e.g., by creating
     * a {@link QuoteClient}).
     */
    @Autowired
    private QuoteClient mQuoteClient;

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

        // Record how long it takes to get the Zippy quotes.
        timeZippyQuotes();

        // Record how long it takes to get the Handey quotes.
        timeHandeyQuotes();

        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving QuoteDriver run()");
        System.exit(0);
    }

    /**
     * Record how long it takes to get the Zippy quotes.
     */
    private void timeZippyQuotes() {
        RunTimer
            .timeRun(() -> StepVerifier
                     .create(runQuotes(ZIPPY))
                     .expectNextCount(10)
                     .as("The count wasn't as expected")
                     .verifyComplete(),
                     "Zippy runQuotes()");

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

        RunTimer
            .timeRun(() ->
                     StepVerifier
                     .create(mQuoteClient
                             .searchQuotes(ZIPPY,
                                           quoteOrList))
                     .expectNextCount(113)
                     .as("The count wasn't as expected")
                     .verifyComplete(),
                     "Zippy searchQuotes()");

        // Make a List of common Zippy words that are used to search
        // for all matches.
        var quoteAndList = List
            .of("yow",
                "yet");
        
        RunTimer
            .timeRun(() -> StepVerifier
                     .create(mQuoteClient
                             .searchQuotesEx(ZIPPY,
                                             quoteAndList))
                     .expectNextCount(6)
                     .as("The count wasn't as expected")
                     .verifyComplete(),
                     "Zippy searchQuotesEx()");
    }

    /**
     * Record how long it takes to get the Handey quotes.
     */
    private void timeHandeyQuotes() {
        RunTimer
            .timeRun(() -> StepVerifier
                     .create(runQuotes(HANDEY))
                     .expectNextCount(10)
                     .as("The runQuotes() count wasn't as expected")
                     .verifyComplete(),
                     "Handey runQuotes()");

        // Make a List of common Handey words.
        var quoteList = List
            .of("Dad",
                "man",
                "gold",
                "stuff",
                "poor",
                "cry");

        RunTimer
            .timeRun(() ->
                     StepVerifier
                     .create(mQuoteClient
                             .searchQuotes(HANDEY,
                                           quoteList))
                     .expectNextCount(14)
                     .as("The searchQuotes() count wasn't as expected")
                     .verifyComplete(),
                     "Handey searchQuotes()");

        // Make a List of common Handey words that are used to search
        // for all matches.
        var quoteAndList = List
            .of("man",
                "the");
        
        RunTimer
            .timeRun(() -> StepVerifier
                     .create(mQuoteClient
                             .searchQuotesEx(HANDEY,
                                             quoteAndList))
                     .expectNextCount(4)
                     .as("The count wasn't as expected")
                     .verifyComplete(),
                     "Handey searchQuotesEx()");
    }

    /**
     * Factor out common code for calling each microservice.
     */
    private Flux<Quote> runQuotes(String quoter) {
        Function<Long,
            Publisher<? extends Quote>> postQuotes = size
            -> mQuoteClient
            // Return the selected quotes.
            .postQuotes(quoter,
                        makeRandom(sQUOTES_REQUESTED,
                                   Math.toIntExact(size)));

        // List holding all Quote objects.
        return mQuoteClient
            // Get a Flux containing all the quotes.
            .getAllQuotes(quoter)

            // .doOnNext(System.out::println)
            // Determine the total number of quotes.
            .count()

            // Return selected random quotes.
            .flatMapMany(postQuotes);
    }
}
