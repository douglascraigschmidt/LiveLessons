package edu.vandy.lockmanager;

import edu.vandy.lockmanager.client.LockAPI;
import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.common.LockManager;
import edu.vandy.lockmanager.server.LockManagerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.stream.IntStream;

import static edu.vandy.lockmanager.utils.Utils.log;

/**
 * This program tests the features of the {@link
 * LockManagerApplication} microservice, which uses Spring WebMVC to
 * provide a distributed lock manager for synchronous Spring client
 * applications using quasi-asynchronous controller methods that
 * return Spring {@link DeferredResult} objects.  It also shows how to
 * use the synchronous HTTP interface features in Spring framework 6,
 * which enables the definition of declarative HTTP services using
 * Java interfaces.
 */
@SpringBootTest(classes = LockManagerApplication.class,
    webEnvironment = SpringBootTest
        .WebEnvironment.DEFINED_PORT)
@ComponentScan("edu.vandy.lockmanager")
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
     * Test the {@link LockManagerApplication} microservice's ability
     * to acquire and release single locks concurrently.
     */
    @Test
    public void testSingleAcquireAndRelease() {
        log("testSingleAcquireAndRelease() started");

        // Define the parameters for the test.
        int maxLocks = 2;
        int maxClients = 4;

        // Create a LockManager instance that holds
        // maxLocks.
        var lockManager = mLockAPI
            .create(maxLocks);

        // Run maxClients tests in parallel.
        IntStream
            // Perform mMAX_CLIENTS tests.
            .range(0, maxClients)

            // Run each test in parallel.
            .parallel()

            // Perform this action for each client.
            .forEach(client ->
                acquireAndReleaseSingleLocks(client,
                    lockManager));

        log("testSingleAcquireAndRelease() finished");
    }

    /**
     * Test the {@link LockManagerApplication} microservice's ability
     * to acquire and release multiple locks concurrently.
     */
    @Test
    public void testMultipleAcquireAndRelease() {
        log("testMultipleAcquireAndRelease() started");

        // Define the parameters for the test.
        int maxLocks = 4;
        int maxClients = 8;
        int maxPermits = 2;

        // Create a LockManager instance.
        var lockManager = mLockAPI
            .create(maxLocks);

        log("create LockManager " + lockManager);

        IntStream
            // Run mMAX_CLIENTS tests.
            .range(0, maxClients)

            // Run each test in parallel.
            .parallel()

            // Perform this action for each client.
            .forEach(client ->
                acquireAndReleaseMultipleLocks(client,
                    lockManager,
                    maxPermits));

        log("testMultipleAcquireAndRelease() finished");
    }

    /**
     * Acquire and release single {@link Lock} objects.
     *
     * @param client      The test client
     * @param lockManager
     */
    private void acquireAndReleaseSingleLocks
    (int client,
     LockManager lockManager) {
        log("Starting client " + client);
        var lock = mLockAPI.acquire(lockManager);
        log(client + " acquired lock " + lock);
        var result = mLockAPI
            .release(lockManager, lock);
        log(client + " released lock " + result);
    }

    /**
     * Acquire and release multiple {@link Lock} objects.
     *
     * @param client     The test client
     * @param maxPermits
     */
    private void acquireAndReleaseMultipleLocks
        (int client,
         LockManager lockManager, int maxPermits) {
        log("Starting client " + client);
        var locks = mLockAPI
            .acquire(lockManager,
                maxPermits);
        log(client + " acquired locks " + locks);
        var result = mLockAPI
            .release(lockManager, locks);
        log(client + " released lock " + result);
    }
}
