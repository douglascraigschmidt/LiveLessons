package edu.vandy.lockmanager.server;

import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.common.LockManager;
import edu.vandy.lockmanager.utils.Utils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static edu.vandy.lockmanager.utils.Utils.generateUniqueId;
import static edu.vandy.lockmanager.utils.Utils.log;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * This Spring {@code Service} implements the {@link
 * LockManagerController} endpoint handler methods using Spring
 * WebFlux reactive types and a {@link Map} of {@link LockManager}
 * objects associated with the {@link ArrayBlockingQueue} objects that
 * store the state of each semaphore.
 */
@SuppressWarnings("BlockingMethodInNonBlockingContext")
@Service
public class LockManagerService {
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
     * @param permitCount The number of {@link Lock} objects to manage
     * @return A {@link Mono} that emits a {@link LockManager}
     *         uniquely identifying this semaphore
     */
    public Mono<LockManager> create(Integer permitCount) {
        return Mono
            .fromSupplier(() -> {
                    var availableLocks =
                        // Make an ArrayBlockQueue with "fair"
                        // semantics that limits concurrent access to
                        // the fixed number of available locks.
                        new ArrayBlockingQueue<Lock>(permitCount,
                                                     true);

                    // Add each Lock to the queue.
                    availableLocks.addAll(makeLocks(permitCount));

                    // Create a new LockManager with a unique name.
                    var lockManager =
                        new LockManager(generateUniqueId(),
                                        permitCount);

                    // Insert the new LockManager and the
                    // ArrayBlockingQueue into the Map.
                    mLockManagerMap.put(lockManager,
                                        availableLocks);

                    log("LockService.create("
                        + permitCount
                        + ") "
                        + "- made "
                        +  lockManager
                        + " with locks = "
                        + availableLocks);

                    // Return the new LockManager.
                    return lockManager;
                });
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
     * Acquire a {@link Lock}, blocking until one is available, but
     * return a {@link Mono} so the caller needn't block.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @return A {@link Mono} that emits a {@link Lock}
     */
    public Mono<Lock> acquire(LockManager lockManager) {
        log("LockService.acquire() on " + lockManager);

        return Mono
            // Acquire an available lock, which may block.
            .fromCallable(() -> {
                    log("LockService - requesting a Lock");

                    // Find the current state of the semaphore
                    // associated with lockManager.
                    var availableLocks =
                        mLockManagerMap.get(lockManager);

                    if (availableLocks == null)
                        throw new IllegalArgumentException
                                     (lockManager.name);
                    else {
                        var lock = availableLocks.poll();

                        if (lock != null)
                            log("LockService - obtained Lock non-blocking "
                                + lock);
                        else {
                            // This call can block since it runs in a
                            // virtual thread.
                            lock = availableLocks.take();

                            log("LockService - obtained Lock blocking "
                                + lock);
                        }

                        // Return the Lock.
                        return lock;
                    }
                })
            // Display any exception that might occur.
            .doOnError(exception ->
                       log("LockService error - "
                           + exception.getMessage()))
            .doOnSuccess(mono ->
                         log("LockService - returning Mono"));
    }

    /**
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param permits The number of permits to acquire
     * @return A {@link Flux} that emits {@code permits} newly
     *         acquired {@link Lock} objects
     */
    public Flux<Lock> acquire(LockManager lockManager,
                              int permits) {
        log("LockService.acquire("
            + permits
            + ")");

        // Find the current state of the semaphore associated with
        // lockManager.
        var availableLocks =
            mLockManagerMap.get(lockManager);

        if (availableLocks == null)
            throw new IllegalArgumentException
                (lockManager.name);
        else {
            // Create a List to hold the acquired Lock objects.
            List<Lock> acquiredLocks =
                new ArrayList<>(permits);

            var flux = Mono
                // Create a Mono that executes tryAcquireLock() method
                // and emits its result.
                .fromSupplier(() ->
                              tryAcquireLock(availableLocks,
                                             acquiredLocks))

                // Repeat the Mono indefinitely.
                .repeat()

                // Take elements from the stream until the number of
                // acquired locks is equal to 'permits'.
                .takeUntil(result -> result.equals(permits))

                // Log the results.
                .doOnNext(result -> {
                        if (result == permits)
                            log("LockService.acquire("
                                + permits
                                + ") = "
                                + result);
                    })
                // Transform Flux<Integer> to Flux<Lock> that emits
                // the acquired Lock objects as individual elements.
                .thenMany(Flux.fromIterable(acquiredLocks));

            log("LockService.acquire("
                + permits
                + ") returning Flux");
            return flux;
        }
    }

    /**
     * This helper method tries to acquire a {@link Lock}.
     *
     * @param availableLocks Contains the state of the semaphore
     * @param acquiredLocks The {@link List} of {@link Lock} objects
     *                      we're trying to acquire
     * @return The number of {@link Lock} objects in {@code
     *         acquiredLocks}
     */
    private Integer tryAcquireLock(ArrayBlockingQueue<Lock> availableLocks,
                                   List<Lock> acquiredLocks) {
        // Perform a non-blocking poll().
        var lock = availableLocks.poll();

        if (lock != null) {
            // Add the acquired lock to the List.
            acquiredLocks.add(lock);

            // Return the number of acquired locks.
            return acquiredLocks.size();
        } else {
            // Not enough locks are available, so release the acquired
            // locks.
            acquiredLocks
                // offer() does not block.
                .forEach(availableLocks::offer);

            // Clear out the acquiredLocks List.
            acquiredLocks.clear();

            // Indicate we need to restart from the beginning.
            return 0;
        }
    }

    /**
     * Release the {@link Lock}.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param lock The {@link Lock} to release
     * @return A {@link Mono} that emits {@link Boolean#TRUE} if the
     *         {@link Lock} was released properly and {@link
     *         Boolean#FALSE} otherwise.
     */
    public Mono<Boolean> release(LockManager lockManager,
                                 Lock lock) {
        log("LockService.release(["
            + lock
            + "]) on "
            + lockManager);

        // Try to get the locks associated with the lockManager.
        var availableLocks =
            mLockManagerMap.get(lockManager);

        if (availableLocks == null)
            return Mono
                .just(FALSE);
        return Mono
            // Put the lock back into mAvailableQueue w/out blocking.
            .just(availableLocks.offer(lock));
    }

    /**
     * Release the {@code locks}.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param locks A {@link List} that contains {@link Lock}
     *              objects to release
     * @return A {@link Mono} that emits {@link Boolean#TRUE} if the
     *         {@link Lock} was released properly and {@link
     *         Boolean#FALSE} otherwise.
     */
    public Mono<Boolean> release(LockManager lockManager,
                                 List<Lock> locks) {
        log("LockService.release("
            + locks.size()
            + ") "
            + locks
            + " on "
            + lockManager);

        // Try to get the locks associated with lockManager.
        var availableLocks =
            mLockManagerMap.get(lockManager);

        if (availableLocks == null)
            return Mono
                .just(FALSE);
        else {
            boolean allReleased = locks
                // Convert List to a Stream.
                .stream()

                // Return true if all locks are put back into
                // mAvailableLocks successfully (does not block).
                .allMatch(availableLocks::offer);

            return Mono
                // Return the result, either true or false.
                .just(allReleased);
        }
    }
}
