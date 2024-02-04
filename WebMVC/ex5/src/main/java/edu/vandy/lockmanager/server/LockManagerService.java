package edu.vandy.lockmanager.server;

import edu.vandy.lockmanager.common.Callback;
import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.common.LockManager;
import edu.vandy.lockmanager.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static edu.vandy.lockmanager.utils.Utils.log;

/**
 * This Spring {@code Service} implements the {@link
 * LockManagerController} endpoint handler methods using Spring WebMVC
 * conventional Java types and a {@link Map} of {@link LockManager}
 * objects associated with the {@link ArrayBlockingQueue} objects that
 * store the state of each semaphore.
 */
@Service
public class LockManagerService {
    /**
     * Obtain the underlying {@link AsyncTaskExecutor} created by
     * Spring, which defaults to {@link SimpleAsyncTaskExecutor}.
     */
    @Autowired
    private AsyncTaskExecutor mExecutor;

    /**
     * A {@link Map} that associates {@link LockManager} objects with
     * the {@link ArrayBlockingQueue} that stores the state of the
     * semaphore.
     */
    private final Map<LockManager,
                      ArrayBlockingQueue<Lock>> mLockManagerMap =
        new ConcurrentHashMap<>();

    /**
     * Initialize the {@link Lock} manager.
     *
     * @param permitCount The number of {@link Lock} objects to
     *                    manage
     * @return A {@link LockManager} that uniquely identifies this
     *         semaphore
     */
    public LockManager create(Integer permitCount) {
        var availableLocks =
            // Make an ArrayBlockQueue with "fair" semantics that
            // limits concurrent access to the fixed number of
            // available locks.
            new ArrayBlockingQueue<Lock>(permitCount,
                                         true);

        // Add each Lock to the queue.
        availableLocks.addAll(makeLocks(permitCount));

        // Create a new LockManager with a unique name.
        var lockManager =
            new LockManager(Utils.generateUniqueId(),
                            permitCount);

        // Insert the new LockManager and the ArrayBlockingQueue into
        // the Map.
        mLockManagerMap.put(lockManager,
                            availableLocks);

        log("LockService.create("
            + permitCount
            + ") "
            + " - made "
            + lockManager
            + " with locks = "
            + availableLocks);

        // Return the new LockManager.
        return lockManager;
    }

    /**
     * Create the requested number of {@link Lock} objects.
     *
     * @param permitCount The number of {@link Lock} objects to create
     */
    private List<Lock> makeLocks(int permitCount) {
        return IntStream
            // Iterate from 0 to permitCount - 1.
            .range(0, permitCount)

            // Convert Integer to String for use as the Lock id.
            .mapToObj(Integer::toString)

            // Create a new Lock with a unique Lock id.
            .map(Lock::new)

            // Convert the Stream to a List.
            .toList();
    }

    /**
     * Acquire a {@link Lock}, blocking until one is available.  Since
     * this method is marked as {@code Async} Spring will run it in a
     * background thread.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param callback The {@link Callback} object that defines
     *                 methods for handling the completion or failure
     *                 of the asynchronous acquire operation
     */
    @Async
    public void acquire(LockManager lockManager,
                        Callback callback) {
        log("LockService.acquire() on " + lockManager);

        // Try to get the locks associated with the lockManager.
        var availableLocks =
            mLockManagerMap.get(lockManager);

        // Check for and handle errors.
        if (availableLocks == null)
            callback.onError(new IllegalArgumentException
                             (lockManager.name));
        else {
            try {
                // Try to acquire a lock without blocking, but block if that
                // doesn't work
                tryAcquire(callback, availableLocks);
            } catch (Exception e) {
                // Set the error.
                callback.onError(e);
            }
        }
    }

    /**
     * Try to acquire a lock without blocking, but block if that doesn't work.
     *
     * @param callback The {@link Callback} object that defines
     *                 methods for handling the completion or failure
     *                 of the asynchronous acquire operation
     * @param availableLocks The locks associated with the {@link LockManager}
     */
    private void tryAcquire(Callback callback,
                            ArrayBlockingQueue<Lock> availableLocks)
        throws InterruptedException {
        log("LockService - requesting a Lock");

        // Try to acquire a Lock without blocking.
        var lock = availableLocks.poll();

        // Check for and handle errors.
        if (lock != null)
            log("LockService -- non-blocking lock acquire");
        else {
            log("LockService -- blocking for lock acquire");

            // Block until a Lock is available.
            lock = availableLocks.take();
        }

        // Set the result to the acquired Lock.
        callback.onSuccess(lock);

        log("LockService - returning Lock["
            + lock
            + "]");
    }

