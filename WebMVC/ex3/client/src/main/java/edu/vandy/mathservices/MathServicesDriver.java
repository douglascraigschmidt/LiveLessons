package edu.vandy.mathservices;

import edu.vandy.mathservices.client.MathServicesClient;
import edu.vandy.mathservices.common.GCDResult;
import edu.vandy.mathservices.common.Options;
import edu.vandy.mathservices.common.PrimeResult;
import edu.vandy.mathservices.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;

/**
 * This program demonstrates Java's structured concurrency frameworks
 * by (1) checking the primality of a {@link List} of random {@link
 * Integer} objects and (2) computing the greatest common divisor
 * (GCD) of pairs of these {@link Integer} objects using Spring WebMVC
 * microservices.  Java version 19 (or greater) and Gradle version 7.6
 * (or greater) must be installed to run this program.
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
     * The main entry point into the Spring application.
     */
    public static void main(String[] args) {
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
            System.out.println("Entering MathServicesDriver run()");

            // Parse any command-line arguments.
            Options.instance().parseArgs(args);

            // Demonstrate Java structured concurrency on a List of
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
        // Supplier to a List holding PrimeResult objects.
        Supplier<List<PrimeResult>> primeCheckFuture = null;

        // Supplier to a List holding GCDResult objects.
        Supplier<List<GCDResult>> gcdComputeFuture = null;

        // Create a new scope to execute virtual Thread-based tasks,
        // which exits only after all tasks complete.
        try (var scope =
             new StructuredTaskScope.ShutdownOnFailure()) {
            primeCheckFuture = scope
                // fork() starts a virtual thread to check primalities
                // concurrently.
                .fork(() -> testClient
                      .checkPrimalities(randomIntegers));

            gcdComputeFuture = scope
                // fork() starts a virtual thread to compute GCDs
                // concurrently.
                .fork(() -> testClient
                      .computeGCDs(randomIntegers));

            // Barrier synchronizer waits for both tasks to complete
            // or throw an exception that occurred.
            scope.join().throwIfFailed();

            Options.display("printing results");

            // The Supplier.get() calls below don't block since
            // the scope.join() call above won't return until both
            // tasks complete.

            // Print the primality results.
            primeCheckFuture
                .get()
                .forEach(primeResult -> System.out
                         .println(STR."result = \{primeResult}"));

            // Print the GCD results.
            gcdComputeFuture
                .get()
                .forEach(gcdResult -> System.out
                         .println(STR."result = \{gcdResult}"));

            // Don't exit the try-with-resources scope until all
            // concurrently executing tasks complete.
        } catch (Exception exception) {
            System.out.println(STR."Exception: \{exception.getMessage()}");
            System.exit(1);
        }
        System.exit(0);
    }                              
}
    
