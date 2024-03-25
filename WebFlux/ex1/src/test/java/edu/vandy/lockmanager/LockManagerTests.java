package edu.vandy.lockmanager;

import edu.vandy.lockmanager.client.LockAPI;
import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.common.LockManager;
import edu.vandy.lockmanager.server.LockManagerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static edu.vandy.lockmanager.utils.Utils.log;

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
                webEnvironment = SpringBootTest
                .WebEnvironment.DEFINED_PORT)
class LockManagerTests {
    /**
     * The auto-wired {@link LockAPI} that accesses the {@link
     * LockManagerApplication} microservice asynchronously using
     * Spring WebFlux.
     */
    @Autowired
    private LockAPI mLockAPI;

    /**
     * Test the {@link LockManagerApplication} microservice's ability
     * to acquire and release single locks.
     */
    @Test
    public void testSingleAcquireAndRelease() {
        log("testSingleAcquireAndRelease() started");

        // Define the parameters for the test.
        int maxLocks = 2;
        int maxClients = 4;

        mLockAPI
            // Create a Lock manager containing maxLocks.
            .create(maxLocks)

            // Acquire and release single locks asynchronously.
            .flatMap(lockManager -> {
                    log("The LockManager's id = " + lockManager);
                    return Flux
                        // Run maxClients tests asynchronously.
                        .range(0, maxClients)

                        // Call acquireAndReleaseLocks() each client.
                        .flatMap(client -> 
                                 acquireAndReleaseSingleLocks(client,
                                                              lockManager))

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

        // Define the parameters for the test.
        int maxLocks = 4;
        int maxClients = 8;
        int maxPermits = 2;

        mLockAPI
            // Create a Lock manager containing maxLocks.
            .create(maxLocks)

            // Acquire and release multiple locks asynchronously.
            .flatMap(lockManager -> {
                log("The LockManager's id = " + lockManager);
                return
                    Flux
                        // Run maxClients tests in asynchronously.
                        .range(0, maxClients)

                        // Call acquireAndReleaseLocks() each client.
                        .flatMap(client ->
                                 acquireAndReleaseMultipleLocks(client,
                                                                lockManager,
                                                                maxPermits))

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
     * @param lockManager The {@link LockManager} associated with this
     *                    test run
     */
    private Mono<Void> acquireAndReleaseSingleLocks
        (int client,
         LockManager lockManager) {
            log("Starting client " + client);
            return mLockAPI
                // Acquire a lock asynchronously.
                .acquire(lockManager)

                // Display the lock when it's acquired.
                .doOnSuccess(lock ->
                             log(client + " acquired lock " + lock))

                // Release the lock asynchronously.
                .flatMap(lock -> mLockAPI
                         .release(lockManager, lock))

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
     * @param client     The test client id
     * @param lockManager The {@link LockManager} associated with this
     *                    test run
     * @param maxPermits The number of permits to acquire and release
     *                   simultaneously
     */
    private Mono<Void> acquireAndReleaseMultipleLocks
        (int client,
         LockManager lockManager,
         int maxPermits) {
        log("Starting client " + client);

        return mLockAPI
            // Asynchronously acquire a maxPermits number of locks.
            .acquire(lockManager, maxPermits)

            // Collect the locks into a Mono to a List.
            .collectList()

            // Perform operations when the Mono emits its List.
            .flatMap(locks -> {
                    log(client + " acquired locks " + locks);

                    return mLockAPI
                        // Release locks after they're acquired.
                        .release(lockManager, locks)

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
