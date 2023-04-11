package primechecker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import primechecker.client.PCClientConcurrentFlux;
import primechecker.client.PCClientParallelFlux;
import primechecker.common.Options;
import primechecker.server.PCServerApplication;
import primechecker.server.PCServerController;
import primechecker.utils.RandomUtils;
import primechecker.utils.RunTimer;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;

/**
 * This program tests the {@link PCClientConcurrentFlux} and {@link
 * PCClientParallelFlux} and its ability to communicate with the
 * {@link PCServerController} via Spring WebFlux features.
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
     * These fields connect {@link PrimeCheckTest} to the {@code
     * PCClientFlux} and {@link PCClientParallelFlux}.  The
     * {@code @Autowired} annotation ensures this field is initialized
     * via Spring dependency injection, where an object receives
     * another object it depends on.
     */
    @Autowired
    private PCClientParallelFlux testClientPF;

    @Autowired
    private PCClientConcurrentFlux testClientCF;

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

        // Parse the arguments.
        Options.instance().parseArgs(mArgv);

        // Generate a List of random Integer objects.
        List<Integer> randomIntegers = RandomUtils
            .generateRandomNumbers(Options.instance().getCount(),
                                   Options.instance().maxValue());

        assert (testClientPF != null);

        // Test sending individual HTTP GET requests to the server
        // sequentially to check if an Integer is prime or not
        timeTest(testClientPF::testIndividualCalls,
                 Flux.fromIterable(randomIntegers),
                 "individualCallsConcurrentFlux");

        // Test sending individual HTTP GET requests to the server in
        // parallel to check if an Integer is prime or not.
        timeTest(testClientCF::testIndividualCalls,
                 Flux.fromIterable(randomIntegers),
                 "individualCallsParallelFlux");

        // Test sending a List in one HTTP POST request to the server,
        // which checks List elements for primality using the flatMap()
        // concurrency idiom.
        timeTest(testClientCF::testFluxCall,
                 Flux.fromIterable(randomIntegers),
                 "listCallConcurrentFlux");

        // Test sending a List in one HTTP POST request to the server,
        // which check List elements for primality using ParallelFlux.
        timeTest(testClientPF::testFluxCall,
                 Flux.fromIterable(randomIntegers),
                 "listCallParallelFlux");

        // Print the results in ascending order.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving runTests()");
    }

    /**
     * Time {@code testName} using the given {@code test}.
     *
     * @param test A {@link Function} that performs the test
     * @param primeCandidates A {@link Flux} that emits {@link
     *                        Integer} objects to check for primality
     * @param testName The name of the test
     */
    private void timeTest
        (Function<Flux<Integer>, Flux<Integer>> test,
         Flux<Integer> primeCandidates,
         String testName) {
        Options.print("Starting "
                      + testName
                      + " with count = "
                      + Options.instance().getCount());

        // Garbage collect to leave memory in a pristine state.
        System.gc();

        var results = RunTimer
            // Time how long this test takes to run.
            .timeRun(() -> test
                     // Run test using the given Function and params.
                     .apply(primeCandidates)

                     // Collect results into a List.
                     .collectList()

                     // Block until the results are received.
                     .block(),

                     // The name of the test.
                     testName);

        // Display the results.
        Options.displayResults(primeCandidates,
                               Flux.fromIterable(results));
    }
}
    
