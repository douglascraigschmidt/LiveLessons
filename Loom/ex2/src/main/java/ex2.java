import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Random;

import utils.ListSpliterator;
import utils.Options;
import utils.GCDResult;
import utils.PrimeResult;

import static java.util.stream.Collectors.toList;
import static utils.ExceptionUtils.rethrowConsumer;
import static utils.ExceptionUtils.rethrowSupplier;

/**
 * This example demonstrates Project Loom structured concurrency
 * features, which enables a main task to split into several
 * concurrent sub-tasks that run concurrently to competion before the
 * main task can complete.  Project Loom supports structured
 * concurrency by enhancing {@link ExecutorService} to support
 * AutoCloseable and updating {@link Executors} to define new static
 * factory methods that support usage in a structured manner.  You'll
 * need to install JDK 18 with Project Loom configured, which you can
 * download from https://jdk.java.net/loom.
 */
public class ex2 {
    /**
     * A List of randomly-generated integers.
     */
    private static List<Integer> sRANDOM_INTEGERS;

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv)
        throws ExecutionException, InterruptedException {
        // Initialize any command-line options.
        Options.instance().parseArgs(argv);

        // Generate the random numbers.
        generateRandomNumbers();

        // Demonstrate Project Loom structured concurrency.
        demoStructuredConcurrency();
    }

    /**
     * Generate a list of random Integer objects used for prime number
     * checking.
     */
    private static void generateRandomNumbers() {
        // Generate a list of random integers.
        sRANDOM_INTEGERS = new Random()

            // Generate the given # of large random ints.
            .ints(Options.instance().numberOfElements(),
                  Integer.MAX_VALUE - Options.instance().numberOfElements(),
                  Integer.MAX_VALUE)

            // Convert each primitive int to Integer.
            .boxed()    
                   
            // Trigger intermediate operations and collect into a List.
            .collect(toList());
    }

    /**
     * Demonstrate Project Loom structured concurrency by concurrently
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

        // Create a new scope for executing virtual tasks, which only
        // exits after all tasks are complete (uses the new
        // AutoClosable feature of ExecutorService).
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            primeCheckFutures = executor
                // Submit call to executor to check primalities
                // concurrently.
                .submit(() -> checkPrimalities(sRANDOM_INTEGERS));

            gcdComputeFutures = executor
                // Submit call to executor to compute GCDs
                // concurrently.
                .submit(() -> computeGCDs(sRANDOM_INTEGERS));

            // Scope doesn't exit until all concurrent tasks complete.
        } 

        display("printing results");

        // The Future.get() calls below don't block since the scope
        // above won't exit until all tasks complete.

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
    private static List<Future<PrimeResult>> checkPrimalities(List<Integer> integers) {
        // Create a new scope for executing virtual tasks, which only
        // exits after all tasks are complete (uses the new
        // AutoClosable feature of ExecutorService).
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            return integers
                // Create a stream of Integers.
                .stream()

                // Concurrently check the primality of each number.
                .map(primeCandidate ->
                     checkPrimality(primeCandidate, executor))

                // Trigger intermediate processing and collect results
                // into a List of Future<PrimeResult> objects.
                .collect(toList());
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
            // Submit call to executor to run concurrently.
            .submit(() -> {
                    // Determine if primeCandidate is prime.
                    int result = isPrime(primeCandidate);

                    display(primeCandidate + " = " + result);

                    // Create a record to hold the results.
                    return new PrimeResult(primeCandidate, result);
                });
    }

    /**
     * This method checks if number {@code primeCandidate} is prime.
     *
     * @param primeCandidate The number to check for primality
     * @return 0 if {@code primeCandidate} is prime, or the smallest
     *         factor if it is not prime
     */
    public static int isPrime(int primeCandidate) {
        if (primeCandidate > 3)
            // Use a brute-force algorithm to burn CPU!
            for (int factor = 2;
                 factor <= primeCandidate / 2;
                 ++factor)
                if (primeCandidate / factor * factor == primeCandidate)
                    return factor;

        return 0;
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
        // Create a new scope for executing virtual tasks, which only
        // exits after all tasks are complete (uses the new
        // AutoClosable feature of ExecutorService).
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            return StreamSupport
                // Convert the List of Integer objects into a
                // sequential stream of two-element Integer objects
                // used to compute the GCD.
                .stream(new ListSpliterator(integers), false)

                // Compute the GCD in the context of the executor.
                .map(params -> computeGCD(params, executor))

                // Trigger intermediate processing and collect results
                // into a List of Future<GCDResult> objects.
                .collect(toList());
        }
    }

    /**
     * Compute whether {@code primeCandidate} is a prime number or not.
     *
     * @param integers A two-element array containing the numbers to
     *                 compute the GCD for
     * @param executor {@link ExecutorService} to perform the task
     * @return A {@link Future} that emits a {@link GCDResult}
     */
    private static Future<GCDResult> computeGCD(Integer[] integers,
                                                ExecutorService executor) {
        return executor
            // Submit call to executor for concurrent execution.
            .submit(() -> {
                    // Compute GCD.
                    int result = gcd(integers[0], integers[1]);

                    display(integers[0] + " = " + integers[1] + " = " + result);

                    // Create a record to hold the results.
                    return new GCDResult(integers[0], integers[1], result);
                });
    }

    /**
     * Provides a recursive implementation of Euclid's algorithm to
     * compute the "greatest common divisor" (GCD) of {@code number1}
     * and {@code number2}.
     */
    private static int gcd(int number1, int number2) {
        // Basis case.
        if (number2 == 0)
            return number1;
        // Recursive call.
        return gcd(number2,
                   number1 % number2);
    }

    /**
     * Display {@code message} after printing thread id.
     * @param message The message to display
     */
    private static void display(String message) {
        System.out.println("Thread = "
                               + Thread.currentThread().getId()
                               + " "
                               + message);
    }
}
