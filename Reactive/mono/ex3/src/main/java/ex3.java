import utils.AsyncTester;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously and concurrently using more advanced Mono features
 * in the Project Reactor framework, including fromCallable(),
 * subscribeOn(), zipWith(), doOnSuccess(), then(), and the parallel
 * thread pool.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test asynchronous BigFraction multiplication and addition
        // using zipWith().
        AsyncTester.register(MonoEx::testFractionCombine);

        @SuppressWarnings("ConstantConditions")
        long testCount = AsyncTester
            // Run all the tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .block();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
