package edu.vandy.lockmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static edu.vandy.lockmanager.Utils.log;

/**
 * This program tests the features of the {@link LockApplication}
 * microservice, which uses Spring WebFlux to provide a distributed
 * lock manager for Spring applications using an asynchronous Spring
 * controller method that returns a Mono reactive type.  It also shows
 * how to use the HTTP interface features in Spring framework 6, which
 * enables the definition of declarative HTTP services using Java
 * interfaces.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LockManagerTests {
    /**
     * Number of permits to acquire and release simultaneously.
     */
    private static final int MULTIPLE_PERMITS = 2;

    /**
     * The auto-wired {@link LockAPI} that accesses the {@link
     * LockApplication} microservice.
     */
    @Autowired
    private LockAPI mLockAPI;

    /**
     * The total number of {@link Lock} objects to create.
     */
    private final Integer mMAX_LOCKS = 4;

    /**
     * The total number of times to iterate.
     */
    private final int mMAX_ITERATIONS = 2;

    /**
     * Test the {@link LockApplication} microservice.
     */
    // @Test
    public void testSingleAcquireAndRelease() {
        log("testSingleAcquireAndRelease() started");

        // Create a Lock manager containing mMAX_LOCKS.
        mLockAPI.create(mMAX_LOCKS);

        Flux
            // Run mMAX_ITERATIONS tests.
            .range(0, mMAX_ITERATIONS)

            // Call acquireAndReleaseLocks() each iteration.
            .flatMap(this::acquireAndReleaseSingleLocks)

            // Collect the results into a List<Void>.
            .collectList()

            // Block until all async processing is done.
            .block();

        log("testSingleAcquireAndRelease() finished");
    }

    /**
     * Test the {@link LockApplication} microservice.
     */
    @Test
    public void testMultipleAcquireAndRelease() {
        log("testMultipleAcquireAndRelease() started");

        // Create a Lock manager containing mMAX_LOCKS.
        mLockAPI.create(mMAX_LOCKS);

        Flux
            // Run mMAX_ITERATIONS tests.
            .range(0, mMAX_ITERATIONS)

            // Call acquireAndReleaseLocks() each iteration.
            .flatMap(this::acquireAndReleaseMultipleLocks)

            // Collect the results into a List<Void>.
            .collectList()

            // Block until all async processing is done.
            .block();

        log("testSingleAcquireAndRelease() finished");
    }

    /**
     * Acquire and release {@link Lock} objects.
     *
     * @param iteration The curren test iteration
     */
    private Mono<Void> acquireAndReleaseSingleLocks
        (Integer iteration) {
        log("Starting iteration "
            + iteration);
        return mLockAPI
            // Acquire a lock asynchronously.
            .acquire()

            // Display the lock when it's acquired.
            .doOnSuccess(lock ->
                log(iteration + " acquired lock " + lock))

            // Release the lock asynchronously.
            .flatMap(lock -> mLockAPI
                .release(lock))

            // Log any exceptions.
            .doOnError(exception ->
                log("exception = " + exception.getMessage()))

            // Indicate that we're finished running asynchronously.
            .then();
    }

    /**
     * Acquire and release {@link Lock} objects.
     *
     * @param iteration The curren test iteration
     */
    private Mono<Void> acquireAndReleaseMultipleLocks
        (Integer iteration) {
        log("Starting iteration "
            + iteration);
        var locks = mLockAPI
            // Acquire a lock asynchronously.
            .acquire(MULTIPLE_PERMITS)

            // Display the lock when it's acquired.
            .doOnNext(lock ->
                log(iteration + " acquired lock " + lock))
            .collectList()
            .block();


        return mLockAPI
            // Release all the locks.
            .release(locks)

            // Log any exceptions.
            .doOnError(exception ->
                log("exception = " + exception.getMessage()))

            // Indicate that we're finished running asynchronously.
            .then();
    }
}
