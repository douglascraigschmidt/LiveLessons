import utils.AsyncTaskBarrier;

/**
 * This example shows how to reduce and/or multiply big fractions
 * asynchronously using the RxJava flatMap() concurrency idiom and
 * many RxJava {@link Observable} operators, including fromArray(),
 * map(), generate(), take(), flatMap(), fromCallable(), filter(),
 * reduce(), collectInto(), subscribeOn(), onErrorReturn(),
 * onErrorResumeNext(), and Schedulers.computation().  It also shows
 * how these operators can be used together with RxJava {@link Single}
 * and {@link Maybe} operators, including fromCallable(), ambArray(),
 * flatMapCompletable(), subscribeOn(), ignoreElement(), and
 * doOnSuccess().
 */
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Test Observable exception handling via onErrorReturn().
        AsyncTaskBarrier.register(ObservableEx::testFractionException1);

        // Test Observable exception handling via onErrorResumeNext().
        AsyncTaskBarrier.register(ObservableEx::testFractionException2);

        // Use an asynchronous Observable stream and a pool of threads
        // to perform BigFraction multiplications and additions.
        AsyncTaskBarrier.register(ObservableEx::testFractionMultiplications);

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
