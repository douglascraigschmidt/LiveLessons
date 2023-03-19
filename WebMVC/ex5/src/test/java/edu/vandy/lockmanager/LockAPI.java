package edu.vandy.lockmanager;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import static edu.vandy.lockmanager.Constants.Endpoints.*;

/**
 * An auto-generated proxy used by clients to access the
 * capabibilities of the {@link LockApplication} microservice.
 */
public interface LockAPI {
    /**
     * Initialize the {@link Lock} manager.
     *
     * @param maxLocks The total number of {@link Lock}
     *                 objects to create
     */
    @PostExchange(CREATE)
    void create(@RequestBody Integer maxLocks);

    /**
     * @return A newly acquired {@link Lock}
     */
    @GetExchange(ACQUIRE_LOCK)
    Lock acquire();

    /**
     * Release the lock back to the {@link Lock} manager.
     *
     * @param lock The {@link Lock} to release
     */
    @PostExchange(RELEASE_LOCK)
    void release(@RequestBody Lock lock);
}
