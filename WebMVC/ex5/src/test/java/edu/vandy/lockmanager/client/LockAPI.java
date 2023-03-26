package edu.vandy.lockmanager.client;

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
     * @return A {@link Boolean} containing {@link Boolean#TRUE} if
     *         the {@code permitCount} changed the state of the lock
     *         manager and {@link Boolean#FALSE} otherwise.
     */
    @PostExchange(CREATE)
    Boolean create(@RequestBody Integer maxLocks);

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
     * @return A {@link Boolean} containing {@link Boolean#TRUE} if
     *         the {@link Lock} was released properly and {@link
     *         Boolean#FALSE} otherwise.
     */
    @PostExchange(RELEASE_LOCK)
    Boolean release(@RequestBody Lock lock);

    /**
     * Release the {@code locks} back to the {@link Lock} manager.
     *
     * @param locks A {@link List} containing {@link Lock} objects to
     *              release
     * @return A {@link Boolean} containing {@link Boolean#TRUE} if
     *         the {@link Lock} was released properly and {@link
     *         Boolean#FALSE} otherwise.
     */
    @PostExchange(RELEASE_LOCKS)
    Boolean release(@RequestBody List<Lock> locks);
}
