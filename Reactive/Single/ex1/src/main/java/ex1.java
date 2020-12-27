import utils.AsyncTaskBarrier;

/**
 * This example shows how to apply RxJava features synchronously to
 * reduce, multiply, and display BigFractions via basic Single operations,
 * including fromCallable(), map(), doOnSuccess(), and ignoreElement().
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex1 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test synchronous BigFraction reduction using a Single and a
        // pipeline of operations that run on the calling thread.
        AsyncTaskBarrier.register(SingleEx::testFractionReductionSync1);

        // Test synchronous BigFraction reduction using a Single and a
        // pipeline of operations that run on the calling thread.
        // Combines the Single with Java functional programming
        // features.
        AsyncTaskBarrier.register(SingleEx::testFractionReductionSync2);

        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running.
            .blockingGet();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
