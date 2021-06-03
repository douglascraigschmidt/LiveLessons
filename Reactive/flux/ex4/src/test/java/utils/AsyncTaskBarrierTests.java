package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
    private Mono<Void> throwException() {
        int numerator = 10;
        int denominator = 0;
        return Mono
            // Intentionally trigger an ArithmeticException.
            .just(numerator)
            .doOnNext (value ->
                           display("throwException",
                                   value))
            .map(n -> n /denominator)
            .then();
    }

    /**
     * Show how onErrorResume() without onErrorStop() behaves.
     */
    private Mono<Void> onErrorResume1() {
        int numerator = 10;
        int denominator = 0;
        return Flux
                .just(numerator)
                // Intentionally trigger an ArithmeticException.
                .map(i -> i / denominator)
                // This call to onErrorResume() isn't run since
                // there's a call to onErrorContinue() downstream.
                .onErrorResume(t -> {
                    display("onErrorResume1(): "
                            + t.getMessage()
                            + "\n",
                            0);
                    return Flux.empty();
                })
                .then();
    }

    /**
     * Show how onErrorResume() with onErrorStop() behaves.
     */
    private Mono<Void> onErrorResume2() {
        int numerator = 10;
        int denominator = 0;
        return Flux
                .just(numerator)
                // Intentionally trigger an ArithmeticException.
                .map(i -> i / denominator)
                // This call to onErrorResume() is run since
                // there's a call to onErrorStop() downstream.
                .onErrorResume(t -> {
                    display("onErrorResume2(): "
                            + t.getMessage()
                            + " ",
                            0);
                    return Flux.empty();
                })
                // Ensures that onErrorResume() is actually run!
                .onErrorStop()
                .then();
    }

    /**
     * Complete successfully synchronously.
     */
    private Mono<Void> synchronouslyCompletesSuccessfully() {
        return Mono
            .fromCallable(() -> 10 * 10)
            .doOnNext (value ->
                           display("synchronouslyCompletesSuccessfully",
                                   value))
            .then();
    }

    /**
     * Complete successfully asynchronously.
     */
    private Mono<Void> asynchronouslyCompletesSuccessfully() {
        return Mono
            .fromCallable(() -> 10 * 10)
            .subscribeOn(Schedulers.single())
            .doOnNext (value ->
                       display("asynchronouslyCompletesSuccessfully",
                                value))
            .then();
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
        Supplier<Mono<Void>> throwExceptions = this::throwException;
        Supplier<Mono<Void>> synchronouslyCompletesSuccessfully =
            this::synchronouslyCompletesSuccessfully;
        Supplier<Mono<Void>> onErrorResume1 = this::onErrorResume1;
        Supplier<Mono<Void>> onErrorResume2 = this::onErrorResume2;

        // Register all the local variables that contain method references.
        AsyncTaskBarrier.register(throwExceptions);
        AsyncTaskBarrier.register(synchronouslyCompletesSuccessfully);
        AsyncTaskBarrier.register(onErrorResume1);
        AsyncTaskBarrier.register(onErrorResume2);

        // Directly register a method reference.
        AsyncTaskBarrier.register(this::asynchronouslyCompletesSuccessfully);

        display("testExceptions", 0);
        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .block();

        assertEquals(testCount, 3);

        System.out.println("Completed " + testCount + " tests\n");

        // Remove all but one of the methods being tested.
        assertTrue(AsyncTaskBarrier.unregister(throwExceptions));
        assertTrue(AsyncTaskBarrier.unregister(onErrorResume1));
        assertTrue(AsyncTaskBarrier.unregister(onErrorResume2));
        assertTrue(AsyncTaskBarrier.unregister(synchronouslyCompletesSuccessfully));

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
