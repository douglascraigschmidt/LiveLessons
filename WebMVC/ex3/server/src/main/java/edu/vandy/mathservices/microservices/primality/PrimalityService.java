package edu.vandy.mathservices.microservices.primality;

import edu.vandy.mathservices.common.Options;
import edu.vandy.mathservices.common.PrimeResult;
import edu.vandy.mathservices.microservices.gcd.GCDController;
import edu.vandy.mathservices.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static edu.vandy.mathservices.utils.FutureUtils.convertFutures;

/**
 * This class defines implementation methods that are called by the
 * {@link GCDController}. These implementation methods check the
 * primality of one or more {@link Integer} objects using the Java
 * structured concurrency framework via the {@link Executors} {@code
 * newVirtualThreadPerTaskExecutor}.
 *
 * This class is annotated as a Spring {@code @Service}, which
 * indicates this class implements "business logic" and enables the
 * auto-detection and wiring of dependent implementation classes via
 * classpath scanning.
 */
@Service
public class PrimalityService {
    /**
     * Check the primality of the {@code integers} param.
     *
     * @param primeCandidates The {@link List} of {@link Integer} objects
     *                        to check for primality
     * @return A {@link List} of {@link PrimeResult} objects
     */
    public List<PrimeResult> checkPrimalities
        (List<Integer> primeCandidates) {
        // Create a List to hold the results.
        List<Future<PrimeResult>> results;

        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (var executor = Executors
             .newVirtualThreadPerTaskExecutor()) {
            results = getFutures(primeCandidates, executor);
            // The block doesn't exit until all tasks are done.
        }

        // Convert the List of Future<PrimeResult> objects to a List
        // of PrimeResult objects.
        return convertFutures(results);
    }

    /**
     * Get a {@link List} of {@link Future<PrimeResult>} objects
     * corresponding to the {@link List} of {@code primeCandidates}.
     *
     * @param primeCandidates The {@link List} of {@link Integer} objects
     *                        used as input to the primality computations
     * @param executor The {@link ExecutorService} used to fork
     *                 virtual {@link Thread} objects
     * @return A {@link List} of {@link Future<PrimeResult>} objects
     */
    @NotNull
    private static List<Future<PrimeResult>> getFutures
        (List<Integer> primeCandidates,
         ExecutorService executor) {
        return primeCandidates
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

                    Options.display("primality of "
                                    + primeCandidate
                                    + " = "
                                    + result);

                    // Create a record to hold the results.
                    return new PrimeResult(primeCandidate, result);
                });
    }
}
