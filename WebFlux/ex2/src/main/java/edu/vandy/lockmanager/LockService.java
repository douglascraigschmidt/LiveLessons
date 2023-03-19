package edu.vandy.lockmanager;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
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
     * Acquire a {@link Lock}, blocking until one is available,
     * but returning a {@link Mono} so the client doesn't
     * have to block.
     *
     * @return A {@link Mono} that emits a {@link Lock}
     */
    public Mono<Lock> acquire() {
        log("LockService.acquire()");

        var result = Mono
            // Acquire an available lock, which may
            // block.
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
                    + exception.getMessage()))

            // Add a delay to make the test interesting.
            .delayElement(Duration.ofSeconds(2));

        log("LockService - returning Mono");

        // This Mono will be returned before the lock
        // is acquired.
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
        log("LockService.acquire(permits)");

        var result = Flux
                .range(0, permits)
                .map(___ -> {
                    log("LockService - requesting a Lock");

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

                // Display any exception that might occur.
                .doOnError(exception ->
                    log("LockService error - "
                        + exception.getMessage()));

                // Add a delay to make the test interesting.
                //.delayElement(Duration.ofSeconds(2));

        log("LockService - returning Flux");

        // This Flux will be returned before the lock
        // is acquired.
        return result;
    }

    /**
     * Release the {@link Lock} so other Beings can acquire it.
     *
     * @param Lock The {@link Lock} to release
     */
    public Mono<Void> release(Lock Lock) {
        log("LockService.release()");
        return Mono
            .fromCallable(() -> {
                try {
                    // Put the Lock parameter back into the queue.
                    mAvailableLocks.put(Lock);
                    log("releasing " + Lock);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Mono.empty();
            })
            .then();
    }
}
