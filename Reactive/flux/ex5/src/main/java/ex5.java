import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import utils.AsyncTaskBarrier;

/**
 * This example demonstrates how to use various {@link Flux}, {@link
 * ParallelFlux}, and {@link Mono} operators to perform BigFraction
 * multiplications and additions in parallel, as well as download and
 * store images from remote web servers in parallel.  The {@link Flux}
 * operators include fromArray(), parallel(), doOnComplete(), and
 * collect().  The {@link ParallelFlux} operators include runOn(),
 * map(), doOnNext(), reduce(), sequential().  The {@link Mono}
 * operators include doOnSuccess() and then().
 */
public class ex5 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Use a ParallelFlux and a pool of threads to perform
        // BigFraction multiplications in parallel.
        AsyncTaskBarrier.register(ParallelFluxEx::testFractionMultiplications1);

        // Use a ParallelFlux and a pool of threads to perform
        // BigFraction multiplications and additions in parallel.
        AsyncTaskBarrier.register(ParallelFluxEx::testFractionMultiplications2);

        // Use a ParallelFLux to download and store images from remote
        // web servers in parallel.
        AsyncTaskBarrier.register(ParallelFluxEx::testParallelDownloads);

        @SuppressWarnings("ConstantConditions")
        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .block();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
