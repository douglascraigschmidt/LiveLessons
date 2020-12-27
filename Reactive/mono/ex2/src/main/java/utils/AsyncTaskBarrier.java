package utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class asynchronously runs tasks that use the Project Reactor
 * framework and ensures that the calling method doesn't exit until
 * all asynchronous task processing is completed.
 * 
 */
public class AsyncTaskBarrier {
    /**
     * Keeps track of all the registered tasks to run.
     */
    private static final List<Supplier<Mono<Void>>> sTasks =
        new ArrayList<>();

    /**
     * Register the {@code task} task so that it can be run
     * asynchronously.  Each task must take no parameters and return a
     * {@code Supplier<Mono<Void>>} result.
     */
    public static void register(Supplier<Mono<Void>> task) {
        sTasks.add(task);
    }

    /**
     * Run all the register tasks.
     *
     * @return a {@code Mono<Long>} that will be triggered when all
     * the asynchronously-run tasks complete to indicate how many
     * tasks were run.
     */
    public static Mono<Long> runTasks() throws InterruptedException {
        return Flux
            // Factory method that converts the list into a flux.
            .fromIterable(sTasks)

            // Run each task, which can execute asynchronously.
            .flatMap(Supplier::get)

            // Collect into an empty list that triggers when all
            // the tasks finish running asynchronously.
            .collectList()

            // Return a mono containing the number of tasks run when
            // we're done.
            .flatMap(l -> Mono
                     // Use just() to return the number of tasks run.
                     .just((long) sTasks.size()));
    }
}

