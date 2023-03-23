package edu.vandy.lockmanager;

import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.server.LockManagerApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

import static edu.vandy.lockmanager.common.Constants.Endpoints.*;

/**
 * An auto-generated proxy used by clients to access the
 * capabilities of the {@link LockManagerApplication} microservice.
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
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param permits The number of permits to acquire
     * @return A {@link List} containing {@code permits} newly
     *         acquired {@link Lock} objects
     */
    @GetExchange(ACQUIRE_LOCKS)
    List<Lock> acquire(@RequestParam Integer permits);

    /**
     * Release the lock back to the {@link Lock} manager.
     *
     * @param lock The {@link Lock} to release
     */
    @PostExchange(RELEASE_LOCK)
    void release(@RequestBody Lock lock);

    /**
     * Release the {@code locks} back to the {@link Lock} manager.
     *
     * @param locks A {@link List} that contains {@link Lock} objects to release
     */
    @PostExchange(RELEASE_LOCKS)
    void release(@RequestBody List<Lock> locks);
}
