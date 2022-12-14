package mathservices.common;

import mathservices.utils.MathUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

/**
 *
 */
public class GCDService {
    /**
     * Compute the GCD of the {@code integers} param.  This method
     * also demonstrates how structured concurrency scopes can nest.
     *
     * @param integers The {@link List} of {@link Integer} objects upon
     *                 which to compute the GCD
     * @return A {@link List} of {@link Future} objects that return
     *         {@link GCDResult} objects
     */
    public static List<Future<GCDResult>> computeGCDs(List<Integer> integers) {
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
