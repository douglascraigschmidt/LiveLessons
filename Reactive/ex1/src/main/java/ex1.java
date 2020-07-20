import ex.FluxEx;
import ex.MonoEx;
import utils.AsyncTester;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously using a wide range of Mono and Flux features in the
 * Reactor framework, including flatMap(), collectList(), zipWith(),
 * first(), when(), and onErrorResume().
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex1 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test synchronous BigFraction reduction using a mono and a
        // pipeline of operations that run on the calling thread.
        AsyncTester.register(MonoEx::testFractionReductionSync);

        // Test BigFraction multiplication using a synchronous Flux
        // stream.
        AsyncTester.register(FluxEx::testFractionMultiplication);

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
