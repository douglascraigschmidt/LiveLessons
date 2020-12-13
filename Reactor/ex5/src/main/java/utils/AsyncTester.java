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
 * This class asynchronously runs tests that use the Project Reactor
 * framework and ensures that the test driver doesn't exit until all
 * the asynchronous processing is completed.
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
     * @return a {@code Mono<Long>} that will be triggered when all
     * the asynchronously-run tests complete to indicate how many
     * tests were run.
     */
    public static Mono<Long> runTests() throws InterruptedException {
        return Flux
            // Factory method that converts the list into a flux.
            .fromIterable(sTests)

            // Run each test, which can execute asynchronously.
            .flatMap(Supplier::get)

            // Collect into an empty list that triggers when all
            // the tests finish running asynchronously.
            .collectList()

            // Return a mono when we're done.
            .flatMap(l -> Mono.just((long) sTests.size()));
    }
}

