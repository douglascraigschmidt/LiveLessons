import common.GCDResult;
import common.Options;
import common.PrimeResult;
import utils.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

import static common.GCDService.computeGCDs;
import static common.PrimalityService.checkPrimalities;

/**
 * This example demonstrates Java 19 structured concurrency features,
 * which enable a main task to split into several concurrent sub-tasks
 * that run concurrently to completion before the main task can
 * complete.  Java 19 supports structured concurrency by enhancing
 * {@link ExecutorService} to support AutoCloseable and updating
 * {@link Executors} to define new static factory methods that support
 * usage in a structured manner.  You'll need to install JDK 19 with
 * gradle version 7.6 configured to run this example.
 */
public class ex2 {
    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv) {
        System.out.println("Entering test");

        // Initialize any command-line options.
        Options.instance().parseArgs(argv);

        // Demonstrate Java 19 structured concurrency on a
        // List of random large Integer objects.
        runTests(RandomUtils
                 .generateRandomIntegers(Options.instance()
                                         .numberOfElements(),
                                         Integer.MAX_VALUE));

        System.out.println("Leaving test");
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

        // Future to a List holding Future<PrimeResult> objects.
        Future<List<Future<PrimeResult>>> primeCheckFutures;

        // Future to a List holding Future<GCDResult> objects.
        Future<List<Future<GCDResult>>> gcdComputeFutures;

        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (ExecutorService executor =
             Executors.newVirtualThreadPerTaskExecutor()) {
            primeCheckFutures = executor
                // submit() starts a virtual thread to check
                // primalities concurrently.
                .submit(() -> checkPrimalities(randomIntegers));

            gcdComputeFutures = executor
                // submit() starts a virtual thread to compute GCDs
                // concurrently.
                .submit(() -> computeGCDs(randomIntegers));

            // Don't exit the try-with-resources scope until all
            // concurrently executing tasks complete.
        } 

        Options.display("printing results");

        // The Future.resultNow() calls below don't block since the
        // try-with-resources scope above won't exit until all tasks
        // complete.

        // Print the primality results.  
        primeCheckFutures
            .resultNow()
            .forEach(future -> System.out
                     .println("result = "
                              + future.resultNow()));

        // Print the GCD results.
        gcdComputeFutures
            .resultNow()
            .forEach(future -> System.out
                     .println("result = "
                              + future.resultNow()));
    }                              
}
