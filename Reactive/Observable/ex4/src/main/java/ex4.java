import utils.AsyncTaskBarrier;

/**
 * This example shows how to apply RxJava features asynchronously to
 * perform a range of Observable operations, including fromArray(),
 * fromCallable(), doOnNext(), map(), flatMap(), subscribeOn(),
 * toFlowable(), subscribe(), and a parallel thread pool.  It also
 * shows the Flowable subscribe() operation.  In addition it shows
 * various Single operations, such as zipArray(), ambArray(),
 * subscribeOn(), flatMapObservable(), flatMapCompletable(),
 * ignoreElement(), flatMap(), and a parallel thread pool.  It also
 * shows how to combine the Java Streams framework with the RxJava
 * framework.
 */
public class ex4 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // A test of BigFraction multiplication using an asynchronous
        // Observable stream and a blocking Subscriber implementation.
        AsyncTaskBarrier
            .register(ObservableEx::testFractionMultiplicationsBlockingSubscriber);

        // Test BigFraction multiplications by combining the Java
        // streams framework with the RxJava framework and the common
        // fork-join pool.
        AsyncTaskBarrier
            .register(ObservableEx::testFractionMultiplicationsStreams);

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
