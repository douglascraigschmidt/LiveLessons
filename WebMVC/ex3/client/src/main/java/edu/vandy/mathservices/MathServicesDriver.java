package edu.vandy.mathservices;

import edu.vandy.mathservices.client.MathServicesClient;
import edu.vandy.mathservices.common.Components;
import edu.vandy.mathservices.common.GCDResult;
import edu.vandy.mathservices.common.Options;
import edu.vandy.mathservices.common.PrimeResult;
import edu.vandy.mathservices.utils.RandomUtils;

import jdk.incubator.concurrent.StructuredTaskScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.concurrent.Future;

/**
 * This program demonstrate Java's structured concurrency frameworks
 * by (1) checking the primality of a {@link List} of random {@link
 * Integer} objects and (2) computing the greatest common divisor
 * (GCD) of pairs of these {@link Integer} objects using Spring WebMVC
 * microservices.
 */
@SpringBootApplication
public class MathServicesDriver 
       implements CommandLineRunner {
    /**
     * This object connects {@link MathServicesDriver} to the {@code
     * MathServicesClient}.
     */
    @Autowired
    private MathServicesClient testClient;

    /**
     * The main entry point into the Spring applicaition.
     */
    public static void main(String[] args) {
        // Process any command-line arguments.
        Options.instance().parseArgs(args);

        // Run the Spring application.
        SpringApplication.run(MathServicesDriver.class, args);
    }

    /**
     * Spring Boot automatically calls this method after the
     * application context has been loaded to exercise the
     * {@code ZippyController} and {@code HandeyController}
     * microservices.
     */
    @Override
    public void run(String... args) {
            System.out.println("Entering MathServicesDriver main()");

            // Parse any command-line arguments.
            Options.instance().parseArgs(args);

            // Demonstrate Java 19 structured concurrency on a List of
            // random large Integer objects.
            runTests(RandomUtils
                     .generateRandomIntegers(Options.instance()
                                             .numberOfElements(),
                                             Integer.MAX_VALUE));

            System.out.println("Leaving MathServicesDriver main()");
    }

    /**
     * Demonstrate Java's structured concurrency frameworks by (1)
     * checking the primality of a {@link List} of random {@link
     * Integer} objects and (2) computing the greatest common divisor
     * (GCD) of pairs of these {@link Integer} objects using Spring
     * WebMVC microservices.
     *
     * @param randomIntegers A {@link List} of random large {@link
     *                       Integer} objects
     */
    public void runTests(List<Integer> randomIntegers) {
        // Future to a List holding PrimeResult objects.
        Future<List<PrimeResult>> primeCheckFutures = null;

        // Future to a List holding GCDResult objects.
        Future<List<GCDResult>> gcdComputeFutures = null;

        // Create a new scope to execute virtual Thread-based tasks,
        // which exits only after all tasks complete.
        try (var scope =
             new StructuredTaskScope.ShutdownOnFailure()) {
            primeCheckFutures = scope
                // fork() starts a virtual thread to check primalities
                // concurrently.
                .fork(() -> testClient
                      .checkPrimalities(randomIntegers));

            gcdComputeFutures = scope
                // fork() starts a virtual thread to compute GCDs
                // concurrently.
                .fork(() -> testClient
                      .computeGCDs(randomIntegers));

            // Barrier synchronizer waits for both tasks to complete.
            scope.join();

            // Throw an Exception on failure.
            scope.throwIfFailed();

            Options.display("printing results");

            // The Future.resultNow() calls below don't block since
            // the scope.join() call above won't return until both
            // tasks complete.

            // Print the primality results.
            primeCheckFutures
                .resultNow()
                .forEach(primeResult -> System.out
                         .println("result = "
                                  + primeResult));

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
        System.exit(0);
    }                              
}
    
