import utils.AsyncTaskBarrier;

/**
 * This example shows how to reduce, multiply, and display BigFractions
 * asynchronously using various Single features in the RxJava framework,
 * including fromCallable(), subscribeOn(), map(), doOnSuccess(),
 * blockOptional(), ignoreElement(), and the Scheduler.single()
 * thread "pool".
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex2 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test asynchronous BigFraction reduction using a Single and a
        // pipeline of operations that run off the calling thread.
        AsyncTaskBarrier.register(SingleEx::testFractionReductionAsync);

        // Test hybrid asynchronous BigFraction multiplication using a
        // Single and a callable, where the processing is performed in a
        // background thread and the result is printed in a blocking
        // manner by the main thread.
        AsyncTaskBarrier.register(SingleEx::testFractionMultiplicationCallable1);

        // Test asynchronous BigFraction multiplication using a Single
        // and a callable, where the processing and the printing of
        // the result is handled in a non-blocking manner by a
        // background thread.
        AsyncTaskBarrier.register(SingleEx::testFractionMultiplicationCallable2);

        @SuppressWarnings("ConstantConditions")
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
