package edu.vandy.lockmanager.server;

import edu.vandy.lockmanager.common.Lock;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.IntStream;

import static edu.vandy.lockmanager.utils.Logger.log;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

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
     * @return A {@link Mono} that emits {@link Boolean#TRUE} if
     * the {@code permitCount} changed the state of the
     * lock manager and {@link Boolean#FALSE} otherwise.
     */
    public Mono<Boolean> create(Integer permitCount) {
        log("creating " + permitCount + " locks");

        // Check to see if mAvailableLocks should be initialized or
        // resized.
        if (mAvailableLocks == null || permitCount != mAvailableLocks.size()) {
            // Clear the existing queue.
            if (mAvailableLocks != null)
                mAvailableLocks.clear();

            mAvailableLocks =
                // Make an ArrayBlockQueue with "fair" semantics.
                new ArrayBlockingQueue<>(permitCount, true);

            mAvailableLocks
                // Add each Lock to the queue.
                .addAll(makeLocks(permitCount));

            return Mono
                // Indicate something changed as a result of
                // this call.
                .just(TRUE);
        } else
            return Mono
                // Indicate nothing changed as a result of this call.
                .just(FALSE);
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

                // This call can block since it runs in a
                // virtual thread.
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
     * acquired {@link Lock} objects
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
            .fromSupplier(() ->
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
     * acquiredLocks}
     */
    private Integer tryAcquireLock(List<Lock> acquiredLocks) {
        // Perform a non-blocking poll().
        var lock = mAvailableLocks.poll();

        if (lock == null) {
            // Not enough locks are available, so release the acquired
            // locks.
            acquiredLocks
                // offer() does not block.
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
     * @return A {@link Mono} that emits {@link Boolean#TRUE} if
     * the {@link Lock} was released properly and
     * {@link Boolean#FALSE} otherwise.
     */
    public Mono<Boolean> release(Lock lock) {
        log("LockService.release("
            + lock
            + ")");

        return Mono
            // Put the lock back into mAvailableQueue w/out blocking.
            .just(mAvailableLocks.offer(lock));
    }

    /**
     * Release the {@code locks}.
     *
     * @param locks A {@link List} that contains {@link Lock}
     *              objects to release
     * @return A {@link Mono} that emits {@link Boolean#TRUE} if
     *         the {@link Lock} was released properly and
     *         {@link Boolean#FALSE} otherwise.
     */
    public Mono<Boolean> release(List<Lock> locks) {
        log("LockService.release("
            + locks
            + ")");

        boolean allReleased = locks
            // Convert List to a Stream.
            .stream()

            // Return true if all locks are put back
            // into mAvailableLocks successfully (does
            // not block).
            .allMatch(mAvailableLocks::offer);

        return Mono
            // Return the result, either true or false.
            .just(allReleased);
    }
}
