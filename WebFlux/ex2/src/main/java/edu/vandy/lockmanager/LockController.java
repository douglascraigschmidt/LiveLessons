package edu.vandy.lockmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static edu.vandy.lockmanager.Constants.Endpoints.*;

/**
 * This Spring {@code @RestController} defines methods that
 * provide a distributed lock manager.
 */
@RestController
public class LockController {
    /**
     * Auto-wire the {@link LockController} to the
     * {@link LockService}.
     */
    @Autowired
    LockService mService;

    /**
     * @return Information indicating the {@link LockController}
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
     */
    @PostMapping(CREATE)
    public void create(@RequestBody Integer lockCount) {
        Utils.log("LockController.create()");

        mService
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
        Utils.log("LockController.acquire()");

        return mService
            // Forward to the service.
            .acquire();
    }

    /**
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param permits The number of permits to acquire
     * @return A {@link Flux} that emits {@code permits} newly
     *         acquired {@link Lock} objects
     */
    @GetMapping(ACQUIRE_LOCKS)
    Flux<Lock> acquire(Integer permits) {
        Utils.log("LockController.acquire(permits)");

        return mService
            // Forward to the service.
            .acquire(permits);
    }

    /**
     * Release the {@link Lock} so other clients can acquire it.
     *
     * @param lock The {@link Lock} to release
     * @return A {@link Mono} that emits {@link Void}
     */
    @PostMapping(RELEASE_LOCK)
    public Mono<Void> release(@RequestBody Lock lock) {
        Utils.log("LockController.release(lock)");

        return mService
            // Forward to the service.
            .release(lock);
    }

    /**
     * Release the {@code locks} so other clients can acquire them.
     *
     * @param locks A {@link List} that contains {@link Lock} objects
     *              to release
     * @return A {@link Mono} that emits {@link Void}
     */
    @PostMapping(RELEASE_LOCKS)
    public Mono<Void> release(@RequestBody List<Lock> locks) {
        Utils.log("LockController.release(locks)");

        return mService
            // Forward to the service.
            .release(locks);
    }
}

