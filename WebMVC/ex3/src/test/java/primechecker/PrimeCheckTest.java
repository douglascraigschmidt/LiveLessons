package primechecker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import primechecker.client.PrimeCheckClient;
import primechecker.server.PrimeCheckApplication;
import primechecker.server.PrimeCheckController;
import primechecker.common.Options;
import primechecker.utils.RunTimer;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toList;

/**
 * This program tests the {@link PrimeCheckClient} and its ability to
 * communicate with the {@link PrimeCheckController} via Spring WebMVC
 * features.
 *
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (a {@code @SpringBootApplication}, i.e.,
 * {@link PrimeCheckApplication}) and use that to start a Spring
 * application context to serve as the target of the tests.
 *
 * The {@code @SpringBootConfiguration} annotation indicates that a
 * class provides a Spring Boot application {@code @Configuration}.
 */
@SpringBootConfiguration
@SpringBootTest(classes = PrimeCheckApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PrimeCheckTest {
    /**
     * This object connects {@link PrimeCheckTest} to the {@code
     * PrimeCheckClient}.  The {@code @Autowired} annotation ensures
     * this field is initialized via Spring dependency injection,
     * where an object receives another object it depends on (e.g., by
     * creating a {@link PrimeCheckClient}).
     */
    @Autowired
    private PrimeCheckClient testClient;

    /**
     * Emulate the "command-line" arguments for the tests.
     */
    private final String[] mArgv = new String[]{
            "-d",
            "false", // Disable debugging messages.
            "-c",
            "500" // Generate and test 500 random large Integer objects.
    };

    /**
     * Run all the tests and print the results.
     */
    @Test
    public void runTests() {
        System.out.println("Entering runTests()");

        Options.instance().parseArgs(mArgv);

        // Generate a list of random numbers.
        List<Integer> randomIntegers = generateRandomNumbers();

        assert (testClient != null);

        // Test sending individual HTTP GET requests to the server
        // sequentially to check if an Integer is prime or not
        timeTest(testClient::testIndividualCalls,
                randomIntegers,
                false,
                "individualCallsSequential");

        // Test sending individual HTTP GET requests to the server in
        // parallel to check if an Integer is prime or not.
        timeTest(testClient::testIndividualCalls,
                randomIntegers,
                true,
                "individualCallsParallel");

        // Test sending a List in one HTTP GET request to the server,
        // which sequentially checks List elements for primality.
        timeTest(testClient::testListCall,
                randomIntegers,
                false,
                "listCallSequential");

        // Test sending a List in one HTTP GET request to the server,
        // which check List elements for primality in parallel.
        timeTest(testClient::testListCall,
                randomIntegers,
                true,
                "listCallParallel");

        // Print the results in ascending order.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving runTests()");
    }

    /**
     * Generate a {@link List} of random {@link Integer} objects used
     * for prime number checking.
     *
     * @return A {@link List} of random {@link Integer} objects
     */
    private List<Integer> generateRandomNumbers() {
        // Generate and return a List of random Integer objects.
        return new Random()
                // Generate the given # of large random ints.
                .ints(Options.instance().count(),
                        Integer.MAX_VALUE - Options.instance().count(),
                        Integer.MAX_VALUE)

                // Convert each primitive int to Integer.
                .boxed()

                // Trigger intermediate operations and collect into a
                // List.
                .collect(toList());
    }

    /**
     * Time {@code testName} using the given {@code test}.
     *
     * @param test            A {@link BiFunction} that performs the test
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel        True if using parallel streams, else false
     * @param testName        The name of the test
     */
    private void timeTest
    (BiFunction<List<Integer>, Boolean, List<Integer>> test,
     List<Integer> primeCandidates,
     Boolean parallel,
     String testName) {
        Options.print("Starting "
                + testName
                + " with count = "
                + Options.instance().count());

        var results = RunTimer
                // Time how long this test takes to run.
                .timeRun(() ->
                         // Run test using the given Function and params.
                         test.apply(primeCandidates, parallel),
                         testName);

        // Display the results.
        Options.displayResults(primeCandidates, results);
    }
}
    
