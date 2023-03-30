package edu.vandy.lockmanager.server;

import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static edu.vandy.lockmanager.common.Constants.Endpoints.*;

/**
 * This Spring {@code @RestController} defines methods that provide a
 * lock manager for a semaphore that can be shared amongst multiple
 * asynchronous Spring WebFlux clients.
 */
@RestController
public class LockManagerController {
    /**
     * Auto-wire the {@link LockManagerController} to the {@link
     * LockManagerService}.
     */
    @Autowired
    LockManagerService mService;

    /**
     * Initialize the {@link Lock} manager.
     *
     * @param permitCount The number of {@link Lock} objects to
     *                    manage
     * @return A {@link Mono} that emits the {@link LockManager}
     *         associated with the state of the semaphore it manages
     */
    @GetMapping(CREATE)
    public Mono<LockManager> create(@RequestParam Integer lockCount) {
        Logger.log("LockController.create()");

        return mService
            // Forward to the service.
            .create(lockCount);
    }

    /**
     * Acquire a {@link Lock}.
     *
     * @param lockManager The {@link LockManager} that is associated
     *         with the state of the semaphore it manages
     * @return A {@link Mono} that emits an acquired {@link Lock}
     */
    @GetMapping(ACQUIRE_LOCK)
    public Mono<Lock> acquire(@RequestParam LockManager lockManager) {
        Logger.log("LockController.acquire()");

        return mService
            // Forward to the service.
            .acquire();
    }

    /**
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param lockManager The {@link LockManager} that is associated
     *         with the state of the semaphore it manages
     * @param permits The number of permits to acquire
     * @return A {@link Flux} that emits {@code permits} number of
     *         acquired {@link Lock} objects
     */
    @GetMapping(ACQUIRE_LOCKS)
    Flux<Lock> acquire(@RequestParam LockManager lockManager,
                       Integer permits) {
        Logger.log("LockController.acquire("
                  + permits
                  + ")");

        return mService
            // Forward to the service.
            .acquire(permits);
    }

    /**
     * Release the {@link Lock} so other clients can acquire it.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param lock The {@link Lock} to release
     * @return A {@link Mono} that emits {@link Boolean#TRUE} if
     *         the {@link Lock} was released properly and
     *         {@link Boolean#FALSE} otherwise.
     */
    @GetMapping(RELEASE_LOCK)
    public Mono<Boolean> release(@RequestParam LockManager lockManager,
                                 @RequestBody Lock lock) {
        Logger.log("LockController.release("
                  + lock
                  + ")");

        return mService
            // Forward to the service.
            .release(lock);
    }

    /**
     * Release the {@code locks} so other clients can acquire them.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param locks A {@link List} that contains {@link Lock} objects
     *              to release
     * @return A {@link Mono} that emits {@link Boolean#TRUE} if the
     *         {@link Lock} was released properly and {@link
     *         Boolean#FALSE} otherwise.
     */
    @PostMapping(RELEASE_LOCKS)
    public Mono<Boolean> release
        (@RequestParam LockManager lockManager,
         @RequestBody List<Lock> locks) {
        Logger.log("LockController.release("
                  + locks
                  + ")");

        return mService
            // Forward to the service.
            .release(locks);
    }
}

