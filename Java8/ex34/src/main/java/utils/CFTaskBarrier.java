package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * This class runs (a)synchronous tasks using the Java completable
 * futures framework.  It can also be used to ensure the calling
 * method doesn't exit until all (a)synchronous task processing
 * completes.
 *
 * <p><b>Sample Usage.</b> The following code snippet illustrates a
 * common idiom for using the {@code CFTaskBarrier} class.<br>
 *
 * <pre>{@code
 *   // Register asynchronous tasks to test.
 *   CFTaskBarrier.register(FluxEx::testFractionMultiplicationsAsync1);
 *   CFTaskBarrier.register(FluxEx::testFractionMultiplicationsAsync2);
 *
 *   long testCount = CFTaskBarrier
 *     // Run all the tasks.
 *     .runTasks()
 *
 *     // Block until all asynchronous processing is done.
 *     .block();
 *
 *   // Print number of tests run.
 *   System.out.println("Completed " + testCount + " tests");
 * }}</pre>
 */
public class CFTaskBarrier {
    /**
     * Keeps track of all the registered tasks to run.
     */
    private static final List<Supplier<CompletableFuture<Void>>> sTasks =
        new ArrayList<>();

    /**
     * Register the {@code task} so it runs (a)synchronously when
     * {@code runTasks()} is called.  Each task takes no parameters and
     * returns a {@code Mono<Void>} result when its {@code Supplier.get()}
     * method is called.
     *
     * @param task The task to register with {@code AsyncTaskBarrier}
     */
    public static void register(Supplier<CompletableFuture<Void>> task) {
        // Appends the task to the list.
        sTasks.add(task);
    }

    /**
     * Unregister the {@code task} task so that it is no longer run
     * when {@code runTasks()} is called.  This {@code task} should
     * previously be registered via the {@code register()} method.
     *
     * @param task The task to unregister with {@code AsyncTaskBarrier}
     * @return True if {@code task} was previously registered, else false.
     */
    public static boolean unregister(Supplier<CompletableFuture<Void>> task) {
        return sTasks.remove(task);
    }

    /**
     * Run all the registered tasks.
     *
     * @return a {@code Completable<Long>} that will be triggered when
     * all the (a)synchronously-run tasks complete to indicate how
     * many tasks were run.
     */
    public static CompletableFuture<Integer> runTasks() {
        // Copy into a local final variable to ensure visibility when
        // used in thenApply() below.
        final int taskListSize = sTasks.size();

        @SuppressWarnings("unchecked") CompletableFuture<Void>[] taskF = sTasks
            // Factory method that converts the list into a stream.
            .stream()

            // Run each task, which can execute (a)synchronously.
            .map(Supplier::get)

            // Collect into an array;
            .toArray(CompletableFuture[]::new);

        return CompletableFuture
            .allOf(taskF)
            .thenApply(v -> taskListSize);
    }
}
