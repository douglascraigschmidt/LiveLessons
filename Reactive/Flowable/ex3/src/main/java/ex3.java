import utils.AsyncTaskBarrier;

/**
 * This example shows how to apply RxJava features to perform a range
 * of {@link Flowable} operators (such as fromIterator(), parallel(),
 * and collect()), {@link ParallelFlowable} operators (such as
 * runOn(), map(), and sequential()), and {@link Single} operators
 * (such as doOnSuccess() and ignoreElement()), as well as the
 * Schedulers.io() thread pool.
 */
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // A test of ...
        AsyncTaskBarrier.register(FlowableEx::testParallelDownloads);

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
