package edu.vandy.mathservices.microservices.gcd;

import edu.vandy.mathservices.common.Options;
import edu.vandy.mathservices.utils.MathUtils;
import jdk.incubator.concurrent.StructuredTaskScope;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

import edu.vandy.mathservices.common.GCDResult;
import edu.vandy.mathservices.common.ListSpliterator;

import static edu.vandy.mathservices.utils.FutureUtils.convertFutures;

/**
 * This class defines implementation methods that are called by the
 * {@link GCDController}. These implementation methods check the
 * primality of one or more {@link Integer} objects using the Java
 * structured concurrency framework via the {@link
 * StructuredTaskScope} classes.
 *
 * This class is annotated as a Spring {@code @Service}, which
 * indicates this class implements "business logic" and enables the
 * auto-detection and wiring of dependent implementation classes via
 * classpath scanning.
 */
@Service
public class GCDService {
    /**
     * Concurrently compute the GCD of the {@code integers} param.
     *
     * @param integers The {@link List} of {@link Integer} objects
     *                 upon which to compute the GCD
     * @return A {@link List} of {@link GCDResult} objects
     */
    public List<GCDResult> computeGCDs(List<Integer> integers) {
        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (var scope =
             new StructuredTaskScope.ShutdownOnFailure()) {
            // Get the results.
            var results =
                getFutures(integers, scope);

            // This barrier synchronizer waits for tasks to complete
            // successfully or throw an Exception upon failure.
            scope.join().throwIfFailed();

            // Convert the List of Future<GCDResult> objects to a List
            // of GCDResult objects.
            return convertFutures(results);
        } catch (Exception exception) {
            System.out.println("Exception: " + exception.getMessage());
            throw new RuntimeException(exception);
        }
    }

    /**
     * Get a {@link List} of {@link Future<GCDResult>} objects
     * corresponding to the {@link List} of {@code integers}.
     *
     * @param integers The {@link List} of {@link Integer} objects
     *                 used as input to the GCD computations
     * @param scope The {@link StructuredTaskScope.ShutdownOnFailure}
     *              used to fork virtual {@link Thread} objects
     * @return A {@link List} of {@link Future<GCDResult>} objects
     */
    @NotNull
    private static List<Future<GCDResult>> getFutures
        (List<Integer> integers,
         StructuredTaskScope.ShutdownOnFailure scope) {
        return StreamSupport
            // Convert the List of Integer objects into a
            // sequential stream of two-element Integer objects
            // used to compute the GCD.
            .stream(new ListSpliterator(integers),
                    false)

            // Compute all the GCDs concurrently.
            .map((Integer[] params) ->
                 // Use executor to start a virtual thread.
                 computeGCD(params, scope))

            // Trigger intermediate processing and collect results
            // into a List of Future<GCDResult> objects.
            .toList();
    }

    /**
     * Compute the GCD of the two-element array {@code integers}.
     *
     * @param integers A two-element array containing the numbers to
     *                 compute the GCD
     * @param scope {@link StructuredTaskScope.ShutdownOnFailure} to perform the task
     * @return A {@link Future} that emits a {@link GCDResult}
     */

    private static Future<GCDResult> computeGCD
        (Integer[] integers,
         StructuredTaskScope.ShutdownOnFailure scope) {
        return scope
            // submit() starts a virtual thread to compute the GCD
            // concurrently.
            .fork(() -> {
                    // Compute GCD.
                    int result = MathUtils
                        .gcd(integers[0], integers[1]);

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
