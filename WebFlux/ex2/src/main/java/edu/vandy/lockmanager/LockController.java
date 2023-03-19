package edu.vandy.lockmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Mono;

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
     *
     * @return A {@link Lock} that can be acquired
     */
    @GetMapping(ACQUIRE_LOCK)
    public Mono<Lock> acquire() {
        Utils.log("LockController.acquire()");

        return mService.acquire();
    }

    /**
     * Release the {@link Lock} so other clients can acquire it.
     *
     * @param lock The {@link Lock} to release
     */
    @PostMapping(RELEASE_LOCK)
    public void release(@RequestBody Lock lock) {
        Utils.log("LockController.release()");

        mService.release(lock);
    }
}

