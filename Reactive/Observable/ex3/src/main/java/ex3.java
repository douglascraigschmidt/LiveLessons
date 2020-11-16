import utils.AsyncTester;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously and concurrently using many advanced RxJava
 * Observable operations, including fromIterable(), map(), create(),
 * flatMap(), flatMapCompletable(), filter(), collectInto(), take(),
 * subscribeOn(), and various types of thread pools.  It also shows
 * advanced RxJava Single operations, such as ambArray(), when(),
 * flatMap(), subscribeOn(), and the parallel thread pool.
 */
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Test BigFraction exception handling using a synchronous
        // Observable stream.
        AsyncTester.register(ObservableEx::testFractionExceptions);

        // Test BigFraction multiplications using a stream of Singles
        // and a pipeline of operations, including create(), take(),
        // flatMap(), collectList(), and first().
        AsyncTester.register(ObservableEx::testFractionMultiplications1);

        long testCount = AsyncTester
            // Run all the tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .blockingGet();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
