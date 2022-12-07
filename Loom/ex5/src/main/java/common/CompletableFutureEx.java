package common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Create a subclass of {@link CompletableFuture} that uses
 * virtual threads by default rather than the common fork-
 * join pool.
 */
public class CompletableFutureEx<T>
       extends CompletableFuture<T> {
    private static Executor sEXEC = Executors
        .newVirtualThreadPerTaskExecutor();

    public static Executor setExecutor(Executor executor) {
        var previousExecutor = sEXEC;
        sEXEC = executor;
        return previousExecutor;
    }

    /**
     * @return An {@link Executors#newVirtualThreadPerTaskExecutor}
     *         that creates a new virtual thread per task
     */
    @Override
    public Executor defaultExecutor() {
        return sEXEC;
    }

    /**
     * @return This virtual constructor returns a new incomplete
     *         {@link CompletableFutureEx} that will be returned by a
     *         {@link CompletionStage} method
     */
    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
        return new CompletableFutureEx<>();
    }

    /**
     * Returns a new {@link CompletableFutureEx} that is
     * asynchronously completed by a task running as a virtual thread
     * with the value obtained by calling the given {@link Supplier}.
     *
     * @param supplier A function returning the value to be used to
     *                 complete the returned {@link CompletableFuture}
     * @return The new {@link CompletableFuture}
     */
    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return new CompletableFutureEx<U>()
            // Completes this CompletableFuture with the result of the
            // given supplier invoked from a virtual thread using the
            // default executor.
            .completeAsync(supplier);
    }
}

