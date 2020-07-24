import utils.AsyncTester;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously using Flux features in the Reactor framework,
 * including create(), interval(), map(), doOnNext(), take(), and
 * subscribe().
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex2 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test a stream of random BigIntegers to determine which
        // values are prime using an asynchronous time-driven Flux
        // stream.
        AsyncTester.register(FluxEx::testIsPrime);

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
