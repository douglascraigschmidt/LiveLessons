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
 * This Spring {@code @RestController} defines methods that
 * provide a distributed lock manager.
 */
@RestController
public class LockManagerController {
    /**
     * Auto-wire the {@link LockManagerController} to the
     * {@link LockManagerService}.
     */
    @Autowired
    LockManagerService mService;

    /**
     * @return Information indicating the {@link LockManagerController}
     *         is running
     */
    @RequestMapping("/")
    public String isAlive() {
        return "Alive and running on thread "
            + Thread.currentThread();
    }

    /**
     * Initialize the {@link Lock} manager.
     *
     * @param lockCount The number of {@link Lock} objects to
     *                  manage.
     * @return A {@link Mono} that emits {@link Boolean#TRUE} if
     *         the {@code permitCount} changed the state of the
     *         lock manager and {@link Boolean#FALSE} otherwise.
     */
    @PostMapping(CREATE)
    public Mono<Boolean> create(@RequestBody Integer lockCount) {
        Logger.log("LockController.create()");

        return mService
            // Forward to the service.
            .create(lockCount);
    }

    /**
     * Acquire a {@link Lock}.
     *
     * @return A {@link Mono} that emits an acquired {@link Lock}
     */
    @GetMapping(ACQUIRE_LOCK)
    public Mono<Lock> acquire() {
        Logger.log("LockController.acquire()");

        return mService
            // Forward to the service.
            .acquire();
    }

    /**
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param permits The number of permits to acquire
     * @return A {@link Flux} that emits {@code permits} number of
     *         acquired {@link Lock} objects
     */
    @GetMapping(ACQUIRE_LOCKS)
    Flux<Lock> acquire(Integer permits) {
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
     * @param lock The {@link Lock} to release
     * @return A {@link Mono} that emits {@link Boolean#TRUE} if
     *         the {@link Lock} was released properly and
     *         {@link Boolean#FALSE} otherwise.
     */
    @PostMapping(RELEASE_LOCK)
    public Mono<Boolean> release(@RequestBody Lock lock) {
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
     * @param locks A {@link List} that contains {@link Lock} objects
     *              to release
     * @return A {@link Mono} that emits {@link Boolean#TRUE} if
     *         the {@link Lock} was released properly and
     *         {@link Boolean#FALSE} otherwise.
     */
    @PostMapping(RELEASE_LOCKS)
    public Mono<Boolean> release(@RequestBody List<Lock> locks) {
        Logger.log("LockController.release("
                  + locks
                  + ")");

        return mService
            // Forward to the service.
            .release(locks);
    }
}

