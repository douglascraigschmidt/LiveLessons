package utils;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class asynchronously runs tasks that use the RxJava framework
 * and ensures that the calling method doesn't exit until all
 * asynchronous task processing is completed.
 * 
 */
public class AsyncTaskBarrier {
    /**
     * Keeps track of all the registered tasks to run.
     */
    private static final List<Supplier<Completable>> sTasks =
        new ArrayList<>();

    /**
     * Register the {@code task} task so that it can be run
     * asynchronously.  Each task must take no parameters and return a
     * {@code Supplier<Completable>} result.
     *
     * @param task The task to register with the {@code AsyncTasker} framework.
     */
    public static void register(Supplier<Completable> task) {
        sTasks.add(task);
    }

    /**
     * Unregister the {@code task} task so that it is no longer run
     * asynchronously when {@code runTasks()} is called.  Each task
     * must take no parameters and return a {@code
     * Supplier<Completable>} result.
     * 
     * @param task The task to unregister with {@code AsyncTaskBarrier}
     * @return True if {@code task} was previously registered, else false.
     */
    public static boolean unregister(Supplier<Completable> task) {
        return sTasks.remove(task);
    }

    /**
     * Run all the register tasks.
     *
     * @return a {@code Single<Long>} that is triggered when all the
     * tasks complete and emits the number of tasks that completed
     * successfully
     */
    public static Single<Long> runTasks() {
        // Keep track of how many exceptions occurred.
        AtomicLong exceptionCount = new AtomicLong(0);

        // Handle task exceptions by recording and swallowing them.
        Function<Throwable, Completable> errorHandler = t -> {
            // Increment the count of exceptions.
            exceptionCount.getAndIncrement();

            // Just return a completed Completable.
            return Completable.complete();
        };

        return Observable
            // Convert the List into an Observable.
            .fromIterable(sTasks)

            // Run each task, which can execute asynchronously or
            // synchronously.
            .map(s -> 
                 // Run the task (swallow any exception after first
                 // recording it).
                 s.get().onErrorResumeNext(errorHandler))

            // Maps each Observable element into a CompletableSource,
            // subscribe to them, waits until the upstream and all
            // CompletableSources complete, and then returns a single
            // Completable.
            .flatMapCompletable(c -> c)

            // Convert the Completable into a Single that returns the
            // number of successful tasks when it completes.
            .toSingle(() -> sTasks.size() - exceptionCount.get());
    }
}

