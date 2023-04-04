import utils.AsyncTaskBarrier;

/**
 * This example shows how to apply Project Reactor features
 * asynchronously to perform a range of Flux operations, including
 * fromArray(), flatMap(), and subscribe().  It also shows various
 * Mono operations, such as fromSupplier(), repeat(), flatMap(),
 * flatMapMany(), flatMapIterable(), subscribeOn(), and the parallel
 * thread pool.  It also shows how to implement a blocking subscriber
 * that uses various type of backpressure mechanisms.
 */
public class ex4 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // A test of BigFraction multiplication using an asynchronous
        // Flux stream and a blocking Subscriber implementation that
        // doesn't apply backpressure.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplicationsBlockingSubscriber1);

        // A test of BigFraction multiplication using an asynchronous
        // Flux stream and a blocking Subscriber implementation that
        // applies backpressure.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplicationsBlockingSubscriber2);

        // A test of BigFraction multiplication using an asynchronous
        // Flux stream with a backpressure strategy and a blocking
        // Subscriber implementation.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplicationsBackpressureStrategy);

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
