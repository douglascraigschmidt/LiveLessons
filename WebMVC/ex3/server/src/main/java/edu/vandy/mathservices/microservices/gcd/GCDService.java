package edu.vandy.mathservices.microservices.gcd;

import edu.vandy.mathservices.common.GCDParam;
import edu.vandy.mathservices.common.GCDResult;
import edu.vandy.mathservices.common.ListSpliterator;
import edu.vandy.mathservices.common.Options;
import edu.vandy.mathservices.utils.MathUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

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
            // Convert the List of Integer objects into a parallel
            // stream of GCDParam objects used to compute the GCD.
            .stream(new ListSpliterator(integers),
                    true)

            // Compute all the GCDs concurrently.
            .map(GCDService::computeGCD)

            // Trigger intermediate processing and collect results
            // into a List of GCDResult objects.
            .toList();
    }

    /**
     * Compute the GCD of the {@link GCDParam}.
     *
     * @param param A {@link GCDParam} containing the numbers used to
     *              compute the GCD
     * @return A {@link GCDResult}
     */

    private static GCDResult computeGCD(GCDParam param) {
        int result = MathUtils
            // Compute the GCD.
            .gcd(param.int1(),
                 param.int2());

        Options.display(STR."GCD of \{param.int1()} & \{param.int2()} = \{result}");

        // Create a GCDResult record to hold the results.
        return new GCDResult(param,
                             result);
    }
}
