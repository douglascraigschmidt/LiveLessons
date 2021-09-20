import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import utils.Options;
import utils.RunTimer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * Synchronous client to perform HTTP requests.
     */
    RestTemplate mRestTemplate = new RestTemplate();

    /**
     * A List of randomly-generated integers.
     */
    private static List<Integer> sRANDOM_INTEGERS;

    /**
     * Location of the server.
     */
    String baseUrl = "http://localhost:8081";

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
        generateRandomNumbers();

        // Test individual HTTP GET requests to the server to check if
        // an Integer is prime or not.
        timeTest(this::testIndividualCalls,
                 true,
                 "individualCallsParallel");

        // Test passing a List in one HTTP GET request to the server
        // to see if all the List elements are prime or not.
        timeTest(this::testListCall,
                 true,
                 "listCallParallel");
    }

    /**
     * Generate a list of random {@link Integer} objects used for
     * prime number checking.
     */
    private static void generateRandomNumbers() {
        // Generate a list of random integers.
        sRANDOM_INTEGERS = new Random()
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
     * Test individual HTTP GET requests to the server to check if an
     * {@link Integer} is prime or not.
     */
    private Void testIndividualCalls(Boolean parallel) {
        var stream = sRANDOM_INTEGERS
            // Convert the List to a stream.
            .stream();

        // Conditionally convert to a parallel stream.
        if (parallel)
            stream.parallel();

        var results = stream
            // Create and send a GET request to the server to check if
            // the primeCandidate is prime or not.
            .map(primeCandidate -> 
                 makeGetRequest(makeCheckIfPrimeUrl(primeCandidate)))

            // Trigger the intermediate operations and collect the
            // results into a List.
            .collect(toList());

        // Print the results.
        printResults(sRANDOM_INTEGERS, results);
        return null;
    }

    /**
     * Test passing a {@link List} in one HTTP GET request to the
     * server to see if all the {@link List} elements are prime or
     * not.
     */
    private Void testListCall(Boolean parallel) {
        ResponseEntity<Integer[]> responseEntity = mRestTemplate
            // Execute the HTTP method to the given URI template,
            // writing the given request entity to the request,
            // and returns the response as ResponseEntity.
            .exchange(makeCheckIfPrimeListUrl(list2String(sRANDOM_INTEGERS), 
                                              parallel),
                      HttpMethod.GET, null,
                      Integer[].class);

        // Convert the array into a List.
        List<Integer> results = Arrays
            .asList(Objects.requireNonNull(responseEntity.getBody()));

        // Print the results.
        printResults(sRANDOM_INTEGERS, results);
        return null;
    }

    /**
     * Convert the contents of the {@link List} param into a
     * comma-separated {@link String}.

     * @param list The {@link List} to convert to a comma-separate
     *        string
     * @return A comma-separated string containing the contents of the
     *         {@link List}
     */
    private static <T> String list2String(List<T> list) {
        return list
            // Convert the List elements into a Stream.
            .stream()

            // Convert each Stream element into a String.
            .map(Object::toString)

            // Trigger intermediate operations and convert each String
            // in the Stream into a single comma-separated String.
            .collect(Collectors.joining(","));
    }

    /**
     * Iterate through the original List of prime candidates and print
     * both each prime candidate and the corresponding prime result.
     *
     * @param original The original {@link List} of prime candidates
     * @param results A {@link List} containing the results of the
     *                primality checks
     */
    private void printResults(List<Integer> original,
                              List<Integer> results) {
        // Iterate through the original List of prime candidates and
        // print both each prime candidate and the corresponding
        // prime result.
        for (int i = 0; i < original.size(); i++)
            System.out.println("result for "
                               + original.get(i)
                               + " = "
                               + results.get(i));
    }

    /**
     * This factory method creates a URL that can be passed to an HTTP
     * GET request to determine if an {@code integer} is prime.

     * @param integer An integer to check for primality
     * @return A URL that can be passed to an HTTP GET request to
     *         determine if the {@code integer} is prime
     */
    private String makeCheckIfPrimeUrl(Integer integer) {
        return baseUrl
            + "/checkIfPrime"
            + "?primeCandidate="
            + integer;
    }

    /**
     * This factory method creates a URL that can be passed to an HTTP
     * GET request to determine if a {@code listOfIntegers} is prime.

     * @param listOfIntegers A comma-separated list of integers
     * @return A URL that can be passed to an HTTP GET request to
     *         determine if a {@code listOfIntegers} is prime
     */
    private String makeCheckIfPrimeListUrl(String listOfIntegers, 
                                           boolean parallel) {
        return baseUrl
            + "/checkIfPrimeList"
            + "?primeCandidates="
            + listOfIntegers
            + "&parallel="
            + parallel;
    }

    /**
     * Make an HTTP GET call to the server passing in the {@code url}.
     *
     * @param url The URL to pass to the server via a GET request
     * @return The result from the server
     */
    protected Integer makeGetRequest(String url) {
        return mRestTemplate
            // Retrieve a representation by doing a GET on the URL.
            .getForEntity(url, Integer.class)

            // Returns the body of this entity.
            .getBody();
    }

    /**
     * Time {@code testName} using the given {@code hashMap}.
     *
     * @param test The prime checker used evaluate prime candidates
     * @param testName The name of the test
     */
    private void timeTest(Function<Boolean, Void> test,
                          Boolean parallel,
                          String testName) {
        RunTimer
            // Time how long this test takes to run.
            .timeRun(() ->
                     // Run the test using the given test.
                     runTest(test, parallel, testName),
                     testName);
    }

    /**
     * Run the prime number test.
     * 
     * @param test A function that maps candidate primes to their
     *        smallest factor (if they aren't prime) or 0 if they are prime
     * @param testName Name of the test
     * @return The prime checker (which may be updated during the test)
     */
    private Void runTest
        (Function<Boolean, Void> test,
         Boolean parallel,
         String testName) {
        Options.print("Starting "
                      + testName
                      + " with count = "
                      + Options.instance().count());
        test.apply(parallel);
        return null;
    }
}
    
