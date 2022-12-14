package mathservices;

import jdk.incubator.concurrent.StructuredTaskScope;
import mathservices.client.MathServicesClient;
import mathservices.common.GCDResult;
import mathservices.common.Options;
import mathservices.common.PrimeResult;
import mathservices.server.gcd.GCDController;
import mathservices.utils.RandomUtils;

import java.util.List;
import java.util.concurrent.Future;

/**
 * This program tests the {@link MathServicesClient} and its ability
 * to communicate with the {@link GCDController} and {@link
 * PrimalityController} via Spring WebMVC features.
 */
public class MathServicesDriver {
    /**
     * This object connects {@link MathServicesDriver} to the {@code
     * MathServicesClient}.
     */
    private static final MathServicesClient testClient =
        new MathServicesClient();

    /**
     * Run all the tests and print the results.
     */
    public static void main(String[] argv) {
        System.out.println("Entering main()");

        Options.instance().parseArgs(argv);

        // Demonstrate Java 19 structured concurrency on a List of
        // random large Integer objects.
        runTests(RandomUtils
                 .generateRandomIntegers(Options.instance()
                                         .numberOfElements(),
                                         Integer.MAX_VALUE));

        System.out.println("Leaving main()");
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
    public static void runTests(List<Integer> randomIntegers) {
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
    }                              
}
    
