package primechecker.utils;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Customize {@link CompletableFuture} so it uses virtual threads by
 * default rather than the Java common fork-join pool.  However, users
 * of this class can configure the {@link Executor} to whatever they
 * prefer.
 */
public class CompletableFutureEx<T>
       extends CompletableFuture<T> {
    /**
     * Store the default {@link Executor}.
     */
    private static Executor sEXEC = Executors
        .newVirtualThreadPerTaskExecutor();

    /**
     * Set the {@link Executor} that's used as the default.
     *
     * @param executor The {@link Executor} that's used by default
     * @return The previous {@link Executor}
     */
    public static Executor setExecutor(Executor executor) {
        var previousExecutor = sEXEC;
        sEXEC = executor;
        return previousExecutor;
    }

    /**
     * @return Returns the default {@link Executor} used for async
     *         methods that do not specify an {@link Executor}
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
    public <T> CompletableFuture<T> newIncompleteFuture() {
        return new CompletableFutureEx<>();
    }

    /**
     * Returns a new {@link CompletableFutureEx} that is asynchronously
     * completed by a task running with the value obtained by calling
     * the given {@link Supplier} using the default {@link Executor}.
     *
     * @param supplier A {@link Supplier} returning the value to be used
     *                 to complete the returned {@link CompletableFuture}
     * @return The new {@link CompletableFuture}
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return new CompletableFutureEx<T>()
            // Completes this CompletableFuture with the result of the
            // given supplier invoked using the default executor.
            .completeAsync(supplier);
    }

    /**
     * Returns a new {@link CompletableFutureEx} that is asynchronously
     * completed by a task running with the value obtained by calling
     * the given {@link Supplier} using the default {@link Executor}.
     *
     * @param supplier A {@link Supplier} returning the value to be used
     *                 to complete the returned {@link CompletableFuture}
     * @param executor The {@link Executor} to set as the default
     * @return The new {@link CompletableFuture}
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier,
                                                       Executor executor) {
        // Set the default Executor if it's different from the current
        // value.
        if (executor != sEXEC)
            setExecutor(executor);
        return new CompletableFutureEx<T>()
                // Completes this CompletableFuture with the result of the
                // given supplier invoked using the default executor.
                .completeAsync(supplier);
    }
}

