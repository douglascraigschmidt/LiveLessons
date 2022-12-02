import utils.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

import static utils.ExceptionUtils.rethrowSupplier;
import static utils.RandomUtils.generateRandomNumbers;

/**
 * This example demonstrates Java 19 structured concurrency features,
 * which enables a main task to split into several concurrent
 * sub-tasks that run concurrently to completion before the main task
 * can complete.  Java 19 supports structured concurrency by enhancing
 * {@link ExecutorService} to support AutoCloseable and updating
 * {@link Executors} to define new static factory methods that support
 * usage in a structured manner.  You'll need to install JDK 19 with
 * gradle version 7.6 configured to run this example.
 */
public class ex2 {
    /**
     * A {@link List} of randomly-generated {@link Integer} objects.
     */
    private static List<Integer> sRANDOM_INTEGERS;

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv)
        throws ExecutionException, InterruptedException {
        System.out.println("Entering test");

        // Initialize any command-line options.
        Options.instance().parseArgs(argv);

        // Generate a List of random numbers.
        sRANDOM_INTEGERS = RandomUtils
            .generateRandomNumbers(Options.instance().numberOfElements(),
                                   Integer.MAX_VALUE);

        // Demonstrate Java 19 structured concurrency.
        demoStructuredConcurrency();

        System.out.println("Leaving test");
    }

    /**
     * Demonstrate modern Java structured concurrency by concurrently
     * (1) checking the primality of a {@link List} of random numbers
     * and (2) computing the greatest common divisor (GCD) of pairs of
     * these random numbers.
     */
    public static void demoStructuredConcurrency()
        throws ExecutionException, InterruptedException {

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
                .submit(() -> checkPrimalities(sRANDOM_INTEGERS));

            gcdComputeFutures = executor
                // submit() starts a virtual thread to compute GCDs
                // concurrently.
                .submit(() -> computeGCDs(sRANDOM_INTEGERS));

            // Don't exit the try-with-resources scope until all
            // concurrently executing tasks complete.
        } 

        Options.display("printing results");

        // The Future.get() calls below don't block since the
        // try-with-resources scope above won't exit until all tasks
        // complete.

        // Print the primality results.  
        primeCheckFutures
            .get()
            .forEach(future -> System.out
                     .println("result = "
                              + rethrowSupplier(future::get).get()));

        // Print the GCD results.
        gcdComputeFutures
            .get()
            .forEach(future -> System.out
                     .println("result = "
                              + rethrowSupplier(future::get).get()));
    }                              

    /**
     * Check the primality of the {@code integers} param.  This method
     * also demonstrates how structured concurrency scopes can nest.
     *
     * @param integers The {@link List} of {@link Integer} objects upon
     *                 which to check for primality
     * @return A {@link List} of {@link Future} objects that return
     *         {@link PrimeResult} objects
     */
    private static List<Future<PrimeResult>> checkPrimalities
        (List<Integer> integers) {
        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (ExecutorService executor =
             Executors.newVirtualThreadPerTaskExecutor()) {
            return integers
                // Create a stream of Integer objects.
                .stream()

                // Check the primality of each number concurrently.
                .map(primeCandidate ->
                     // Use executor to start a virtual thread.
                     checkPrimality(primeCandidate, executor))

                // Trigger intermediate processing and collect/return
                // results in a List of Future<PrimeResult> objects.
                .toList();
        }
    }

    /**
     * Checks whether {@code primeCandidate} is a prime number or not.
     *
     * @param primeCandidate The number to check for primality
     * @param executor {@link ExecutorService} to perform the task
     * @return A {@link Future} that emits a {@link PrimeResult}
     */
    private static Future<PrimeResult> checkPrimality(int primeCandidate,
                                                      ExecutorService executor) {
        return executor
            // submit() starts a virtual thread to check primality
            // concurrently.
            .submit(() -> {
                    // Determine if primeCandidate is prime.
                    int result = MathUtils.isPrime(primeCandidate);

                    Options.display(primeCandidate + " = " + result);

                    // Create a record to hold the results.
                    return new PrimeResult(primeCandidate, result);
                });
    }

    /**
     * Compute the GCD of the {@code integers} param.  This method
     * also demonstrates how structured concurrency scopes can nest.
     *
     * @param integers The {@link List} of {@link Integer} objects upon
     *                 which to compute the GCD
     * @return A {@link List} of {@link Future} objects that return
     *         {@link GCDResult} objects
     */
    private static List<Future<GCDResult>> computeGCDs(List<Integer> integers) {
        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (ExecutorService executor =
             Executors.newVirtualThreadPerTaskExecutor()) {
            return StreamSupport
                // Convert the List of Integer objects into a
                // sequential stream of two-element Integer objects
                // used to compute the GCD.
                .stream(new ListSpliterator(integers), false)

                // Compute all the GCDs concurrently.
                .map((Integer[] params) ->
                     // Use executor to start a virtual thread.
                     computeGCD(params, executor))

                // Trigger intermediate processing and collect results
                // into a List of Future<GCDResult> objects.
                .toList();
        }
    }

    /**
     * Compute the GCD of the two-element array {@code integers}.
     *
     * @param integers A two-element array containing the numbers to
     *                 compute the GCD
     * @param executor {@link ExecutorService} to perform the task
     * @return A {@link Future} that emits a {@link GCDResult}
     */
    private static Future<GCDResult> computeGCD(Integer[] integers,
                                                ExecutorService executor) {
        return executor
            // submit() starts a virtual thread to compute the GCD
            // concurrently.
            .submit(() -> {
                    // Compute GCD.
                    int result = MathUtils.gcd(integers[0], integers[1]);

                    Options.display(integers[0]
                                    + " = "
                                    + integers[1]
                                    + " = "
                                    + result);

                    // Create a record to hold the results.
                    return new GCDResult(integers[0], integers[1], result);
                });
    }
}
