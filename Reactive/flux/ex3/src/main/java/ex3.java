import utils.AsyncTaskBarrier;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously and concurrently using many advanced Flux features
 * in the Project Reactor framework, including fromIterable(), map(),
 * create(), flatMap(), filter(), collectList(), subscribeOn(),
 * take(), and various types of thread pools.  It also shows advanced
 * Mono operations, such as first(), when(), flatMap(), subscribeOn(),
 * and the parallel thread pool.  It also demonstrates how to combine
 * the Java streams framework with the Project Reactor framework.
 */
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test BigFraction exception handling using a synchronous
        // Flux stream.
        AsyncTaskBarrier.register(FluxEx::testFractionExceptions);

        // Test BigFraction multiplications using a stream of monos
        // and a pipeline of operations, including create(), take(),
        // flatMap(), collectList(), and first().
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplications1);

        // Use an asynchronous Flux stream and a pool of threads to
        // perform BigFraction multiplications and additions.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplications2);

        // Test BigFraction multiplications by combining the Java
        // streams framework with the Reactor framework and the common
        // fork-join pool.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplications3);

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
