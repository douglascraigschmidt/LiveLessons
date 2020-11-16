import utils.AsyncTester;

/**
 * This example shows how to apply RxJava features asynchronously and
 * concurrently reduce, multiply, and display BigFractions via various
 * Single operations, including fromCallable(), subscribeOn(), zipWith(),
 * doOnSuccess(), ignoreElement(), and Schedulers.computation().
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test asynchronous BigFraction multiplication and addition
        // using zipWith().
        AsyncTester.register(SingleEx::testFractionCombine);

        @SuppressWarnings("ConstantConditions")
            long testCount = AsyncTester
            // Run all the tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .blockingGet();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
