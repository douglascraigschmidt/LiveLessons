package common;

import utils.MathUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

/**
 * This Java utility class defines methods for computing the Greatest
 * Common Divisor (GCD) of {@link List} of {@link Integer} objects using
 * Java structured concurrency and the Java streams framework.
 */
public class GCDService {
    /**
     * A Java utility class should have a private constructor.
     */
    private GCDService() {}

    /**
     * Compute the GCD of the {@code integers} param.  This method
     * also demonstrates how structured concurrency scopes can nest.
     *
     * @param integers The {@link List} of {@link Integer} objects upon
     *                 which to compute the GCD
     * @return A {@link List} of {@link Future} objects that return
     *         {@link GCDResult} objects
     */
    public static List<Future<GCDResult>> computeGCDs
        (List<Integer> integers) {
        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (var executor = Executors
             .newVirtualThreadPerTaskExecutor()) {
            return StreamSupport
                // Convert the List of Integer objects into a
                // sequential stream of GCDParam objects used to
                // compute the GCD.
                .stream(new ListSpliterator(integers), false)

                // Compute all the GCDs concurrently.
                .map(param ->
                     // Use executor to start a virtual thread.
                     computeGCD(param, executor))

                // Trigger intermediate processing and collect results
                // into a List of Future<GCDResult> objects.
                .toList();
        }
    }

    /**
     * Compute the GCD of the {@link GCDParam}.
     *
     * @param param A {@link GCDParam} containing the numbers to
     *              compute the GCD
     * @param executor {@link ExecutorService} to perform the task
     * @return A {@link Future} that emits a {@link GCDResult}
     */
    private static Future<GCDResult> computeGCD(GCDParam param,
                                                ExecutorService executor) {
        return executor
            // submit() starts a virtual thread to compute the GCD
            // concurrently.
            .submit(() -> {
                    // Compute the GCD.
                    int result = MathUtils.gcd(param.int1(), param.int2());

                    Options.display("GCD of "
                                    + param.int1()
                                    + " & "
                                    + param.int2()
                                    + " = "
                                    + result);

                    // Create a GCDResult record to hold the results.
                    return new GCDResult(param, result);
                });
    }
}
