import utils.AsyncTaskBarrier;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously and concurrently using many RxJava Observable
 * operations, including fromArray(), map(), generate(), take(),
 * flatMap(), fromCallable(), filter(), reduce(), collectInto(),
 * subscribeOn(), onErrorReturn(), and Schedulers.computation().  It
 * also shows RxJava Single and Maybe operations, including
 * fromCallable(), flatMapCompletable(), ambArray(), subscribeOn(),
 * ignoreElement(), and doOnSuccess().
 */
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Use an asynchronous Observable stream and a pool of threads
        // to perform BigFraction multiplications and additions.
        AsyncTaskBarrier.register(ObservableEx::testFractionMultiplications);

        // Use an asynchronous Observable stream and a pool of threads
        // to showcase exception handling of BigFraction objects.
        AsyncTaskBarrier.register(ObservableEx::testFractionExceptions);

        // Use an asynchronous Observable stream and a pool of threads
        // to perform BigFraction reductions and multiplications.
        AsyncTaskBarrier.register(ObservableEx::testFractionReductionMultiplications);

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
