package berraquotes;

import berraquotes.client.BerraQuotesClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

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
    private BerraQuotesClient testClient;

    /**
     * Number of quotes to request.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final int sNUMBER_OF_QUOTES_REQUESTED = 5;

    /**
     * Run all the tests.
     */
    @Test
    public void runTests() {
        System.out.println("Entering the BerraTest");

        // List holding all Quote objects.
        var berraQuotes = testClient
            .getAllQuotes();

        var size = berraQuotes.size();

        berraQuotes = testClient
            .getQuotes(makeRandomIndices(sNUMBER_OF_QUOTES_REQUESTED,
                                         berraQuotes.size()));

        // Get the Berra quotes.
        System.out.println("Printing "
                           + berraQuotes.size()
                           + " Berra quote results out of "
                           + size
                           + " quotes:");

        // Print the Berra quote results.
        berraQuotes
            .forEach(berraQuote -> System.out
                     .println("id = "
                              + berraQuote.id()
                              + " quote = "
                              + berraQuote.quote()));

        berraQuotes = testClient
            .searchQuotes("Baseball");

        System.out.println("Printing "
                           + berraQuotes.size()
                           + " Berra quote containing the word \"Baseball\"");

        // Print the Berra quote results.
        berraQuotes
            .forEach(berraQuote -> System.out
                     .println("id = "
                              + berraQuote.id()
                              + " quote = "
                              + berraQuote.quote()));

        System.out.println("Leaving the BerraTest");
    }                              
}
    
