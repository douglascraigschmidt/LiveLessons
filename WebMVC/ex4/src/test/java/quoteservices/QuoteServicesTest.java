package quoteservices;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import quoteservices.client.QuoteClient;
import quoteservices.common.Options;
import quoteservices.common.ZippyQuote;
import quoteservices.common.HandeyQuote;
import quoteservices.server.handey.HandeyApplication;
import quoteservices.server.handey.HandeyController;
import quoteservices.server.zippy.ZippyApplication;
import quoteservices.server.zippy.ZippyController;

import java.util.List;

import static quoteservices.utils.RandomUtils.makeRandomIndices;

/**
 * This program tests the {@link QuoteClient} and its ability to
 * communicate with the {@link ZippyController} and {@link
 * HandeyController} via Spring WebMVC features.
 *
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (a {@code @SpringBootApplication}, i.e.,
 * {@link ZippyApplication}) and use that to start a Spring
 * application context to serve as the target of the tests.
 *
 * The {@code @SpringBootConfiguration} annotation indicates that a
 * class provides a Spring Boot application {@code @Configuration}.
 */
@SpringBootConfiguration
@SpringBootTest(classes = {ZippyApplication.class,
                           HandeyApplication.class},
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class QuoteServicesTest {
    /**
     * This object connects {@link QuoteServicesTest} to the {@code
     * QuoteClient}.  The {@code @Autowired} annotation ensures this
     * field is initialized via Spring dependency injection, where an
     * object receives another object it depends on (e.g., by creating
     * a {@link QuoteClient}).
     */
    @Autowired
    private QuoteClient testClient;

    /**
     * Emulate the "command-line" arguments for the tests.
     */
    private final String[] mArgv = new String[]{
            "-d",
            "false", // Disable debugging messages.
            "-n",
            "500" // Generate and test 500 random large Integer objects.
    };

    /**
     * Run all the tests and print the results.
     */
    @Test
    public void runTests() {
        System.out.println("Entering runTests()");

        Options.instance().parseArgs(mArgv);

        // Run the tests.
        runTests();

        System.out.println("Leaving runTests()");
    }

    /**
     * Demonstrate ...
     */
    public void runTests() {
        Options.display("printing results");

        // @@ Monte, do you know if it's possible for a SpringBootTest
        // to autolaunch more than one microservice per test run?  The
        // test program works fine until I uncomment the test for
        // second microservice below, in which case things go awry!

        /*
        // Future to a List holding Future<QuoteResult> objects.
        List<ZippyQuote> zippyQuotes = testClient
            .getZippyQuotes(makeRandomIndices(4, 10));


        // Print the zippy quote results.
        zippyQuotes
            .forEach(zippyQuote -> System.out
                     .println("result = "
                              + zippyQuote));
         */

        // Future to a List holding Future<QuoteResult> objects.
        List<HandeyQuote> handeyQuotes = testClient
            .getHandeyQuotes(makeRandomIndices(4, 10));

        // Print the handey quote results.
        handeyQuotes
            .forEach(handeyQuote -> System.out
                     .println("result = "
                              + handeyQuote));
    }                              
}
    
