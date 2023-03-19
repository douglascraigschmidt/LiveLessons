package edu.vandy.lockmanager;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
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
     * Acquire a {@link Lock}, blocking until one is available.
     *
     * @return A {@link Lock} that can be gazed into
     */
    public Mono<Lock> acquire() {
        log("LockService.acquire()");

        var result = Mono
            .fromCallable(() -> {
                log("LockService - requesting a Lock");

                var lock = mAvailableLocks.take();

                log("LockService - returning Lock "
                    + lock);
                return lock;
            })
            .subscribeOn(Schedulers.parallel())
            .doOnError(exception ->
                log("LockService error - "
                    + exception.getMessage()))
            .delayElement(Duration.ofSeconds(2));

        log("LockService - returning Mono");
        return result;
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
