package edu.vandy.lockmanager;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
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
     * An ArrayBlockingQueue that limits concurrent access to the
     * fixed number of available locks managed by the
     * {@link LockService}.
     */
    private ArrayBlockingQueue<Lock> mAvailableLocks;

    /**
     * Initialize the {@link Lock} manager.
     *
     * @param lockCount The number of {@link Lock} objects to
     *                  manage.
     */
    public void create(Integer lockCount) {
        mAvailableLocks =
            new ArrayBlockingQueue<>(lockCount,
                                     true);

        // Add each Lock to the queue.
        mAvailableLocks.addAll(makeLocks(lockCount));
    }

    /**
     * Create the requested number of {@link Lock} objects.
     *
     * @param count The number of {@link Lock} objects to create
     */
    private List<Lock> makeLocks(int count) {
        return IntStream
            // Iterate from 0 to count - 1.
            .range(0, count)

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

        // Create a new VirtualThreadPerTaskExecutor().
        var executor = Executors
            .newVirtualThreadPerTaskExecutor();

        try {
            // Run the computation off the Servlet thread.
            executor
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

        // Create a new VirtualThreadPerTaskExecutor().
        var executor = Executors
            .newVirtualThreadPerTaskExecutor();

        try {
            // Run the computation off the Servlet thread.
            executor
                .submit(() -> {
                        var locks = IntStream
                            // Iterate 'permit' times.
                            .range(0, permits)

                            // Acquire a Lock each iteration.
                            .mapToObj(___ -> {
                                    log("LockService - requesting a Lock");

                                    log("LockService -- blocking for lock acquire");

                                    // Block until a Lock is available.
                                    Lock lock = null;

                                    try {
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
            var lock = new Lock(exception.getMessage());
            deferredResult
                .setResult(List.of(lock));
        }
        log("returning deferredResult");
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
            // Add the Lock parameter back to the queue.
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
            .forEach(lock -> {
                    try {
                        // Put the Lock parameter back into the queue.
                        mAvailableLocks.put(lock);
                        log("releasing " + lock);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }
}
