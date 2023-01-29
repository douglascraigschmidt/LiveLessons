package berraquotes;

import berraquotes.client.BerraQuotesClient;
import berraquotes.common.Quote;
import berraquotes.server.BerraQuotesApplication;
import berraquotes.server.BerraQuotesController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import berraquotes.utils.RunTimer;

import static berraquotes.common.Constants.Strategies.*;
import static berraquotes.utils.RandomUtils.makeRandomIndices;

/**
 * This program tests the {@link BerraQuotesClient} and its ability to
 * communicate with the {@link BerraQuotesController} via Spring WebMVC
 * features.
 *
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (a {@code @SpringBootApplication}, i.e.,
 * {@link BerraQuotesApplication}) and use that to start a Spring
 * application context to serve as the target of the tests.
 *
 * The {@code @SpringBootConfiguration} annotation indicates that a
 * class provides a Spring Boot application {@code @Configuration}.
 */
@SpringBootConfiguration
@SpringBootTest(classes = BerraQuotesApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BerraQuotesTest {
    /**
     * This auto-wired field connects the {@link BerraQuotesTest} to the
     * {@link BerraQuotesClient}.
     */
    @Autowired
    private BerraQuotesClient quoteClient;

    /**
     * Number of quotes to request.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int sQUOTE_COUNT = 5;

    /**
     * Run all the tests.
     */
    @Test
    public void runTests() {
        System.out.println("Entering the BerraTest");

        timeBerraQuotes(PARALLEL_STREAMS_REGEX,
                "Parallel Streams Regex");
        timeBerraQuotes(STRUCTURED_CONCURRENCY,
                        "Structured Concurrency");
        timeBerraQuotes(PARALLEL_STREAMS,
                        "Parallel Streams");
        timeBerraQuotes(SEQUENTIAL_STREAMS,
                        "Sequential Streams");

        System.out.println(RunTimer.getTimingResults());
        System.out.println("Leaving the BerraTest");
    }                              

    /**
     * Record how long it takes to get the Berra quotes.
     *
     * @param strategy The quote checking strategy to use
     */
    private void timeBerraQuotes(int strategy,
                                 String strategyName) {
        // Get the Berra quotes and record how much time was spent on
        // this.
        var berraQuotes = RunTimer
            .timeRun(() -> runQuotes(strategy),
                     strategyName + " Berra quotes");


        System.out.println(strategyName
                           + (berraQuotes.size() == 5
                            ? " successfully"
                            : " unsuccessfully")
                           + " received expected 5 Berra quote results");

        berraQuotes = RunTimer
            .timeRun(() -> runSearches(strategy),
                     strategyName + " Berra searches");

        System.out.println(strategyName
                + (berraQuotes.size() == 30
                ? " successfully"
                : " unsuccessfully")
                + " received expected "
                + berraQuotes.size()
                + " Berra search results");

        /*
        printResults(berraQuotes,
                     strategyName
                     + " Berra search results");

         */
    }

    /**
     * Factors out common code for get quotes from the microservice
     * implementation identified by the {@code strategy}.
     */
    private List<Quote> runQuotes(int strategy) {
        // List holding all Quote objects.
        var quotes = quoteClient
            .getAllQuotes(strategy);

        // Generate random indicates.
        var quoteIds = makeRandomIndices
            (sQUOTE_COUNT,
             quotes.size());

        // Return the selected quotes.
        return quoteClient
            .getQuotes(strategy,
                       quoteIds);
    }

    /**
     * Factors out common code for running searches from the
     * microservice implementation identified by the {@code strategy}.
     */
    private List<Quote> runSearches(int strategy) {
        return Stream
            .of(quoteClient
                .searchQuotes(strategy,
                              "hit"),
                quoteClient
                .searchQuotes(strategy,
                              List.of("baseball",
                                      "game",
                                      "Little League",
                                      "good",
                                      "you",
                                      "wrong",
                                      "gonna",
                                      "people")))
            .flatMap(List::stream)
            .toList();
    }

    private void printResults(List<Quote> quotes,
                              String testName) {
        System.out.println("Printing "
                           + quotes.size()
                           + " "
                           + testName);
                           
        quotes
            .forEach(quote -> System.out
                     .println("id = "
                              + quote.id()
                              + " quote "
                              + quote.quote()));
    }
}
    
