package edu.vandy.lockmanager;

import edu.vandy.lockmanager.client.LockAPI;
import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.server.LockManagerApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static edu.vandy.lockmanager.utils.Logger.log;

/**
 * This program tests the features of the {@link LockManagerApplication}
 * microservice, which uses Spring WebFlux to provide a distributed
 * lock manager for asynchronous Spring applications using
 * asynchronous controller methods that return Project Reactor {@link
 * Mono} and {@link Flux} reactive objects.  It also shows how to use the asynchronous
 * HTTP interface features in Spring framework 6, which enables the
 * definition of declarative HTTP services using Java interfaces.
 */
@SpringBootTest(classes = LockManagerApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LockManagerTests {
    /**
     * The auto-wired {@link LockAPI} that accesses the {@link
     * LockManagerApplication} microservice.
     */
    @Autowired
    private LockAPI mLockAPI;

    /**
     * Number of permits to acquire and release simultaneously.
     */
    private static final int sMULTIPLE_PERMITS = 2;

    /**
     * The total number of {@link Lock} objects to create.
     */
    private static final Integer sMAX_LOCKS = 4;

    /**
     * The total number of clients to run concurrently.
     */
    private static final int sMAX_CLIENTS = 4;

    /**
     * Test the {@link LockManagerApplication} microservice's ability
     * to acquire and release single locks.
     */
    @Test
    public void testSingleAcquireAndRelease() {
        log("testSingleAcquireAndRelease() started");

        mLockAPI
            // Create a Lock manager containing mMAX_LOCKS.
            .create(sMAX_LOCKS)

            // Acquire and release single locks asynchronously.
            .flatMap(changed -> {
                    log("The LockManager state changed = "
                        + changed);
                   return Flux
                        // Run mMAX_CLIENTS tests in parallel.
                        .range(0, sMAX_CLIENTS)

                        // Call acquireAndReleaseLocks() each client.
                        .flatMap(this::acquireAndReleaseSingleLocks)

                        // Collect the results into a List<Void>.
                        .collectList();
                })

            // Block until all async processing is done.
            .block();

        log("testSingleAcquireAndRelease() finished");
    }

    /**
     * Test the {@link LockManagerApplication} microservice's ability to
     * acquire and release multiple locks.
     */
    @Test
    public void testMultipleAcquireAndRelease() {
        log("testMultipleAcquireAndRelease() started");

        mLockAPI
            // Create a Lock manager containing mMAX_LOCKS.
            .create(sMAX_LOCKS)

            // Acquire and release multiple locks asynchronously.
            .flatMap(changed -> {
                log("The LockManager state changed = "
                    + changed);
                return
                    Flux
                        // Run mMAX_CLIENTS tests in parallel.
                        .range(0, sMAX_CLIENTS)

                        // Call acquireAndReleaseLocks() each client.
                        .flatMap(this::acquireAndReleaseMultipleLocks)

                        // Collect the results into a List<Void>.
                        .collectList();
            })

            // Block until all async processing is done.
            .block();

        log("testMultipleAcquireAndRelease() finished");
    }

    /**
     * Acquire and release single {@link Lock} objects.
     *
     * @param client The test client
     */
    private Mono<Void> acquireAndReleaseSingleLocks
    (Integer client) {
        log("Starting client "
            + client);
        return mLockAPI
            // Acquire a lock asynchronously.
            .acquire()

            // Display the lock when it's acquired.
            .doOnSuccess(lock ->
                log(client + " acquired lock " + lock))

            // Release the lock asynchronously.
            .flatMap(lock -> mLockAPI
                .release(lock))

            // Indicate whether release() worked.
            .doOnSuccess(result ->
                log(client + " released lock " + result))

            // Log any exceptions.
            .doOnError(exception ->
                log("exception = " + exception.getMessage()))

            // Indicate that we're finished running asynchronously.
            .then();
    }

    /**
     * Acquire and release multiple {@link Lock} objects.
     *
     * @param client The test client
     */
    private Mono<Void> acquireAndReleaseMultipleLocks
    (Integer client) {
        log("Starting client " + client);

        return mLockAPI
            // Asynchronously acquire a lock.
            .acquire(sMULTIPLE_PERMITS)

            // Collect the locks into a Mono to a List.
            .collectList()

            // Perform operations when the Mono emits its List.
            .flatMap(locks -> {
                log(client + " acquired locks " + locks);

                return mLockAPI
                    // Release the locks when they're all acquired.
                    .release(locks)

                    // Indicate whether release() worked.
                    .doOnSuccess(result ->
                        log(client + " released locks " + result))

                    // Log any exceptions.
                    .doOnError(exception -> log("exception = "
                        + exception.getMessage()))

                    // Return an empty Mono to indicate we're done.
                    .then();
            });
    }
}
