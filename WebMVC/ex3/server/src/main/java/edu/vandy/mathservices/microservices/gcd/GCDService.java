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
 * parallel streams framework.
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
        return StreamSupport
            // Convert the List of Integer objects into a
            // parallel stream of two-element Integer objects
            // used to compute the GCD.
            .stream(new ListSpliterator(integers),
                    true)

            // Compute all the GCDs concurrently.
            .map(GCDService::computeGCD)

            // Trigger intermediate processing and collect results
            // into a List of GCDResult objects.
            .toList();
    }

    /**
     * Compute the GCD of the two-element array {@code integers}.
     *
     * @param integers A two-element array containing the numbers to
     *                 compute the GCD
     * @return A {@link GCDResult}
     */

    private static GCDResult computeGCD
        (Integer[] integers) {
        // Compute the GCD.
        int result = MathUtils.gcd(integers[0], integers[1]);

        Options.display(integers[0]
                        + " = "
                        + integers[1]
                        + " = "
                        + result);

        // Create a record to hold the results.
        return new GCDResult(integers[0], integers[1], result);
    }
}
