package edu.vandy.berraquotes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This program tests the {@link BerraClient} and its ability to
 * communicate with the {@link BerraController} via Spring WebMVC
 * features.
 *
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (a {@code @SpringBootApplication}, i.e.,
 * {@link MoviesApplication}) and use that to start a Spring
 * application context to serve as the target of the tests.
 *
 * The {@code @SpringBootConfiguration} annotation indicates that a
 * class provides a Spring Boot application {@code @Configuration}.
 */
@SpringBootConfiguration
@SpringBootTest(classes = BerraApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BerraTest {
    /**
     * This auto-wired field connects the {@link BerraTest} to the
     * {@link BerraClient}.
     */
    @Autowired
    private BerraClient testClient;

    /**
     * Run all the tests.
     */
    @Test
    public void runTests() {
        System.out.println("Entering the BerraTest");

        // List holding all Quote objects.
        var berraQuotes = testClient
            .getAllQuotes(BERRA);

        var size = berraQuotes.size();

        berraQuotes = quoteClient
            .getQuotes(BERRA,
                       makeRandomIndices(sNUMBER_OF_QUOTES_REQUESTED,
                                         berraQuotes.size()));

        // Get the Berra quotes.
        Options.display("Printing "
                        + berraQuotes.size()
                        + " Berra quote results out of "
                        + size
                        + " quotes:");

        // Print the Berra quote results.
        berraQuotes
            .forEach(berraQuote -> System.out
                     .println("id = "
                              + berraQuote.id
                              + " quote = "
                              + berraQuote.quote));

        System.out.println("Leaving the BerraTest");
    }                              
}
    
