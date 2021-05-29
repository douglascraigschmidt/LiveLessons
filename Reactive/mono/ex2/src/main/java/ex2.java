import reactor.core.publisher.Mono;
import utils.AsyncTaskBarrier;
import utils.BigFraction;

/**
 * This example shows how to reduce, multiply, and display
 * BigFractions asynchronously using various Project Reactor Mono
 * operators, including fromCallable(), just(), subscribeOn(), map(),
 * doOnSuccess(), blockOptional(), onErrorResume(), then(), and the
 * Scheduler.single() thread "pool".
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex2 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test asynchronous BigFraction reduction using a Mono and a
        // pipeline of operations that run off the calling thread.
        AsyncTaskBarrier.register(MonoEx::testFractionReductionAsync1);

        //Test asynchronous BigFraction reduction using a Mono and a
        // pipeline of operations that run in the background (i.e.,
        // off the calling thread), but the result is printed in a
        // timed-blocking manner by the main thread.
        AsyncTaskBarrier.register(MonoEx::testFractionReductionAsync2);

        // Test hybrid asynchronous BigFraction multiplication using a
        // mono and a callable, where the processing is performed in a
        // background thread and the result is printed in a blocking
        // manner by the main thread.
        AsyncTaskBarrier.register(MonoEx::testFractionMultiplicationCallable1);

        // Test asynchronous BigFraction multiplication using a mono
        // and a callable, where the processing and the printing of
        // the result is handled in a non-blocking manner by a
        // background thread.
        AsyncTaskBarrier.register(MonoEx::testFractionMultiplicationCallable2);

        // Test asynchronous BigFraction multiplication using a mono
        // and a callable, where the processing and the printing of
        // the result is handled in a non-blocking manner by a
        // background thread and exceptions are handled gracefully.
        AsyncTaskBarrier.register(MonoEx::testFractionMultiplicationErrorHandling);

        @SuppressWarnings("ConstantConditions")
        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .block();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
