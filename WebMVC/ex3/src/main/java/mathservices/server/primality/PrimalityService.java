package mathservices.server.primality;

import mathservices.common.GCDResult;
import mathservices.common.Options;
import mathservices.common.PrimeResult;
import mathservices.server.gcd.GCDController;
import mathservices.utils.MathUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static mathservices.utils.FutureUtils.convertFutures;

/**
 * This class defines implementation methods that are called by the
 * {@link GCDController}. These implementation methods check the
 * primality of one or more {@link Integer} objects using the Java
 * structured concurrency framework via a {@code
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
        List<Future<PrimeResult>> results = new ArrayList<>();

        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (ExecutorService executor =
             Executors.newVirtualThreadPerTaskExecutor()) {
            results = primeCandidates
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

        // Convert the List of Future<PrimeResult> objects to a List
        // of PrimeResult objects.
        return convertFutures(results);
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
