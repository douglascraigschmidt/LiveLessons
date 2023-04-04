import utils.AsyncTaskBarrier;

/**
 * This example shows how to apply Project Reactor features
 * asynchronously to perform a range of Flux operations, including
 * flatMap(), collect(), subscribeOn(), and various types of thread
 * pools.  It also shows various Mono operations, such as when(),
 * firstWithSignal(), materialize(), flatMap(), flatMapMany(),
 * flatMapIterable(), subscribeOn(), and the parallel thread pool.  In
 * addition, it demonstrates how to combine the Java streams framework
 * with the Project Reactor framework.
 */
public class ex5 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test BigFraction multiplications by combining the Java
        // Streams framework with the Project Reactor framework and
        // the Java common fork-join pool.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplicationsStreams);

        // Test BigFraction multiplications by combining the Java
        // Streams framework with the Project Reactor framework and
        // the Java common fork-join pool in a slightly different way.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplicationsStreamsEx1);

        // Test BigFraction multiplications by combining the Java
        // Streams framework with the Project Reactor framework and
        // the Java common fork-join pool in yet another slightly
        // different way.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplicationsStreamsEx2);

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
