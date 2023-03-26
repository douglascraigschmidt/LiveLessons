package edu.vandy.lockmanager.client;

import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.server.LockManagerApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
     * Acquire a {@link Lock}.
     *
     * @return A {@link Mono} that emits a newly acquired
     *         {@link Lock}
     */
    @GetExchange(ACQUIRE_LOCK)
    Mono<Lock> acquire();

    /**
     * Acquire {@code permits} number of {@link Lock} objects.
     *
     * @param permits The number of permits to acquire
     * @return A {@link Flux} that emits {@code permits} newly
     *         acquired {@link Lock} objects
     */
    @GetExchange(ACQUIRE_LOCKS)
    Flux<Lock> acquire(@RequestParam Integer permits);

    /**
     * Release the {@code lock} back to the {@link Lock} manager.
     *
     * @param lock A {@link Lock} to release
     * @return A {@link Mono} that emits a {@link Void}
     */
    @PostExchange(RELEASE_LOCK)
    Mono<Void> release(@RequestBody Lock lock);

    /**
     * Release the {@code locks} back to the {@link Lock} manager.
     *
     * @param locks A {@link List} that contains {@link Lock} objects to release
     * @return A {@link Mono} that emits a {@link Void}
     */
    @PostExchange(RELEASE_LOCKS)
    Mono<Void> release(@RequestBody List<Lock> locks);
}
