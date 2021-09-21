import client.TestClient;
import utils.Options;
import utils.RunTimer;

import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toList;

/**
 * This program applies WebMVC ...
 */
public class ex1 {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * A List of randomly-generated integers.
     */
    private static List<Integer> sRANDOM_INTEGERS;

    /**
     * Main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Create an instance to test.
        ex1 test = new ex1(argv);

        // Run the tests.
        test.run();
    }

    /**
     * Constructor initializes the fields.
     */
    ex1(String[] argv) {
        // Parse the command-line arguments.
        Options.instance().parseArgs(argv);
    }

    /**
     * Run all the tests and print the results.
     */
    private void run() {
        // Generate a list of random numbers.
        sRANDOM_INTEGERS = generateRandomNumbers();

        // Test individual HTTP GET requests to the server to check if
        // an Integer is prime or not sequentially.
        timeTest(TestClient::testIndividualCalls,
                 sRANDOM_INTEGERS,
                 false,
                 "individualCallsSequential");

        // Test passing a List in one HTTP GET request to the server
        // to see if all the List elements are prime or not sequentially.
        timeTest(TestClient::testListCall,
                 sRANDOM_INTEGERS,
                 false,
                 "listCallSequential");

        // Test individual HTTP GET requests to the server to check if
        // an Integer is prime or not in parallel.
        timeTest(TestClient::testIndividualCalls,
                 sRANDOM_INTEGERS,
                 true,
                 "individualCallsParallel");

        // Test passing a List in one HTTP GET request to the server
        // to see if all the List elements are prime or not in parallel.
        timeTest(TestClient::testListCall,
                 sRANDOM_INTEGERS,
                 true,
                 "listCallParallel");

        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Generate a {@link List} of random {@link Integer} objects used
     * for prime number checking.
     *
     * @return A {@link List} of random {@link Integer} objects
     */
    private static List<Integer> generateRandomNumbers() {
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
    private void timeTest(BiFunction<List<Integer>, Boolean, Void> test,
                          List<Integer> primeCandidates,
                          Boolean parallel,
                          String testName) {
        RunTimer
            // Time how long this test takes to run.
            .timeRun(() ->
                     // Run test using the given Function and params.
                     runTest(test, primeCandidates, parallel, testName),
                     testName);
    }

    /**
     * Run the test.
     * 
     * @param test A {@link BiFunction} that performs the test
     * @param primeCandidates A {@link List} of {@link Integer}
     *                        objects to check for primality
     * @param parallel True if using parallel streams, else false
     * @param testName Name of the test
     */
    private Void runTest
        (BiFunction<List<Integer>, Boolean, Void> test,
         List<Integer> primeCandidates,
         Boolean parallel,
         String testName) {
        Options.print("Starting "
                      + testName
                      + " with count = "
                      + Options.instance().count());

        // Run the test.
        test.apply(primeCandidates, parallel);
        return null;
    }
}
    
