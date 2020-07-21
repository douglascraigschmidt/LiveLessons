import ex.FluxEx;
import ex.MonoEx;
import utils.AsyncTester;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously using many advanced Mono and Flux features in the
 * Reactor framework, including flatMap(), collectList(), zipWith(),
 * first(), take(), when(), subscribeOn(), create(), and various
 * thread pools.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex2 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test asynchronous BigFraction reduction using a Mono and a
        // pipeline of operations that run off the calling thread.
        AsyncTester.register(MonoEx::testFractionReductionAsync);

        // Test asynchronous BigFraction multiplication using a mono,
        // callable, and the common fork-join pool.
        AsyncTester.register(MonoEx::testFractionMultiplicationCallable);

        // Test asynchronous BigFraction multiplication and addition
        // using zipWith().
        AsyncTester.register(MonoEx::testFractionCombine);

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
            // Run all the asynchronous tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .block();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
