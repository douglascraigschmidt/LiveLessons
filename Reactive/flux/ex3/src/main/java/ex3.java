import utils.AsyncTester;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously and concurrently using many advanced Flux features
 * in the Project Reactor framework, including just(), create(),
 * map(), flatMap(), collectList(), take(), subscribeOn(), and various
 * types of thread pools.  It also shows advanced Mono operations,
 * such as first() and when().  It also demonstrates how to combine
 * the Java streams framework with the Project Reactor framework.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test BigFraction exception handling using a synchronous
        // Flux stream.
        AsyncTester.register(FluxEx::testFractionExceptions);

        // Test BigFraction multiplications using a stream of monos
        // and a pipeline of operations, including flatMap(),
        // collectList(), and first().
        AsyncTester.register(FluxEx::testFractionMultiplications1);

        // Test BigFraction multiplications by combining the Java
        // streams framework with the Reactor framework and the
        // common fork-join pool.
        AsyncTester.register(FluxEx::testFractionMultiplications2);

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
