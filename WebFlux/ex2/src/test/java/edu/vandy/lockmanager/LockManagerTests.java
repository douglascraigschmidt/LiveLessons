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

        Flux
            // Run mMAX_ITERATIONS tests.
            .range(0, mMAX_ITERATIONS)

            // Call acquireAndReleaseLocks() each iteration.
            .flatMap(this::acquireAndReleaseLocks)

            // Collect the results into a List<Void>.
            .collectList()

            // Block until all async processing is done.
            .block();

        log("testLockApplication() finished");
    }

    /**
     * Acquire and release {@link Lock} objects.
     *
     * @param iteration The curren test iteration
     */
    private Mono<Void> acquireAndReleaseLocks
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
}
