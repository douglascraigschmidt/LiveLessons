package primechecker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import primechecker.client.PCClientCompletableFuture;
import primechecker.client.PCClientCompletableFutureEx;
import primechecker.client.PCClientParallelStream;
import primechecker.client.PCClientStructuredConcurrency;
import primechecker.common.Options;
import primechecker.server.PCServerApplication;
import primechecker.server.PCServerController;
import primechecker.utils.RandomUtils;
import primechecker.utils.RunTimer;

import java.util.List;
import java.util.function.BiFunction;

/**
 * This program tests the {@link PCClientParallelStream} and its ability to
 * communicate with the {@link PCServerController} via Spring WebMVC
 * features.
 *
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (a {@code @SpringBootApplication}, i.e.,
 * {@link PCServerApplication}) and use that to start a Spring
 * application context to serve as the target of the tests.
 *
 * The {@code @SpringBootConfiguration} annotation indicates that a
 * class provides a Spring Boot application {@code @Configuration}.
 */
@SpringBootConfiguration
@SpringBootTest(classes = PCServerApplication.class,
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PrimeCheckTest {
    /**
     * This object connects {@link PrimeCheckTest} to the {@code
     * PrimeCheckClient}.  The {@code @Autowired} annotation ensures
     * this field is initialized via Spring dependency injection,
     * where an object receives another object it depends on (e.g., by
     * creating a {@link PCClientParallelStream}).
     */
    @Autowired
    private PCClientParallelStream testClientPS;

    @Autowired
    private PCClientStructuredConcurrency testClientSC;

    @Autowired
    private PCClientCompletableFuture testClientCF;

    @Autowired
    private PCClientCompletableFutureEx testClientCFEx;

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

        // Generate a List of random Integer objects.
        List<Integer> randomIntegers = RandomUtils
            .generateRandomNumbers(Options.instance().getCount(),
                                   Options.instance().maxValue());

        assert (testClientPS != null);

        /*
        // Test sending individual HTTP GET requests to the server
        // sequentially to check if an Integer is prime or not
        timeTest(testClientPS::testIndividualCalls,
                randomIntegers,
                false,
                "individualCallsSequentialPS");

        // Test sending individual HTTP GET requests to the server in
        // parallel to check if an Integer is prime or not.
        timeTest(testClientPS::testIndividualCalls,
                randomIntegers,
                true,
                "individualCallsParallelPS");

        // Test sending a List in one HTTP GET request to the server,
        // which sequentially checks List elements for primality.
        timeTest(testClientPS::testListCall,
                randomIntegers,
                false,
                "listCallSequentialPS");

        // Test sending a List in one HTTP GET request to the server,
        // which check List elements for primality in parallel.
        timeTest(testClientPS::testListCall,
                randomIntegers,
                true,
                "listCallParallelPS");

         */

        // Test sending a List in one HTTP GET request to the server,
        // which sequentially checks List elements for primality.
        timeTest(testClientCF::testListCall,
                randomIntegers,
                false,
                "listCallSequentialCF");
        // Test sending individual HTTP GET requests to the server
        // sequentially to check if an Integer is prime or not
        timeTest(testClientCF::testIndividualCalls,
                randomIntegers,
                false,
                "individualCallsSequentialCF");

        // Test sending individual HTTP GET requests to the server in
        // parallel to check if an Integer is prime or not.
        timeTest(testClientCF::testIndividualCalls,
                randomIntegers,
                true,
                "individualCallsParallelCF");

        // Test sending a List in one HTTP GET request to the server,
        // which check List elements for primality in parallel.
        timeTest(testClientCF::testListCall,
                randomIntegers,
                true,
                "listCallParallelCF");

        // Test sending a List in one HTTP GET request to the server,
        // which sequentially checks List elements for primality.
        timeTest(testClientCFEx::testListCall,
                randomIntegers,
                false,
                "listCallSequentialCFEx");

        // Test sending a List in one HTTP GET request to the server,
        // which check List elements for primality in parallel.
        timeTest(testClientCFEx::testListCall,
                randomIntegers,
                true,
                "listCallParallelCFEx");

        // Test sending individual HTTP GET requests to the server
        // sequentially to check if an Integer is prime or not
        timeTest(testClientCFEx::testIndividualCalls,
                randomIntegers,
                false,
                "individualCallsSequentialCFEx");

        // Test sending individual HTTP GET requests to the server in
        // parallel to check if an Integer is prime or not.
        timeTest(testClientCFEx::testIndividualCalls,
                randomIntegers,
                true,
                "individualCallsParallelCFEx");

        /*
        // Test sending individual HTTP GET requests to the server
        // sequentially to check if an Integer is prime or not
        timeTest(testClientSC::testIndividualCalls,
                randomIntegers,
                false,
                "individualCallsSequentialSC");

        // Test sending individual HTTP GET requests to the server in
        // parallel to check if an Integer is prime or not.
        timeTest(testClientSC::testIndividualCalls,
                randomIntegers,
                true,
                "individualCallsParallelSC");

        // Test sending a List in one HTTP GET request to the server,
        // which sequentially checks List elements for primality.
        timeTest(testClientSC::testListCall,
                randomIntegers,
                false,
                "listCallSequentialSC");

        // Test sending a List in one HTTP GET request to the server,
        // which check List elements for primality in parallel.
        timeTest(testClientSC::testListCall,
                randomIntegers,
                true,
                "listCallParallelSC");

         */

        // Print the results in ascending order.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving runTests()");
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
                + Options.instance().getCount());

        // Garbage collect to leave memory in a pristine state.
        System.gc();

        var results = RunTimer
                // Time how long this test takes to run.
                .timeRun(() ->
                         // Run test using the given Function and params.
                         test.apply(primeCandidates, parallel),
                         testName);

        // System.out.println("results.size() = " + results.size());
        // Display the results.
        Options.displayResults(primeCandidates, results);
    }
}
    
