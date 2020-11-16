import utils.AsyncTester;

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
        AsyncTester.register(SingleEx::testFractionReductionSync1);

        // Test synchronous BigFraction reduction using a Single and a
        // pipeline of operations that run on the calling thread.
        // Combines the Single with Java functional programming
        // features.
        AsyncTester.register(SingleEx::testFractionReductionSync2);

        long testCount = AsyncTester
            // Run all the tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running.
            .blockingGet();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
