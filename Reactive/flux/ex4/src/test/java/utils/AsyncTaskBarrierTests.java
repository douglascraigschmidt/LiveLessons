package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
     * Intentionally trigger an exception in a synchronous chain.
     */
    private Mono<Void> syncThrowException() {
        int numerator = 10;

        // This will cause trouble!
        int denominator = 0;
        return Mono
            // Intentionally trigger an ArithmeticException.
            .just(numerator)
            .doOnNext(value ->
                display("syncThrowException",
                    value))
            .map(n -> n / denominator)
            .then();
    }

    /**
     * Intentionally trigger an exception in an asynchronous chain.
     */
    private Mono<Void> asyncThrowException() {
        int numerator = 10;

        // This will still cause trouble!
        int denominator = 0;

        return Mono
            // Intentionally trigger an ArithmeticException.
            .just(numerator)
            .subscribeOn(Schedulers.single())
            .doOnNext(value ->
                display("asyncThrowException",
                    value))
            .map(n -> n / denominator)
            .then();
    }

    /**
     * Show how onErrorResume() without onErrorStop() behaves.
     */
    private Mono<Void> onErrorResume1() {
        int numerator = 10;

        // Still causing trouble ;-)
        int denominator = 0;

        return Flux
            .just(numerator)

            // Intentionally trigger an ArithmeticException.
            .map(i -> i / denominator)

            // This call to onErrorResume() isn't run since
            // there's a call to onErrorContinue() downstream
            // in the AsyncTaskBarrier framework itself.
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

        // Will generate the ArithmeticException.
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

            // Ensures that onErrorResume() is actually run
            // by overriding the onErrorContinue() operator.
            .onErrorStop()

            .then();
    }

    /**
     * Complete successfully synchronously.
     */
    private Mono<Void> syncNoThrow() {
        return Mono
            .fromCallable(() -> 10 * 10)
            .doOnNext(value ->
                display("syncNoThrow",
                    value))
            .then();
    }

    /**
     * Complete successfully asynchronously.
     */
    private Mono<Void> asyncNoThrow() {
        return Mono
            .fromCallable(() -> 10 * 10)
            .subscribeOn(Schedulers.single())
            .doOnNext(value ->
                display("asyncNoThrow",
                    value))
            .then();
    }

    /**
     * Ensure that the AsyncTaskBarrier methods work properly, even
     * when exceptions occur.
     */
    @Test
    public void testExceptions() {
        // Create local variables so that unregister() works properly.
        Supplier<Mono<Void>> syncThrowExceptions = this::syncThrowException;
        Supplier<Mono<Void>> asyncThrowExceptions = this::asyncThrowException;
        Supplier<Mono<Void>> syncNoThrow = this::syncNoThrow;
        Supplier<Mono<Void>> onErrorResume1 = this::onErrorResume1;
        Supplier<Mono<Void>> onErrorResume2 = this::onErrorResume2;

        // Register all the local variables that contain method references.
        AsyncTaskBarrier.register(syncThrowExceptions);
        AsyncTaskBarrier.register(syncNoThrow);
        AsyncTaskBarrier.register(asyncThrowExceptions);
        AsyncTaskBarrier.register(onErrorResume1);
        AsyncTaskBarrier.register(onErrorResume2);

        // Directly register a method reference.
        AsyncTaskBarrier.register(this::asyncNoThrow);

        long testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .block();

        // Only three tests should complete successfully.
        assertEquals(testCount, 3);

        System.out.println("Completed " + testCount + " tests successfully\n");

        // Remove all but one of the methods being tested.
        assertTrue(AsyncTaskBarrier.unregister(syncThrowExceptions));
        assertTrue(AsyncTaskBarrier.unregister(onErrorResume1));
        assertTrue(AsyncTaskBarrier.unregister(onErrorResume2));
        assertTrue(AsyncTaskBarrier.unregister(syncNoThrow));
        assertTrue(AsyncTaskBarrier.unregister(asyncThrowExceptions));

        testCount = AsyncTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .block();

        // Only one test ran (and completed successfully).
        assertEquals(testCount, 1);

        System.out.println("Completed " + testCount + " test successfully\n");
    }

    /**
     * Display the results.
     *
     * @param calledBy The method that calls {@code display}
     * @param integer  The value to print
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
}
