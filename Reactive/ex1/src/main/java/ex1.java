import ex.FluxEx;
import ex.MonoEx;
import utils.AsyncTester;

/**
 * This class shows how to apply Project Reactor features
 * synchronously to perform basic Mono and Flux operations, including
 * just(), map(), subscribe(), fromCallable(), map(), doOnSuccess(),
 * and then().
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
