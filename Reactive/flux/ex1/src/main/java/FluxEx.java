import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply Project Reactor features
 * synchronously and asynchronously to perform basic Flux operations,
 * including just(), fromIterable(), fromArray(), from(), doOnNext(),
 * map(), mergeWith(), repeat(), subscribeOn(), and subscribe().  Also
 * shows how to implement a blocking subscriber in Project Reactor.
 */
@SuppressWarnings("ALL")
public class FluxEx {
    /**
     * Test BigFraction multiplication using a synchronous Flux
     * stream.
     */
    public static Mono<Void> testFractionMultiplicationSync1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationSync1()\n");

        Flux
            // Use just() to generate a stream of big fractions.
            .just(BigFraction.valueOf(100, 3),
                  BigFraction.valueOf(100, 4),
                  BigFraction.valueOf(100, 2),
                  BigFraction.valueOf(100, 1))

            // Log the contents of the computation.
            .doOnNext(bigFraction -> 
                      sb.append("    ["
                                + Thread.currentThread().getId()
                                + "] "
                                + bigFraction.toMixedString()
                                + " x "
                                + sBigReducedFraction.toMixedString()))

            // Use map() to multiply each element in the stream by a
            // constant.
            .map(bigFraction -> 
                 // Multiply and return result.
                 bigFraction.multiply(sBigReducedFraction))

            // Use subscribe() to initiate all the processing and
            // handle the results.  This call runs synchronously since
            // the publisher (just()) is synchronous and runs in the
            // calling thread.  However, there are more interesting
            // types of publishers that enable asynchrony.
            .subscribe(// Handle next event.
                       multipliedBigFraction ->
                       // Add fraction to the string buffer.
                       sb.append(" = " 
                                 + multipliedBigFraction.toMixedString() 
                                 + "\n"),

                       // Handle error result event.
                       error -> sb.append(error.getMessage()),

                       // Handle final completion event.
                       () ->
                       // Display results when processing is done.
                       BigFractionUtils.display(sb.toString()));

        // Return empty mono to indicate to the AsyncTaskBarrier that
        // all the processing is done.
        return sVoidM;
    }

    /**
     * Another test of BigFraction multiplication using a synchronous
     * Flux stream and several local variables.
     */
    public static Mono<Void> testFractionMultiplicationSync2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationSync2()\n");

        // Create a list of BigFraction objects.
        List<BigFraction> bigFractionList = List.of
            (BigFraction.valueOf(100, 3),
             BigFraction.valueOf(100, 4),
             BigFraction.valueOf(100, 2),
             BigFraction.valueOf(100, 1));

        Flux
            // Use fromIterable() to generate a stream of big
            // fractions.
            .fromIterable(bigFractionList)

            // Log the contents of the computation.
            .doOnNext(bf -> logBigFraction(sBigReducedFraction, bf, sb))

            // Use map() to multiply each element in the stream by a
            // constant.
            .map(fraction -> fraction.multiply(sBigReducedFraction))

            // Use subscribe() to initiate all the processing and
            // handle the results.  This call runs synchronously since
            // the publisher (fromIterable()) is synchronous and runs
            // in the calling thread.  However, there are more
            // interesting types of publishers that enable asynchrony.
            .subscribe(// Handle next event.
                       multipliedBigFraction ->
                       // Add fraction to the string buffer.
                       sb.append(" = " 
                                 + multipliedBigFraction.toMixedString() 
                                 + "\n"),

                       // Handle error result event.
                       error -> sb.append(error.getMessage()),

                       // Handle final completion event.
                       () ->
                       // Display results when processing is done.
                       BigFractionUtils.display(sb.toString()));

        // Return empty mono to indicate to the AsyncTaskBarrier that
        // all the processing is done.
        return sVoidM;
    }

    /**
     * A test of BigFraction multiplication using an asynchronous Flux
     * stream and a Subscriber implementation.
     */
    public static Mono<Void> testFractionMultiplicationAsync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationAsync()\n");

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

        Flux<BigFraction> f1 = Flux
            // Use fromArray() to generate a stream of big
            // fractions.
            .fromArray(bigFractionArray);

        Flux<BigFraction> f2 = Flux
            // Use from() and Mono.fromCallable() to "lazily" generate a
            // stream of random big fractions.
            .from(Mono.fromCallable(() ->
                                    BigFractionUtils.makeBigFraction(random,
                                                                     true)))

            // Generate random big fractions 4 times.
            .repeat(4);

        f1
            // Flatten Flux f1 and f2 into a single Flux sequence,
            // without any transformations.
            .mergeWith(f2)    

            // Arrange to run this Flux stream in a background
            // thread.
            .subscribeOn(Schedulers.single())

            // Log the contents of the computation.
            .doOnNext(bf -> logBigFraction(sBigReducedFraction, bf, sb))

            // Use map() to multiply each element in the stream by
            // a constant.
            .map(fraction -> fraction.multiply(sBigReducedFraction))

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
}
