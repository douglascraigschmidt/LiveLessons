package edu.vandy.lockmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

        Flux
            // Run mMAX_CLIENTS tests.
            .range(0, mMAX_CLIENTS)

            // Call acquireAndReleaseLocks() each iteration.
            .flatMap(this::acquireAndReleaseSingleLocks)

            // Collect the results into a List<Void>.
            .collectList()

            // Block until all async processing is done.
            .block();

        log("testSingleAcquireAndRelease() finished");
    }

    /**
     * Test the {@link LockApplication} microservice's ability to
     * acquire and release multiple locks.
     */
    @Test
    public void testMultipleAcquireAndRelease() {
        log("testMultipleAcquireAndRelease() started");

        // Create a Lock manager containing mMAX_LOCKS.
        mLockAPI.create(mMAX_LOCKS);

        Flux
            // Run mMAX_ITERATIONS tests.
            .range(0, mMAX_CLIENTS)

            // Call acquireAndReleaseLocks() each iteration.
            .flatMap(this::acquireAndReleaseMultipleLocks)

            // Collect the results into a List<Void>.
            .collectList()

            // Block until all async processing is done.
            .block();

        log("testMultipleAcquireAndRelease() finished");
    }

    /**
     * Acquire and release single {@link Lock} objects.
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
     * Acquire and release multiple {@link Lock} objects.
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

            // Collect the Flux contents into a List.
            .collectList()

            // Block until all locks are acquired.
            .block();

        return mLockAPI
            // Release all the locks at once.
            .release(locks)

            // Log any exceptions.
            .doOnError(exception ->
                log("exception = " + exception.getMessage()))

            // Indicate that we're finished running asynchronously.
            .then();
    }
}
