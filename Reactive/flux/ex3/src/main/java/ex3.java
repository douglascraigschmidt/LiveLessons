import utils.AsyncTaskBarrier;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously and concurrently using many advanced Flux features
 * in the Project Reactor framework, including fromIterable(),
 * generate(), map(), flatMap(), onErrorResume(), onErrorStop(),
 * onErrorContinue(), collectList(), collect(), reduce(), take(),
 * filter(), and various types of thread pools.  It also shows various
 * Mono operations, such as flatMap(), onErrorResume(), firstWithSignal(),
 * subscribeOn(), and the parallel thread pool.
 */
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Test Flux exception handling via onErrorResume() using a
        // synchronous Flux stream (with asynchrony only at the end of
        // the stream).
        AsyncTaskBarrier.register(FluxEx::testFractionException1);

        // Test Flux exception handling via onErrorContinue() using a
        // synchronous Flux stream (with asynchrony only at the end of
        // the stream).
        AsyncTaskBarrier.register(FluxEx::testFractionException2);

        // Test Mono exception handling via onErrorResume() using an
        // asynchronous Flux stream and a pool of threads.
        AsyncTaskBarrier.register(FluxEx::testFractionException3);

        // Test an asynchronous Flux stream consisting of generate(),
        // take(), flatMap(), collect() and a pool of threads to
        // perform BigFraction reductions and multiplications.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplications1);

        // Test an asynchronous Flux stream consisting of
        // fromIterable(), flatMap(), reduce(), and a pool of threads
        // to perform BigFraction multiplications.
        AsyncTaskBarrier.register(FluxEx::testFractionMultiplications2);

        // Test an asynchronous Flux stream consisting of
        // fromIterable(), flatMap(), collectMap(), and a pool of threads
        // to perform BigFraction reductions and multiplications.
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
