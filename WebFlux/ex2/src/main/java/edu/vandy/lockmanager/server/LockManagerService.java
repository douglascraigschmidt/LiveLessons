package edu.vandy.lockmanager.server;

import edu.vandy.lockmanager.common.Lock;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.IntStream;

import static edu.vandy.lockmanager.utils.Logger.log;

/**
 * This Spring {@code Service} implements the {@link LockManagerController}
 * endpoint handler methods.
 */
@SuppressWarnings("BlockingMethodInNonBlockingContext")
@Service
public class LockManagerService {
    /**
     * An {@link ArrayBlockingQueue} that limits concurrent access to
     * the fixed number of available locks managed by the {@link
     * LockManagerService}.
     */
    private ArrayBlockingQueue<Lock> mAvailableLocks;

    /**
     * Initialize the {@link Lock} manager.
     *
     * @param permitCount The number of {@link Lock} objects to manage
     */
    public void create(Integer permitCount) {
        log("creating " + permitCount + " locks");

        if (mAvailableLocks == null) {
            mAvailableLocks =
                // Make an ArrayBlockQueue with "fair" semantics.
                new ArrayBlockingQueue<>(permitCount,
                                         true);
        } else if (permitCount != mAvailableLocks.size()) {
            mAvailableLocks.clear();
            mAvailableLocks =
                // Make an ArrayBlockQueue with "fair" semantics.
                new ArrayBlockingQueue<>(permitCount,
                                         true);
        } else
            return;

        // Add each Lock to the queue.
        mAvailableLocks.addAll(makeLocks(permitCount));
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
     * Acquire a {@link Lock}, blocking until one is available,
     * but returning a {@link Mono} so the client doesn't
     * have to block.
     *
     * @return A {@link Mono} that emits a {@link Lock}
     */
    public Mono<Lock> acquire() {
        log("LockService.acquire()");

        var result = Mono
            // Acquire an available lock, which may block.
            .fromCallable(() -> {
                    log("LockService - requesting a Lock");

                    var lock = mAvailableLocks.take();

                    log("LockService - obtained Lock "
                        + lock);

                    // Return the Lock.
                    return lock;
                })

            // Display any exception that might occur.
            .doOnError(exception ->
                       log("LockService error - "
                           + exception.getMessage()));

        log("LockService - returning Mono");

        // This Mono is returned before the lock is acquired.
        return result;
    }

    /**
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param permits The number of permits to acquire
     * @return A {@link Flux} that emits {@code permits} newly
     *         acquired {@link Lock} objects
     */
    public Flux<Lock> acquire(int permits) {
        log("LockService.acquire("
            + permits
            + ")");

        // Create a List to hold the acquired Lock objects.
        List<Lock> acquiredLocks =
            new ArrayList<>(permits);

        return Mono
            // Create a Mono that executes tryAcquireLock() method and
            // emits its result.
            .fromCallable(() ->
                          tryAcquireLock(acquiredLocks))

            // Repeat the Mono indefinitely.
            .repeat()

            // Take elements from the stream until the number of
            // acquired locks is equal to 'permits'.
            .takeUntil(result -> result.equals(permits))

            // Transform the Flux<Integer> to a Flux<Lock> that emits
            // the acquired Lock objects as individual elements.
            .thenMany(Flux.fromIterable(acquiredLocks));
    }

    /**
     * This helper method tries to acquire a {@link Lock}.
     *
     * @param acquiredLocks The {@link List} of {@link Lock} objects
     *                      we're trying to acquire
     * @return The number of {@link Lock} objects in {@code
     *         acquiredLocks}
     */
    private Integer tryAcquireLock(List<Lock> acquiredLocks) {
        // Perform a non-blocking poll().
        var lock = mAvailableLocks.poll();

        if (lock == null) {
            // Not enough locks are available, so release the acquired
            // locks.
            acquiredLocks
                .forEach(mAvailableLocks::offer);

            // Clear out the acquiredLocks List.
            acquiredLocks.clear();

            // Indicate we need to restart from the beginning.
            return 0;
        }

        // Add the acquired lock to the List.
        acquiredLocks.add(lock);

        // Return the number of acquired locks.
        return acquiredLocks.size();
    }

    /**
     * Release the {@link Lock}.
     *
     * @param lock The {@link Lock} to release
     * @return A {@link Mono} that emits {@link Void}
     */
    public Mono<Void> release(Lock lock) {
        log("LockService.release("
            + lock
            + ")");

        // Put the lock back into mAvailableQueue.
        mAvailableLocks.offer(lock);

        return Mono
            // Indicate that we're done.
            .empty();
    }

    /**
     * Release the {@code locks}.
     *
     * @param locks A {@link List} that contains {@link Lock}
     *              objects to release
     * @return A {@link Mono} that emits {@link Void}
     */
    public Mono<Void> release(List<Lock> locks) {
        log("LockService.release("
            + locks
            + ")");

        locks
            // Put all the locks back into mAvailableLocks.
            .forEach(mAvailableLocks::offer);

        return Mono
            // Indicate that we're done.
            .empty();
    }
}