    /**
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param permits The number of permits to acquire
     * @return A {@link DeferredResult<List>} containing {@code
     *         permits} number of acquired {@link Lock} objects
     */
    public DeferredResult<List<Lock>> acquire(LockManager lockManager,
                                              int permits) {
        log("LockService.acquire("
            + permits
            + ")");

        // Create a DeferredResult containing the List of Lock
        // objects.
        DeferredResult<List<Lock>> deferredResult = new DeferredResult<>();

        // Try to get the locks associated with the lockManager.
        var availableLocks =
            mLockManagerMap.get(lockManager);

        // Check for and handle errors.
        if (availableLocks == null)
            deferredResult
                .setErrorResult(new IllegalArgumentException
                                (lockManager.name));
        else {
            try {
                // Run the computation off the Servlet thread.
                mExecutor
                    .submit(getRunnable(permits,
                                            availableLocks,
                                            deferredResult));
            } catch (Exception exception) {
                log("Catch exception "
                    + exception.getMessage());
                // Return an error message.

                deferredResult
                    .setErrorResult(exception);
            }
        }

        log("returning deferredResult");

        // Return the deferredResult before the locks
        // are obtained.
        return deferredResult;
    }

    /**
     * This factory method returns a {@link Runnable} that executes
     * asynchronously via the {@link SimpleAsyncTaskExecutor}.
     *
     * @param permits The number of permits to acquire
     * @param availableLocks The locks associated with the {@link LockManager}
     * @param deferredResult A {@link DeferredResult} containing the {@link List}
     *                      of {@link Lock} objects
     * @return A {@link Runnable} that executes asynchronously via the
     *         {@code SimpleAsyncTaskExecutor}
     */
    private Runnable getRunnable(int permits,
                                 ArrayBlockingQueue<Lock> availableLocks,
                                 DeferredResult<List<Lock>> deferredResult) {
        return () -> {
            // Create a List to hold the acquired Lock objects.
            List<Lock> acquiredLocks =
                new ArrayList<>(permits);

            // Keep trying to acquire 'permit' number of locks until we succeed.
            while (tryAcquireLock(availableLocks,
                                  acquiredLocks) != permits)
                continue;

            log("LockService - got all "
                + acquiredLocks.size()
                + " lock(s) "
                + acquiredLocks);

            // Set the deferredResult to the acquired List
            // of Lock objects.
            deferredResult.setResult(acquiredLocks);

            log("LockService - returning acquired locks "
                + acquiredLocks);
        };
    }

    /**
     * This helper method tries to acquire a {@link Lock}.
     *
     * @param availableLocks Contains the state of the semaphore
     * @param acquiredLocks The {@link List} of {@link Lock} objects
     *                       we're trying to acquire
     * @return The number of {@link Lock} objects in {@code
     *         acquiredLocks}
     */
    private Integer tryAcquireLock
        (ArrayBlockingQueue<Lock> availableLocks,
         List<Lock> acquiredLocks) {
        // Perform a non-blocking poll().
        var lock = availableLocks.poll();

        if (lock != null) {
            // Add the acquired lock to the List.
            acquiredLocks.add(lock);

            // Return the number of locks acquired.
            return acquiredLocks.size();
        } else {
            // Not enough locks available (yet), so release
            // the acquired locks and return 0 so the caller
            // will keep trying.
            acquiredLocks
                .forEach(availableLocks::offer);

            // Clear out the acquiredLocks List.
            acquiredLocks.clear();

            // Indicate that we need to start from the beginning.
            return 0;
        }
    }

    /**
     * Release the {@link Lock} so other Beings can acquire it.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param lock The {@link Lock} to release
     * @return A {@link Boolean} that emits {@link Boolean#TRUE} if
     *         the {@link Lock} was released properly and {@link
     *         Boolean#FALSE} otherwise
     */
    public Boolean release(LockManager lockManager,
                           Lock lock) {
        log("LockService.release(["
            + lock
            + "]) on "
            + lockManager);

        // Try to get the locks associated with the lockManager.
        var availableLocks =
            mLockManagerMap.get(lockManager);

        // Check for errors.
        if (availableLocks == null)
            return false;
        else
            // Put the Lock parameter back to the queue (does not block).
            return availableLocks.offer(lock);
    }

    /**
     * Release the {@code locks}.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param locks A {@link List} that contains {@link Lock}
     *              objects to release
     * @return A {@link Boolean} that emits {@link Boolean#TRUE} if
     *         the {@link Lock} was released properly and {@link
     *         Boolean#FALSE} otherwise
     */
    public Boolean release(LockManager lockManager,
                           List<Lock> locks) {
        log("LockService.release("
            + locks.size()
            + ") "
            + locks
            + " on "
            + lockManager);

        // Try to get the locks associated with the lockManager.
        var availableLocks =
            mLockManagerMap.get(lockManager);

        // Check for errors.
        if (availableLocks == null)
            return false;
        else {
            return locks
                // Convert List to a Stream.
                .stream()

                // Return true if all locks are put back
                // into mAvailableLocks successfully (does
                // not block).
                .allMatch(availableLocks::offer);
        }
    }
}
