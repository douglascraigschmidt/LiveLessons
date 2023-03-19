package edu.vandy.lockmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
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
     * The total number of {@link Lock} objects to create.
     */
    private final Integer mMAX_LOCKS = 1;

    /**
     * The total number of times to iterate.
     */
    private final int mMAX_ITERATIONS = 2;

    /**
     * Test the {@link LockApplication} microservice.
     */
    @Test
    public void testLockApplication() {
        log("testLockApplication() started");

        // Create a Lock manager containing mMAX_LOCKS.
        mLockAPI.create(mMAX_LOCKS);

        // Run mMAX_ITERATIONS tests in parallel.
        IntStream
            .range(0, mMAX_ITERATIONS)
            .parallel()
            .forEach(iteration -> acquireAndReleaseLocks());

        log("testLockApplication() finished");
    }

    /**
     * Acquire and release {@link Lock} objects.
     */
    private void acquireAndReleaseLocks() {
        // Iterate for multiple iterations.
        for (int i = 0; i < mMAX_ITERATIONS; i++) {
            var lock = mLockAPI.acquire();
            log("acquired lock " + lock.id);
            mLockAPI.release(lock);
        }
    }
}
