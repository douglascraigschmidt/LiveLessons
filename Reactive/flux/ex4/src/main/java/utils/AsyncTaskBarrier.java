package utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
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
 *   // Register asynchronous tasks to test.
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
 *   // Print number of tests run.
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
     * {@code runTasks()} is called.  Each task takes no parameters and
     * returns a {@code Mono<Void>} result when its {@code Supplier.get()}
     * method is called.
     *
     * @param task The task to register with {@code AsyncTaskBarrier}
     */
    public static void register(Supplier<Mono<Void>> task) {
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
    public static boolean unregister(Supplier<Mono<Void>> task) {
        return sTasks.remove(task);
    }

    /**
     * Run all the registered tasks.  This method uses {@code
     * onErrorContinue()} internally, so tasks registered to use this
     * method must use {@code onErrorStop()} in conjunction with
     * {@code onErrorResume()} to avoid problems with {@code
     * onErrorContinue()} overriding the behavior of {@code
     * onErrorResume()}, which is discussed further at the URL
     * devdojo.com/ketonemaniac/reactor-onerrorcontinue-vs-onerrorresume.
     *
     * @return a {@code Mono<Long>} that will be triggered when all
     * the (a)synchronously-run tasks complete to indicate how many
     * tasks were run.
     */
    public static Mono<Long> runTasks() {
        // Needed to keep track of how many exceptions occurred.
        AtomicLong exceptionCount = new AtomicLong(0);

        // Log the exception and increment the exceptionCount.
        BiConsumer<Throwable,
                   Object> errorHandler = (t, i) -> {
            // Record the exception message.
            System.out.println("exception = "
                                   + t.getMessage()
                                   + "\n");
            // Increment the count of exceptions.
            exceptionCount.getAndIncrement();
        };

        // Copy into a local final variable to ensure visibility
        // when used in Mono.just() below.
        final int taskListSize = sTasks.size();

        return Flux
            // Factory method that converts the list into a flux.
            .fromIterable(sTasks)

            // Run each task, which can execute (a)synchronously.
            .flatMap(Supplier::get)

            // Log the exception and continue processing.  The use of
            // this method has implications for tasks registered with
            // the AsyncTaskBarrier, which must use onErrorStop() in
            // conjunction with onErrorResume().
            .onErrorContinue(errorHandler)

            // Collect into an empty list that triggers when all the
            // tasks finish running (a)synchronously.
            .collectList()

            // Return a mono containing the number of tasks that
            // completed successfully (i.e., without exceptions).
            .flatMap(f -> Mono
                    .just(taskListSize - exceptionCount.get()));
    }
}
