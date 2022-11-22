import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import utils.AsyncTaskBarrier;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously and concurrently using RxJava {@link ParallelFlowable}
 * operations, including fromArray(), parallel(), runOn(), flatMap(),
 * sequential(), reduce(), and Schedulers.computation().
 */
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Use a {@link ParallelFlowable} stream and a pool of threads to
        // perform BigFraction multiplications and additions in parallel.
        AsyncTaskBarrier.register(FlowableEx::testFractionMultiplications);

        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .blockingGet();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
