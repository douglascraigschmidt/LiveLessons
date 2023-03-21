package edu.vandy.lockmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

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

        mService.create(lockCount);
    }

    /**
     * Acquire a {@link Lock}, blocking until one is available.
     * A {@link DeferredResult} result is used to avoid blocking
     * a thread in the Servlet thread pool.
     *
     * @return A {@link DeferredResult} to a {@link Lock} that a
     *         client can acquire and hold during critical sections
     */
    @GetMapping(ACQUIRE_LOCK)
    public DeferredResult<Lock> acquire() {
        Utils.log("LockController.acquire()");

        return mService.acquire();
    }

    /**
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param permits The number of permits to acquire
     * @return A {@link DeferredResult<List>} containing {@code
     *         permits} number of acquired {@link Lock} objects
     */
    @GetMapping(ACQUIRE_LOCKS)
    DeferredResult<List<Lock>> acquire(Integer permits) {
        Utils.log("LockController.acquire(permits)");

        return mService
            // Forward to the service.
            .acquire(permits);
    }

    /**
     * Release the {@link Lock} so other clients can acquire it.
     *
     * @param lock The {@link Lock} to release
     */
    @PostMapping(RELEASE_LOCK)
    public void release(@RequestBody Lock lock) {
        Utils.log("LockController.release(lock)");

        mService.release(lock);
    }

    /**
     * Release the {@code locks} so other clients can acquire them.
     *
     * @param locks A {@link List} that contains {@link Lock} objects
     *              to release
     */
    @PostMapping(RELEASE_LOCKS)
    public void release(@RequestBody List<Lock> locks) {
        Utils.log("LockController.release(locks)");

        mService
            // Forward to the service.
            .release(locks);
    }
}

