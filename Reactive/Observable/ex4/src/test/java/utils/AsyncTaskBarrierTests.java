package utils;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ConstantConditions")
/**
 * Unit tests for the AsyncTaskBarrier class.
 */
public class AsyncTaskBarrierTests {
    /**
     * Intentionally trigger an exception.
     */
    private Completable throwException() {
        int numerator = 10;
        int denominator = 0;
        return Single
            // Intentionally trigger an ArithmeticException.
            .just(numerator)
            .map(n -> n / denominator)
            .doOnError (t ->
                        display("throwException failed with "
                                + t.getMessage(),
                                denominator))
            .ignoreElement();
    }

    /**
     * Complete successfully synchronously.
     */
    private Completable synchronouslyCompletesSuccessfully() {
        return Single
            .fromCallable(() -> 10 * 10)
            .doOnSuccess (value ->
                          display("synchronouslyCompletesSuccessfully",
                                  value))
            .ignoreElement();
    }

    /**
     * Complete successfully asynchronously.
     */
    private Completable asynchronouslyCompletesSuccessfully() {
        return Single
            .fromCallable(() -> 10 * 10)
            .subscribeOn(Schedulers.single())
            .doOnSuccess(value ->
                         display("asynchronouslyCompletesSuccessfully",
                                 value))
            .ignoreElement();
    }

    /**
     * Display the results.
     *
     * @param calledBy The method that calls {@code display}
     * @param integer The value to print
     */
    private void display(String calledBy,
                         Integer integer) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + calledBy
                           + " "
                           + integer);
    }

    /**
     * Ensure that the AsyncTaskBarrier methods work properly, even
     * when exceptions occur.
     */
    @Test
    public void testExceptions() {
        // Create local variables so that unregister() works properly.
        Supplier<Completable> throwExceptions = this::throwException;
        Supplier<Completable> synchronouslyCompletesSuccessfully =
            this::synchronouslyCompletesSuccessfully;

        // Register all the local variables that contain method references.
        AsyncTaskBarrier.register(throwExceptions);
        AsyncTaskBarrier.register(synchronouslyCompletesSuccessfully);

        // Directly register a method reference.
        AsyncTaskBarrier.register(this::asynchronouslyCompletesSuccessfully);

        // display("testExceptions", 0);
        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .blockingGet();

        // Only two tests completed successfully.
        assertEquals(testCount, 2);

        System.out.println("Completed " + testCount + " tests successfully\n");

        // Remove all but one of the methods being tested.
        assertTrue(AsyncTaskBarrier.unregister(throwExceptions));
        assertTrue(AsyncTaskBarrier.unregister(synchronouslyCompletesSuccessfully));

        testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .blockingGet();

        // Only one test ran (and completed successfully).
        assertEquals(testCount, 1);

        System.out.println("Completed " + testCount + " test successfully\n");
    }
}
