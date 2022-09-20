package server;

import client.PrimeCheckClient;
import common.Components;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import utils.Options;
import utils.RunTimer;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toList;

/**
 * This program tests the {@link PrimeCheckClient} and its ability to
 * communicate with the {@link PrimeCheckController} via Spring WebMVC
 * features.
 */
@SpringBootTest
@ContextConfiguration(classes = {
    Components.class,
    PrimeCheckClient.class,
    PrimeCheckController.class
})
public class PrimeCheckTest {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * This object connects to the {@code testClient}.  The {@code @Autowired}
     * annotation ensures this field is initialized via Spring
     * dependency injection, where an object receives another object
     * it depends on (e.g., by creating a {@link PrimeCheckClient}).
     */
    @Autowired
    private PrimeCheckClient testClient;

    /**
     * Emulate the "command-line" arguments.
     */
    private final String[] mArgv = new String[]{
        "-d",
        "false",
        "-c",
        "500"
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

        // Test individual HTTP GET requests to the server to check if
        // an Integer is prime or not sequentially.
        timeTest(testClient::testIndividualCalls,
                 randomIntegers,
                 false,
                 "individualCallsSequential");

        // Test passing a List in one HTTP GET request to the server
        // to see if all the List elements are prime or not sequentially.
        timeTest(testClient::testListCall,
                 randomIntegers,
                 false,
                 "listCallSequential");

        // Test individual HTTP GET requests to the server to check if
        // an Integer is prime or not in parallel.
        timeTest(testClient::testIndividualCalls,
                 randomIntegers,
                 true,
                 "individualCallsParallel");

        // Test passing a List in one HTTP GET request to the server
        // to see if all the List elements are prime or not in parallel.
        timeTest(testClient::testListCall,
                 randomIntegers,
                 true,
                 "listCallParallel");

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
     * @param test A {@link BiFunction} that performs the test
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallel streams, else false
     * @param testName The name of the test
     */
    private void timeTest(BiFunction<List<Integer>, Boolean, List<Integer>> test,
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
                     runTest(test, primeCandidates, parallel, testName),
                     testName);

        // Display the results.
        Options.displayResults(primeCandidates, results);
    }

    /**
     * Run the test and return the results.
     * 
     * @param test A {@link BiFunction} that performs the test
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallel streams, else false
     * @param testName Name of the test
     * @return The {@link List} of results of applying the primality test
     */
    private List<Integer> runTest
        (BiFunction<List<Integer>, Boolean, List<Integer>> test,
         List<Integer> primeCandidates,
         Boolean parallel,
         String testName) {
        // Run the test and return the results.
        return test.apply(primeCandidates, parallel);
    }
}
    
