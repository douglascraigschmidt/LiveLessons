package utils;

import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

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
    private static final List<Supplier<Mono<Void>>> sTests =
        new ArrayList<>();

    /**
     * Register the {@code test} test so that it can be run
     * asynchronously.  Each test must take no parameters and return a
     * {@code Supplier<Mono<Void>>} result.
     */
    public static void register(Supplier<Mono<Void>> test) {
        sTests.add(test);
    }

    /**
     * Run all the register tests.
     *
     * @return a {@code Mono<Void>} that will be
     * triggered when all the asynchronously-run tests complete.
     */
    public static Mono<Long> runTests() throws InterruptedException {
        Flux
                // Convert the list into a stream.
                .fromIterable(sTests)

                    // Run each test, which can execute asynchronously.
                .flatMap(Supplier::get)

                // Get the next element.
                .next()

                // Block until we're done.
                .block();

        // Return a count of the number of tests.
        return Mono.just((long) sTests.size());
    }
}

