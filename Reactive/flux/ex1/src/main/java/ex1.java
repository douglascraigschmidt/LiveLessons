import utils.AsyncTester;

/**
 * This example shows how to apply Project Reactor features
 * synchronously to perform basic Flux operations, including just(),
 * map(), and subscribe().
 */
public class ex1 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test BigFraction multiplication using a synchronous Flux
        // stream.
        AsyncTester.register(FluxEx::testFractionMultiplication);

        @SuppressWarnings("ConstantConditions")
        long testCount = AsyncTester
            // Run all the tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running.
            .block();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
