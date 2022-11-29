import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import utils.AsyncTaskBarrier;

/**
 * This example shows how to multiply and add big fractions
 * asynchronously and concurrently using RxJava {@link Flowable}
 * operators, including fromArray() and parallel(), and {@link
 * ParallelFlowable} operators, including runOn(), flatMap(),
 * reduce(), and firstElement(), as well as the Schedulers.computation()
 * thread pool.
 */
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Use a ParallelFlowable stream and a pool of threads to perform
        // BigFraction multiplications and additions in parallel.
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
