import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;
import static utils.MonosCollector.toMono;

/**
 * This class shows how to apply RxJava features asynchronously to
 * perform a range of Observable operations, including fromArray(),
 * map(), flatMap(), collect(), subscribeOn(), and various types of
 * thread pools.  It also shows various Single operations, such as
 * when(), firstWithSignal(), materialize(), flatMap(), flatMapMany(),
 * subscribeOn(), and the parallel thread pool.  
 */
@SuppressWarnings("ALL")
public class FluxEx {
    /**
     * Create a random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * A test of BigFraction multiplication using an asynchronous
     * Observable stream and a blocking Subscriber implementation.
     */
    public static Mono<Void> testFractionMultiplicationsBlockingSubscriber() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationsBlockingSubscriber()\n");

        // Random number generator.
        Random random = new Random();

        // Create an array of BigFraction objects.
        BigFraction[] bigFractionArray = {
            BigFraction.valueOf(100, 3),
            BigFraction.valueOf(100, 4),
            BigFraction.valueOf(100, 2),
            BigFraction.valueOf(100, 1)
        };

        // Create a blocking subscriber.
        BlockingSubscriber blockingSubscriber = new BlockingSubscriber(sb);

        Flux<BigFraction> bfF = Flux
            // Use fromArray() to generate a stream of big
            // fractions.
            .fromArray(bigFractionArray);

        Mono<BigFraction> bfM = Mono
            // Use Mono.fromCallable() to "lazily" generate a random
            // BigFraction.
            .fromCallable(() ->
                          BigFractionUtils.makeBigFraction(random,
                                                           true));

        bfM
            // Transform the item emitted by this Mono into a
            // Publisher and then forward its emissions into the
            // returned Flux.
            .flatMapMany(bf1 -> bfF
                         // Perform the Project Reactor flatMap()
                         // concurrency idiom.
                         .flatMap(bf2 -> Flux
                                  // Emit bf2 to start a new stream.
                                  .just(bf2)
                                  
                                  // Arrange to run each element in
                                  // this Flux stream in parallel.
                                  .subscribeOn(Schedulers.parallel())

                                  // Log contents of the computation.
                                  .doOnNext(bf ->
                                            logBigFraction(bf1, bf2, sb))

                                  // Use map() to multiply each
                                  // element in the stream by the
                                  // value emitted by the mono.
                                  .map(___ -> bf2.multiply(bf1))))

            // Use subscribe() to initiate all the processing and
            // handle the results asynchronously.
            .subscribe(blockingSubscriber);

        // Wait for all the computations to complete.
        blockingSubscriber.await();

        // Return empty mono to indicate to the AsyncTaskBarrier
        // that all the processing is done.
        return sVoidM;
    }

    /**
     * Define a Subscriber implementation that handles blocking, which
     * is otherwise not supported by Project Reactor.
     */
    private static class BlockingSubscriber
        implements Subscriber<BigFraction> {
        /**
         * The calling thread uses this Barrier synchronizer to wait
         * for a subscriber to complete all its async processing.
         */
        final CountDownLatch mLatch;

        /**
         * A StringBuilder used to log the output.
         */
        final StringBuilder mSb;

        /**
         * Constructor initializes the fields.
         */
        BlockingSubscriber(StringBuilder stringBuilder) {
            mLatch = new CountDownLatch(1);
            mSb = stringBuilder;
        }

        /**
         * Block until all events have been processed by subscribe().
         */
        void await() {
            try {
                mLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * Hook method invoked after calling subscribe(subscriber)
         * below.  No data starts flowing until s.request(long) is
         * invoked.
         */
        @Override
        public void onSubscribe(Subscription s) {
            // Disable backpressure.
            s.request(Integer.MAX_VALUE);
        }

        /**
         * Process the next element in the stream.
         * @param bigFraction The next BigFraction in the stream
         */
        @Override
        public void onNext(BigFraction bigFraction) {
            // Add fraction to the string buffer.
            mSb.append(" = " + bigFraction.toMixedString() + "\n");
        }

        /**
         * Handle an error event.
         * @param t The exception that occurred
         */
        @Override
        public void onError(Throwable t) {
            // Append the error message to the
            // StringBuilder.
            mSb.append(t.getMessage());

            // Display results when processing is done.
            BigFractionUtils.display(mSb.toString());

            // Release the latch.
            mLatch.countDown();
        }

        /**
         * Handle final completion event.
         */
        @Override
        public void onComplete() {
            // Display results when processing is done.
            BigFractionUtils.display(mSb.toString());

            // Release the latch.
            mLatch.countDown();
        }
    }

    /**
     * This factory method returns a mono that's signaled after the
     * {@code unreducedFraction} is reduced/multiplied asynchronously
     * in background threads from the given {@code scheduler}.
     */
    private static Mono<BigFraction> reduceAndMultiplyFraction
        (BigFraction unreducedFraction,
         Scheduler scheduler) {
        return Mono
            // Omit one item that performs the reduction.
            .fromCallable(() ->
                          BigFraction.reduce(unreducedFraction))

            // Perform all processing asynchronously in a pool of
            // background threads.
            .subscribeOn(scheduler)

            // Return a mono to a multiplied big fraction.
            .flatMap(reducedFraction -> Mono
                     // Multiply the big fractions
                     .fromCallable(() -> reducedFraction
                                   .multiply(sBigReducedFraction))
                                   
                     // Perform all processing asynchronously in a
                     // pool of background threads.
                     .subscribeOn(scheduler));
    }
}
