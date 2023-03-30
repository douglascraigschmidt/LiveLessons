package edu.vandy.lockmanager.client;

import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.common.LockManager;
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
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param permitCount The total number of {@link Lock}
     *                    objects to create
     * @return A {@link LockManager} that is associated with
     *         the state of the semaphore it manages
     */
    @GetExchange(CREATE)
    LockManager create(@RequestParam Integer permitCount);

    /**
     * Acquire a {@link Lock}, blocking until one is available.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @return A newly acquired {@link Lock}
     */
    @GetExchange(ACQUIRE_LOCK)
    Lock acquire(@RequestParam LockManager lockManager);

    /**
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param permits The number of permits to acquire
     * @return A {@link List} containing {@code permits} newly
     *         acquired {@link Lock} objects
     */
    @GetExchange(ACQUIRE_LOCKS)
    List<Lock> acquire(@RequestParam LockManager lockManager,
                       @RequestParam Integer permits);

    /**
     * Release the lock back to the {@link Lock} manager.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param lock The {@link Lock} to release
     * @return A {@link Boolean} containing {@link Boolean#TRUE} if
     *         the {@link Lock} was released properly and {@link
     *         Boolean#FALSE} otherwise
     */
    @GetExchange(RELEASE_LOCK)
    Boolean release(@RequestParam LockManager lockManager,
                    @RequestParam Lock lock);

    /**
     * Release the {@code locks} back to the {@link Lock} manager.
     *
     * @param lockManager The {@link LockManager} that is associated
     *                    with the state of the semaphore it manages
     * @param locks A {@link List} containing {@link Lock} objects to
     *              release
     * @return A {@link Boolean} containing {@link Boolean#TRUE} if
     *         the {@link Lock} was released properly and {@link
     *         Boolean#FALSE} otherwise
     */
    @PostExchange(RELEASE_LOCKS)
    Boolean release(@RequestParam LockManager lockManager,
                    @RequestBody List<Lock> locks);
}
