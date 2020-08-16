package utils;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Supplier;

import java.util.ArrayList;
import java.util.List;

/**
 * This class asynchronously runs tests that use the RxJava framework
 * and ensures that the test driver doesn't exit until all the
 * asynchronous processing is completed.
 * 
 */
public class AsyncTester {
    /**
     * Keeps track of all the registered tests to run.
     */
    private static final List<Supplier<Completable>> sTests =
        new ArrayList<>();

    /**
     * Register the {@code test} test so that it can be run
     * asynchronously.  Each test must take no parameters and return a
     * {@code Supplier<Completable>} result.
     */
    public static void register(Supplier<Completable> test) {
        sTests.add(test);
    }

    /**
     * Run all the register tests.
     *
     * @return a {@code Single<Long>} that will be triggered when all
     * the asynchronously-run tests complete.
     */
    public static Single<Long> runTests() {
        return Observable
            // Factory method that converts the list into an
            // Observable.
            .fromIterable(sTests)

            // Run each test, which can execute asynchronously.
            .map(Supplier::get)

            // Map each element of the Observable into
            // CompletableSources, subscribes to them, and waits until
            // the upstream and all CompletableSources complete.
            .flatMapCompletable(c -> c)

            // Convert the returned Completable into a Single that
            // returns the number of tests when it completes.
            .toSingleDefault((long) sTests.size());
    }
}

