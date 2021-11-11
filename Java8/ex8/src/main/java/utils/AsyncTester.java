package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static utils.FuturesCollector.toFuture;

/**
 * This class asynchronously runs tests that use the Java completable
 * futures framework and ensures that the test driver doesn't exit
 * until all the asynchronous processing is completed.
 * 
 */
public class AsyncTester {
    /**
     * Keeps track of all the registered tests to run.
     */
    private static final List<Supplier<CompletableFuture<Void>>> sTests =
        new ArrayList<>();

    /**
     * Register the {@code test} test so that it can be run
     * asynchronously.  Each test must take no parameters and return a
     * {@code Supplier<CompletableFuture<Void>>} result.
     */
    public static void register(Supplier<CompletableFuture<Void>> test) {
        sTests.add(test);
    }

    /**
     * Run all the register tests.
     *
     * @return a {@code CompletableFuture<Void>} that will be
     * triggered when all the asynchronously-run tests complete.
     */
    public static CompletableFuture<Void> runTests() {
        return sTests
            // Convert the list into a stream.
            .stream()

            // Run each test, which can execute asynchronously.
            .map(Supplier::get)

            // Trigger intermediate operation processing and return a
            // single future that will be triggered when all the tests
            // are complete.
            .collect(toFuture())

            // Print the number of completed tests when all the tests
            // complete.
            .thenAccept(list ->
                        System.out.println("Completed "
                                           + list.size()
                                           + " tests"));
    }
}

