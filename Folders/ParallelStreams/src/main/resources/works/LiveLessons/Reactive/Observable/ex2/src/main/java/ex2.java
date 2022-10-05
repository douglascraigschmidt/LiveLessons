import utils.AsyncTaskBarrier;

/**
 * This example shows how to apply RxJava features asynchronously to
 * perform various Observable operations, including create(),
 * interval(), filter(), doOnNext(), doOnComplete(), doFinally(),
 * take(), map(), subscribe(), range(), subscribeOn(), observeOn(),
 * and various thread pools.
 */
public class ex2 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test a stream of random BigIntegers to determine which
        // values are prime using an asynchronous time-driven Observable
        // stream.
        AsyncTaskBarrier.register(ObservableEx::testIsPrimeTimed);

        // Test a stream of random BigIntegers to determine which
        // values are prime using an asynchronous Observable stream.
        AsyncTaskBarrier.register(ObservableEx::testIsPrimeAsync);

        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .blockingGet();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
