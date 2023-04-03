import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tests.ConcurrentMonos;
import tests.ParallelFluxes;

import java.util.Random;

/**
 * This example shows how to apply timeouts with the Project Reactor
 * framework.
 */
public class ex5 {
    /**
     * The number of iterations to run the test.
     */
    private static final int sMAX_ITERATIONS = 5;

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        // Run a test that demonstrates timeouts for Project Reactor
        // concurrent Monos.
        ConcurrentMonos.runTest(sMAX_ITERATIONS);

        // Run a test that demonstrates timeouts for Project Reactor
        // ParallelFluxes.
        ParallelFluxes.runTest(sMAX_ITERATIONS);
    }
}
