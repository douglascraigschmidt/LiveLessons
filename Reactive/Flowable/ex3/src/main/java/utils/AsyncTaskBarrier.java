package utils;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Supplier;

import java.util.ArrayList;
import java.util.List;

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
     * @return a {@code Single<Long>} that will be triggered when all
     * the asynchronously-run tasks complete.
     */
    public static Single<Long> runTasks() {
        return Observable
            // Factory method that converts the list into an
            // Observable.
            .fromIterable(sTasks)

            // Run each task, which can execute asynchronously.
            .map(Supplier::get)

            // Map each element of the Observable into
            // CompletableSources, subscribe to them, and waits until
            // the upstream and all CompletableSources complete.
            .flatMapCompletable(c -> c)

            // Convert the returned Completable into a Single that
            // returns the number of tasks when it completes.
            .toSingleDefault((long) sTasks.size());
    }
}

