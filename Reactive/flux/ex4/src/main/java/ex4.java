import utils.AsyncTaskBarrier;

/**
 * This example shows how to apply Project Reactor features
 * asynchronously to perform a range of Flux operations, including
 * fromArray(), map(), flatMap(), collect(), and various types of
 * thread pools.  It also shows various Mono operations, such as
 * when(), firstWithSignal(), materialize(), flatMap(), flatMapMany(),
 * subscribeOn(), and the parallel thread pool.  In addition, it
 * demonstrates how to combine the Java streams framework with the
 * Project Reactor framework.
 */
public class ex4 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test BigFraction multiplications by combining the Java
        // streams framework with the Reactor framework and the common
        // fork-join pool.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplicationsStreams);

        // A test of BigFraction multiplication using an asynchronous
        // Flux stream and a Subscriber implementation.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplicationsBlockingSubscriber);

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
