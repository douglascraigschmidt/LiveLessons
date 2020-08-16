import utils.AsyncTester;

/**
 * This example shows how to apply RxJava features asynchronously to
 * perform various Observable operations, including create(),
 * interval(), map(), filter(), doOnNext(), doOnComplete(), take(),
 * subscribe(), ignoreElement(), range(), subscribeOn(), observeOn(),
 * count(), and various thread pools.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex2 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test a stream of random BigIntegers to determine which
        // values are prime using an asynchronous time-driven Flux
        // stream.
        AsyncTester.register(ObservableEx::testIsPrimeTimed);

        // Test a stream of random BigIntegers to determine which
        // values are prime using an asynchronous Flux stream.
        AsyncTester.register(ObservableEx::testIsPrimeAsync);

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
