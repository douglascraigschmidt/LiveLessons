package edu.vandy.lockmanager.server;

import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

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
     * @param permitCount The number of {@link Lock} objects to
     *                    manage
     */
    @PostMapping(CREATE)
    public void create(@RequestBody Integer permitCount) {
        Logger.log("LockController.create()");

        mService.create(permitCount);
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
        Logger.log("LockController.acquire()");

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
        Logger.log("LockController.acquire(permits)");

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
        Logger.log("LockController.release(lock)");

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
        Logger.log("LockController.release(locks)");

        mService
            // Forward to the service.
            .release(locks);
    }
}

