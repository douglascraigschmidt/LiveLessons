package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
/**
 * Unit tests for the CFTaskBarrier class.
 */
public class CFTaskBarrierTests {
    /**
     * Intentionally trigger an exception in an asynchronous chain.
     */
    private CompletableFuture<Void> asyncThrowException() {
        int numerator = 10;
        int denominator = 0;
        return CompletableFuture
            // Intentionally trigger an ArithmeticException.
            .supplyAsync(() -> numerator / denominator)
            .thenAccept(value -> display("asyncThrowException",
                                         value));
    }

    /**
     * Complete successfully asynchronously.
     */
    private CompletableFuture<Void> asyncNoThrow() {
        return CompletableFuture
            .supplyAsync(() -> 10 * 10)
            .thenAccept(value -> display("asyncNoThrow",
                                         value));
    }

    /**
     * Ensure that the CFTaskBarrier methods work properly, even
     * when exceptions occur.
     */
    @Test
    public void testExceptions() {
        // Create local variables so that unregister() works properly.
        Supplier<CompletableFuture<Void>> asyncThrowExceptions = this::asyncThrowException;

        // Register all the local variables that contain method references.
        CFTaskBarrier.register(asyncThrowExceptions);

        // Directly register a method reference.
        CFTaskBarrier.register(this::asyncNoThrow);

        int testCount = CFTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .join();

        // Only one tests should complete successfully.
        assertEquals(testCount, 1);

        System.out.println("Completed " + testCount + " tests successfully\n");

        // Remove all but one of the methods being tested.
        assertTrue(CFTaskBarrier.unregister(asyncThrowExceptions));

        testCount = CFTaskBarrier
            // Run all the tests.
            .runTasks()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .join();

        // Only one test ran (and completed successfully).
        assertEquals(testCount, 1);

        System.out.println("Completed " + testCount + " test successfully\n");
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
}
