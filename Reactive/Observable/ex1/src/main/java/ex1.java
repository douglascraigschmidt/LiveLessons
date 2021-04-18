import utils.AsyncTaskBarrier;

/**
 * This example shows how to apply RxJava features synchronously to
 * perform basic Observable operations, including just(),
 * fromCallable(), fromArray(), doOnNext(), map(), mergeWith(),
 * repeat(), and blockingSubscribe().
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ex1 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Test BigFraction multiplication using a synchronous
        // Observable stream.
        AsyncTaskBarrier.register(ObservableEx::testFractionMultiplicationSync1);

        // Another BigFraction multiplication test using a synchronous
        // Observable stream.
        AsyncTaskBarrier.register(ObservableEx::testFractionMultiplicationSync2);

        // A test of BigFraction multiplication using an synchronous
        // Observable stream that merges results together.
        AsyncTaskBarrier.register(ObservableEx::testFractionMultiplicationSync3);

        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running.
            .blockingGet();

        // Print the results.
        System.out.println("Completed " + testCount + " tests");
    }
}
