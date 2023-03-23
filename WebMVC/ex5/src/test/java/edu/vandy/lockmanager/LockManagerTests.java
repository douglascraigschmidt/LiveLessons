package edu.vandy.lockmanager;

import edu.vandy.lockmanager.common.Lock;
import edu.vandy.lockmanager.server.LockManagerApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.stream.IntStream;

import static edu.vandy.lockmanager.utils.Logger.log;

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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
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
     * This method runs before each test to initialize the
     * LockManager.
     */
    @BeforeEach
    public void initializeTests() {
        // Create a Lock manager containing mMAX_LOCKS.
        mLockAPI.create(sMAX_LOCKS);
    }

    /**
     * Test the {@link LockManagerApplication} microservice's ability
     * to acquire and release single locks concurrently.
     */
    @Test
    public void testSingleAcquireAndRelease() {
        log("testSingleAcquireAndRelease() started");

        // Run mMAX_CLIENTS tests in parallel.
        IntStream
            // Perform mMAX_CLIENTS tests.
            .range(0, sMAX_CLIENTS)

            // Run each test in parallel.
            .parallel()

            // Perform this action for each client.
            .forEach(this::acquireAndReleaseSingleLocks);

        log("testSingleAcquireAndRelease() finished");
    }

    /**
     * Test the {@link LockManagerApplication} microservice's ability
     * to acquire and release multiple locks concurrently.
     */
    @Test
    public void testMultipleAcquireAndRelease() {
        log("testMultipleAcquireAndRelease() started");

        IntStream
            // Run mMAX_CLIENTS tests.
            .range(0, sMAX_CLIENTS)

            // Run each test in parallel.
            .parallel()

            // Perform this action for each client.
            .forEach(this::acquireAndReleaseMultipleLocks);

        log("testMultipleAcquireAndRelease() finished");
    }

    /**
     * Acquire and release single {@link Lock} objects.
     *
     * @param client The test client
     */
    private void acquireAndReleaseSingleLocks(int client) {
        log("client " + client);
        var lock = mLockAPI.acquire();
        log("acquired lock " + lock);
        mLockAPI.release(lock);
    }

    /**
     * Acquire and release multiple {@link Lock} objects.
     *
     * @param client The test client
     */
    private void acquireAndReleaseMultipleLocks(int client) {
        log("client " + client);
        var locks = mLockAPI.acquire(sMULTIPLE_PERMITS);
        log("acquired locks " + locks);
        mLockAPI.release(locks);
        log("released locks");
    }
}
