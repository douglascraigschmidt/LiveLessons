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
     * The total number of Locks.
     */
    private int mLockCount;

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
        mLockCount = lockCount;

        mAvailableLocks =
            new ArrayBlockingQueue<>(lockCount,
                true);

        // Add each Locki to the queue.
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
     * @return A {@link Lock} that can be gazed into
     */
    public DeferredResult<Lock> acquire() {
        log("LockService.acquire()");

        DeferredResult<Lock> deferredResult =
            new DeferredResult<>();

        var executor = Executors
            .newVirtualThreadPerTaskExecutor();
        try {
            executor.submit(() -> {
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

                    // Sleep for 2 seconds.
                    Thread.sleep(2000);

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
}
