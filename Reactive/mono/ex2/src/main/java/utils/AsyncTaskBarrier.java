package utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class runs (a)synchronous tasks using the Project Reactor
 * framework.  It can also be used to ensure the calling method
 * doesn't exit until all (a)synchronous task processing completes.
 *
 * <p><b>Sample Usage.</b> The following code snippet illustrates a
 * common idiom for using the {@code AsyncTaskBarrier} class.<br>
 *
 * <pre>{@code
 *   // Register asynchronous tasks under test.
 *   AsyncTaskBarrier.register(FluxEx::testFractionMultiplicationsAsync1);
 *   AsyncTaskBarrier.register(FluxEx::testFractionMultiplicationsAsync2);
 *
 *   long testCount = AsyncTaskBarrier
 *     // Run all the tasks.
 *     .runTasks()
 * 
 *     // Block until all asynchronous processing is done.
 *     .block();
 *
 *   System.out.println("Completed " + testCount + " tests");
 * }}</pre>
 */
public class AsyncTaskBarrier {
    /**
     * Keeps track of all the registered tasks to run.
     */
    private static final List<Supplier<Mono<Void>>> sTasks =
        new ArrayList<>();

    /**
     * Register the {@code task} so it runs (a)synchronously when
     * {@code runTasks()} is called.  Each task takes no parameters
     * and returns a {@code Supplier<Mono<Void>>} result.
     * 
     * @param task The task to register with {@code AsyncTaskBarrier}
     */
    public static void register(Supplier<Mono<Void>> task) {
        // Appends the task to the list.
        sTasks.add(task);
    }

    /**
     * Unregister the {@code task} task so that it is no longer run
     * when {@code runTasks()} is called.  Each task must take no
     * parameters and return a {@code Supplier<Mono<Void>>} result.
     * 
     * @param task The task to unregister with {@code AsyncTaskBarrier}
     * @return True if {@code task} was previously registered, else false.
     */
    public static boolean unregister(Supplier<Mono<Void>> task) {
        return sTasks.remove(task);
    }

    /**
     * Run all the register tasks.
     *
     * @return a {@code Mono<Long>} that will be triggered when all
     * the (a)synchronously-run tasks complete to indicate how many
     * tasks were run.
     */
    public static Mono<Long> runTasks() {
        return Flux
            // Factory method that converts the list into a flux.
            .fromIterable(sTasks)

            // Run each task, which can execute (a)synchronously.
            .flatMap(Supplier::get)

            // Collect into an empty list that triggers when all
            // the tasks finish running (a)synchronously.
            .collectList()

            // Return a mono containing the number of tasks run when
            // they all complete.
            .flatMap(l -> Mono
                     // Use just() to return the number of tasks run.
                     .just((long) sTasks.size()));
    }
}
