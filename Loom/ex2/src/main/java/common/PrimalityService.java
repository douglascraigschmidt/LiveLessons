package common;

import utils.MathUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This Java utility class defines methods for checking whether a
 * {@link List} of {@link Integer} objects are prime using Java
 * structure concurrency and the Java streams framework.
 */
public class PrimalityService {
    /**
     * A Java utility class should have a private constructor.
     */
    private PrimalityService() {}

    /**
     * Check the primality of the {@code integers} param.  This method
     * also demonstrates how structured concurrency scopes can nest.
     *
     * @param integers The {@link List} of {@link Integer} objects upon
     *                 which to check for primality
     * @return A {@link List} of {@link Future} objects that return
     *         {@link PrimeResult} objects
     */
    public static List<Future<PrimeResult>> checkPrimalities
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
}
