import utils.AsyncTaskBarrier;

/**
 * This example shows how to apply RxJava features asynchronously and
 * concurrently reduce, multiply, and display BigFractions via various
 * Single operations, including fromCallable(), subscribeOn(),
 * zipArray(), zipWith(), doOnSuccess(), ignoreElement(), and
 * Schedulers.computation().
 */
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test asynchronous BigFraction multiplication using flatMap().
        AsyncTaskBarrier.register(SingleEx::testFractionMultiplyAsync);

        // Test asynchronous BigFraction multiplication and addition
        // using zipWith().
        AsyncTaskBarrier.register(SingleEx::testFractionCombine1);

        // Test asynchronous BigFraction multiplication and addition
        // using zipArray().
        AsyncTaskBarrier.register(SingleEx::testFractionCombine2);

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
