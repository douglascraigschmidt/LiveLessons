import utils.ArraySpliterator;
import utils.GCDResult;
import utils.PrimeResult;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static utils.ExceptionUtils.rethrowConsumer;
import static utils.ExceptionUtils.rethrowSupplier;

/**
 * This example demonstrates various features of Project Loom,
 * including virtual threads and structured concurrency.  You'll need
 * to install JDK 18 with Project Loom configured, which you can
 * download from https://jdk.java.net/loom.
 */
public class ex35 {
    /**
     * The number of times to recurse.
     */
    private static final int sMAX = 500;

    /**
     * The number of virtual threads to create/start.
     */
    private static final int sNUMBER_OF_THREADS = 20_000;

    /**
     * Values to use for various computations.
     */
    private static final Integer[] sBIG_INTS = {
        999_023_101,
        999_032_013,
        998_203_141,
        999_303_242,
        455_052_511,
        179_424_673,
        989_330_420,
        989_301_301,
        998_031_031,
        999_999_977
    };


    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv)
        throws ExecutionException, InterruptedException {
        startManyVirtualThreads();
        structuredConcurrency();
    }

    /**
     * Demonstrate how to create and start many virtual threads using
     * Project Loom.
     */
    private static void startManyVirtualThreads() {
        // Create a List of many virtual threads.
        List<Thread> threads = IntStream
            // Generate a range of ints.
            .rangeClosed(1, sNUMBER_OF_THREADS)

            // Print out a diagnostic every 1,000 ints.
            .peek(i -> {
                    if (i % 1_000 == 0)
                        System.out.println(i + " thread started");
                })

            // Make a new virtual thread for each int.
            .mapToObj(__ -> makeThread(() -> looper(1, sMAX)))

            // Collect the Thread objects into a List.
            .collect(toList());

        // Start all virtual threads.
        threads.forEach(Thread::start);

        // Join all virtual threads.
        threads.forEach(rethrowConsumer(Thread::join));
    }

    /**
     * Demonstrate Project Loom structured concurrency.
     */
    public static void structuredConcurrency()
        throws ExecutionException, InterruptedException {

        // Create a List to hold Future<PrimeResult> objects.
        Future<List<Future<PrimeResult>>> primeCheckFutures;

        // Create a List to hold Future<GCDResult> objects.
        Future<List<Future<GCDResult>>> gcdComputeFutures;

        // Create a new scope for executing virtual tasks, which only
        // exits after all tasks are complete.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            // Concurrently check primalities.
            primeCheckFutures = executor.submit(() -> checkPrimalities(sBIG_INTS));

            // Concurrently compute GCDs.
            gcdComputeFutures = executor.submit(() -> computeGCDs(sBIG_INTS));

            // Scope doesn't exit until all concurrent tasks complete.
        } 

        display("printing results");

        // The future::get calls below return immediately since the
        // scope above won't exit until all tasks complete.

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
     * Check the primality of the {@code integers} param.
     *
     * @param integers The integers to check for primality
     * @return A {@link List} of {@link Future} objects that return
     *         {@link PrimeResult} objects
     */
    private static List<Future<PrimeResult>> checkPrimalities
        (Integer[] integers) {

        // Create a new scope for executing virtual tasks, which only
        // exits after all tasks are complete.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            return Stream
                // Create a stream of Integers.
                .of(integers)

                // Concurrently check the primality of each number.
                .map(primeCandidate -> checkPrimality(primeCandidate, executor))

                // Trigger intermediate processing and collect the
                // results into a List.
                .collect(toList());
        }
    }

    /**
     * Compute the GCD of the {@code integers} param.
     *
     * @param integers The integers to compute GCD
     * @return A {@link List} of {@link Future} objects that return
     *         {@link GCDResult} objects
     */
    private static List<Future<GCDResult>> computeGCDs(Integer[] integers) {
        // Create a new scope for executing virtual tasks, which only
        // exits after all tasks are complete.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            return StreamSupport
                // Convert the array of Integer objects into a stream
                // of two-element Integers representing the values to
                // compute the GCD for.
                .stream(new ArraySpliterator(integers), false)

                // Compute the GCD in the context of the executor.
                .map(param -> computeGCD(param, executor))

                // Trigger intermediate processing and return the
                // results as a List.
                .collect(toList());
        }
    }


    /**
     * Checks whether {@code primeCandidate} is a prime number or not.
     *
     * @param primeCandidate The number to check for primality
     * @param executor Executor to perform the task
     * @return A {@link Future} that emits a {@link PrimeResult}
     */
    private static Future<PrimeResult> checkPrimality
        (int primeCandidate, ExecutorService executor) {
        return executor
            // Submit call to executor for concurrent execution.
            .submit(() -> {
                    // Determine if primeCandidate is prime.
                    int result = isPrime(primeCandidate);
                    display(primeCandidate
                            + " = "
                            + result);

                    // Create a record to hold the results.
                    return new PrimeResult(primeCandidate,
                                           result);
                });
    }

    /**
     * Compute whether {@code primeCandidate} is a prime number or not.
     *
     * @param integers A two-element array containing the numbers to
     *                 compute the GCD for
     * @param executor Executor to perform the task
     * @return A {@link Future} that emits a {@link GCDResult}
     */
    private static Future<GCDResult> computeGCD
        (Integer[] integers, ExecutorService executor) {
        return executor
            // Submit call to executor for concurrent execution.
            .submit(() -> {
                    // Compute GCD.
                    int result = gcd(integers[0], integers[1]);

                    display(integers[0]
                            + " = "
                            + integers[1]
                            + " = "                            
                            + result);

                    // Create a record to hold the results.
                    return new GCDResult(integers[0],
                                         integers[1],
                                         result);
                });
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

    /**
     * Provides a recursive implementation of Euclid's algorithm to
     * compute the "greatest common divisor" (GCD).
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
     * A factory method that makes a new unstarted virtual thread that
     * runs the given {@code runnable}.
     */
    public static Thread makeThread(Runnable runnable) {
        return Thread.ofVirtual().unstarted(runnable);
    }

    /**
     * Burn CPU time doing a recursive "loop" until count > {@code max}.
     *
     * @param count The current count
     * @param max The max number of times to recurse
     */
    private static void looper(int count, int max) {
        // Bail out of recursion when count > max.
        if (count > max)
            return;

        // Sleep for a short amount of time.
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Print a diagnostic ever 100 recursions.
        if (count % 100 == 0)
            System.out.println("Thread id: "+ Thread.currentThread().getId() +" : "+ count);

        // Call looper recursively.
        looper(count + 1, max);
    }

    /**
     * This method provides a brute-force determination of whether
     * number {@code primeCandidate} is prime.  Returns 0 if it is
     * prime, or the smallest factor if it is not prime.
     */
    public static int isPrime(int primeCandidate) {
        if (primeCandidate > 3)
            for (int factor = 2;
                 factor <= primeCandidate / 2;
                 ++factor)
                if (primeCandidate / factor * factor == primeCandidate)
                    return factor;

        return 0;
    }
}
