package mathservices;

import jdk.incubator.concurrent.StructuredTaskScope;
import mathservices.common.GCDResult;
import mathservices.common.PrimeResult;
import mathservices.server.primality.PrimalityApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import mathservices.client.MathServicesClient;
import mathservices.common.Options;
import mathservices.server.gcd.GCDApplication;
import mathservices.server.gcd.GCDController;
import mathservices.utils.RandomUtils;

import java.util.List;
import java.util.concurrent.Future;

/**
 * This program tests the {@link MathServicesClient} and its ability to
 * communicate with the {@link GCDController} via Spring WebMVC
 * features.
 *
 * The {@code @SpringBootTest} annotation tells Spring to look for a
 * main configuration class (a {@code @SpringBootApplication}, i.e.,
 * {@link GCDApplication}) and use that to start a Spring
 * application context to serve as the target of the tests.
 *
 * The {@code @SpringBootConfiguration} annotation indicates that a
 * class provides a Spring Boot application {@code @Configuration}.
 */
@SpringBootConfiguration
@SpringBootTest(classes = {GCDApplication.class, PrimalityApplication.class},
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MathServicesTest {
    /**
     * This object connects {@link MathServicesTest} to the {@code
     * MathServicesClient}.  The {@code @Autowired} annotation ensures
     * this field is initialized via Spring dependency injection,
     * where an object receives another object it depends on (e.g., by
     * creating a {@link MathServicesClient}).
     */
    @Autowired
    private MathServicesClient testClient;

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

        // Demonstrate Java 19 structured concurrency on a
        // List of random large Integer objects.
        runTests(RandomUtils
                 .generateRandomIntegers(Options.instance()
                                         .numberOfElements(),
                                         Integer.MAX_VALUE));

        System.out.println("Leaving runTests()");
    }

    /**
     * Demonstrate modern Java structured concurrency by concurrently
     * (1) checking the primality of a {@link List} of random numbers
     * and (2) computing the greatest common divisor (GCD) of pairs of
     * these random numbers.
     *
     * @param randomIntegers A {@link List} of random large {@link
     *                       Integer} objects
     */
    public void runTests(List<Integer> randomIntegers) {

        // Future to a List holding Future<PrimeResult> objects.
        Future<List<PrimeResult>> primeCheckFutures = null;

        // Future to a List holding Future<GCDResult> objects.
        Future<List<GCDResult>> gcdComputeFutures = null;

        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (var scope =
             new StructuredTaskScope.ShutdownOnFailure()) {
            /*
            primeCheckFutures = scope
                // submit() starts a virtual thread to check
                // primalities concurrently.
                .fork(() -> testClient
                        .checkPrimalities(randomIntegers));

             */

            gcdComputeFutures = scope
                // submit() starts a virtual thread to compute GCDs
                // concurrently.
                .fork(() -> testClient
                        .computeGCDs(randomIntegers));



            // Barrier synchronizer waits for both tasks to
            // complete.
            scope.join();

            // Throw an Exception on failure.
            scope.throwIfFailed();

            Options.display("printing results");

            // The Future.resultNow() calls below don't block since the
            // scope.join() call above won't return until both tasks
            // complete.

            /*
            // Print the primality results.
            primeCheckFutures
                    .resultNow()
                    .forEach(primeResult -> System.out
                            .println("result = "
                                    + primeResult));

             */

            // Print the GCD results.
            gcdComputeFutures
                    .resultNow()
                    .forEach(gcdResult -> System.out
                            .println("result = "
                                    + gcdResult));

            // Don't exit the try-with-resources scope until all
            // concurrently executing tasks complete.
        } catch (Exception exception) {
            System.out.println("Exception: "
                               + exception.getMessage());
        }
    }                              
}
    
