package edu.vandy.lockmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.stream.IntStream;

import static edu.vandy.lockmanager.Utils.log;

/**
 * This program tests the features of the {@link LockApplication}
 * microservice, which uses Spring WebMVC to provide a distributed
 * lock manager for Spring applications using quasi-asynchronous
 * Spring {@link DeferredResult} controller methods with a synchronous
 * client.  It also shows how to use the HTTP interface features in
 * Spring framework 6, which enables the definition of declarative
 * HTTP services using Java interfaces.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LockManagerTests {
    /**
     * The auto-wired {@link LockAPI} that accesses the {@link
     * LockApplication} microservice.
     */
    @Autowired
    private LockAPI mLockAPI;

    /**
     * Number of permits to acquire and release simultaneously.
     */
    private static final int MULTIPLE_PERMITS = 2;

    /**
     * The total number of {@link Lock} objects to create.
     */
    private final Integer mMAX_LOCKS = 1;

    /**
     * The total number of times to iterate.
     */
    private final int mMAX_CLIENTS = 2;

    /**
     * Test the {@link LockApplication} microservice's ability to
     * acquire and release single locks.
     */
    @Test
    public void testSingleAcquireAndRelease() {
        log("testSingleAcquireAndRelease() started");

        // Create a Lock manager containing mMAX_LOCKS.
        mLockAPI.create(mMAX_LOCKS);

        // Run mMAX_ITERATIONS tests in parallel.
        IntStream
            // Perform mMAX_CLIENT.
            .range(0, mMAX_CLIENTS)

            // Run the operations in parallel.
            .parallel()

            // Perform this action each iteration.
            .forEach(this::acquireAndReleaseSingleLocks);

        log("testSingleAcquireAndRelease() finished");
    }

    /**
     * Test the {@link LockApplication} microservice's ability to
     * acquire and release multiple locks.
     */
    // @Test
    public void testMultipleAcquireAndRelease() {
        log("testMultipleAcquireAndRelease() started");

        // Create a Lock manager containing mMAX_LOCKS.
        mLockAPI.create(mMAX_LOCKS * 2);

        IntStream
            // Run mMAX_ITERATIONS tests.
            .range(0, mMAX_CLIENTS)

            // Run the operations in parallel.
            .parallel()

            // Call acquireAndReleaseLocks() each iteration.
            .forEach(this::acquireAndReleaseMultipleLocks);

        log("testMultipleAcquireAndRelease() finished");
    }

    /**
     * Acquire and release single {@link Lock} objects.
     *
     * @param iteration The curren test iteration
     */
    private void acquireAndReleaseSingleLocks(int iteration) {
        log("iteration " + iteration);
        var lock = mLockAPI.acquire();
        log("acquired lock " + lock);
        mLockAPI.release(lock);
    }

    /**
     * Acquire and release multiple {@link Lock} objects.
     *
     * @param iteration The curren test iteration
     */
    private void acquireAndReleaseMultipleLocks(int iteration) {
        log("iteration " + iteration);
        var locks = mLockAPI.acquire(MULTIPLE_PERMITS);
        log("acquired locks " + locks);
        mLockAPI.release(locks);
        log("released locks");
    }
}
