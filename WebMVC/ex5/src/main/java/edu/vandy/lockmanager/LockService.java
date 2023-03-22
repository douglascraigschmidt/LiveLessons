package edu.vandy.lockmanager;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static edu.vandy.lockmanager.Utils.log;

/**
 * This Spring {@code Service} implements the {@link LockController}
 * endpoint handler methods.
 */
@Service
public class LockService {
    /**
     * Create a new {@code VirtualThreadPerTaskExecutor}.
     */
    ExecutorService mExecutor = Executors
        .newVirtualThreadPerTaskExecutor();

    /**
     * An ArrayBlockingQueue that limits concurrent access to the
     * fixed number of available locks managed by the
     * {@link LockService}.
     */
    private ArrayBlockingQueue<Lock> mAvailableLocks;

    /**
     * Initialize the {@link Lock} manager.
     *
     * @param permitCount The number of {@link Lock} objects to
     *                  manage.
     */
    public void create(Integer permitCount) {
        mAvailableLocks =
            // Make an ArrayBlockQueue with "fair" semantics.
            new ArrayBlockingQueue<>(permitCount,
                                     true);

        // Add each Lock to the queue.
        mAvailableLocks.addAll(makeLocks(permitCount));
    }

    /**
     * Create the requested number of {@link Lock} objects.
     *
     * @param permitCount The number of {@link Lock} objects to create
     */
    private List<Lock> makeLocks(int permitCount) {
        return IntStream
            // Iterate from 0 to count - 1.
            .range(0, permitCount)

            // Convert Integer to String.
            .mapToObj(Integer::toString)

            // Create a new Lock.
            .map(Lock::new)

            // Convert the Stream to a List.
            .toList();
    }

    /**
     * Acquire a {@link Lock}, blocking until one is available.
     *
     * @return A {@link DeferredResult<Lock>}
     */
    public DeferredResult<Lock> acquire() {
        log("LockService.acquire()");

        // Create a DeferredResult containing the List of Lock
        // objects.
        DeferredResult<Lock> deferredResult =
            new DeferredResult<>();

        try {
            // Run the computation off the Servlet thread.
            mExecutor
                .submit(() -> {
                        log("LockService - requesting a Lock");

                        try {
                            var lock = mAvailableLocks
                                .poll();
                    
                            if (lock == null) {
                                log("LockService -- blocking for lock acquire");

                                // Block until a Lock is available.
                                lock = mAvailableLocks.take();
                            } else
                                log("LockService -- non-blocking lock acquire");

                            // Set the result to the acquired Lock.
                            deferredResult
                                .setResult(lock);

                            log("LockService - returning Lock "
                                + lock);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (Exception exception) {
            log("Catch exception "
                + exception.getMessage());
            deferredResult
                .setResult(new Lock(exception.getMessage()));
        }
        log("returning deferredResult");

        // Return the deferredResult before the lock
        // are obtained.
        return deferredResult;
    }

    /**
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param permits The number of permits to acquire
     * @return A {@link DeferredResult<List>} containing {@code
     *         permits} number of acquired {@link Lock} objects
     */
    public DeferredResult<List<Lock>> acquire(int permits) {
        log("LockService.acquire(permits)");

        // Create a DeferredResult containing the List of Lock
        // objects.
        DeferredResult<List<Lock>> deferredResult =
            new DeferredResult<>();

        try {
            // Run the computation off the Servlet thread.
            mExecutor
                .submit(() -> {
                        var locks = IntStream
                            // Iterate 'permit' times.
                            .range(0, permits)

                            // Acquire a Lock each iteration.
                            .mapToObj(___ -> {
                                    log("LockService -- blocking for lock acquire");

                                    Lock lock = null;

                                    try {
                                        // Block until a Lock is available.
                                        lock = mAvailableLocks.take();
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                    log("LockService - obtained Lock "
                                        + lock);
                                
                                    // Return the Lock.
                                    return lock;
                                })

                            // Convert Stream to a List.
                            .toList();

                        log("LockService - collected the locks "
                            + locks);

                        // Set the deferredResult to the acquired List
                        // of Lock objects.
                        deferredResult
                            .setResult(locks);

                        log("LockService - returning Locks "
                            + locks);
                    });
        } catch (Exception exception) {
            log("Catch exception "
                + exception.getMessage());
            // Return an error message.
            var lock = new Lock(exception.getMessage());

            deferredResult
                .setResult(List.of(lock));
        }
        log("returning deferredResult");

        // Return the deferredResult before the locks
        // are obtained.
        return deferredResult;
    }

    /**
     * Release the {@link Lock} so other Beings can acquire it.
     *
     * @param Lock The {@link Lock} to release
     */
    public void release(Lock Lock) {
        log("LockService.release()");
        try {
            // Put the Lock parameter back to the queue.
            mAvailableLocks.put(Lock);
            log("releasing " + Lock);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Release the {@code locks}.
     *
     * @param locks A {@link List} that contains {@link Lock}
     *              objects to release
     */
    public void release(List<Lock> locks) {
        log("LockService.release(locks)");
        locks
            // Put each lock back in the queue.
            .forEach(lock -> {
                    try {
                        // Put the lock back into the queue.
                        mAvailableLocks.put(lock);
                        log("releasing " + lock);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }
}
