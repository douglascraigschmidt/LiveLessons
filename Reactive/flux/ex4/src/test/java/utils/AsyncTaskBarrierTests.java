package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
/**
 * Unit tests for the AsyncTaskBarrier class.
 */
public class AsyncTaskBarrierTests {
    /**
     * Intentionally trigger an exception.
     */
    private Mono<Void> throwExceptions() {
        int numerator = 10;
        int denominator = 0;
        return Mono
            // Intentionally trigger an ArithmeticException.
            .just(numerator / denominator)
            .then();
    }

    /**
     * Complete successfully synchronously.
     */
    private Mono<Void> synchronouslyCompletesSuccessfully() {
        return Mono.empty();
    }

    /**
     * Complete successfully asynchronously.
     */
    private Mono<Void> asynchronouslyCompletesSuccessfully() {
        return Mono
            .fromCallable(() -> 10)
            .subscribeOn(Schedulers.single())
            .then();
    }

    /**
     * Ensure that the AsyncTaskBarrier methods work properly, even
     * when exceptions occur.
     */
    @Test
    public void testExceptions() {
        Supplier<Mono<Void>> throwExceptions = this::throwExceptions;
        AsyncTaskBarrier.register(throwExceptions);

        Supplier<Mono<Void>> synchronouslyCompletesSuccessfully =
            this::synchronouslyCompletesSuccessfully;

        AsyncTaskBarrier.register(synchronouslyCompletesSuccessfully);
        AsyncTaskBarrier.register(this::asynchronouslyCompletesSuccessfully);

        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .block();

        assertEquals(testCount, 2);

        System.out.println("Completed " + testCount + " tests\n");

        // Remove several methods.
        assertTrue(AsyncTaskBarrier.unregister(throwExceptions));
        assertTrue(AsyncTaskBarrier
                       .unregister(synchronouslyCompletesSuccessfully));

        testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .block();

        assertEquals(testCount, 1);

        System.out.println("Completed " + testCount + " tests\n");
    }
}
