import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;
import utils.BlockingSubscriber;

import java.util.Random;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;
import static utils.SinglesCollector.toSingle;

/**
 * This class shows how to apply RxJava features asynchronously to
 * perform a range of Observable operations, including fromArray(),
 * just(), doOnNext(), map(), flatMap(), subscribeOn(), toFlowable()
 * and a parallel thread pool.  It also shows the Flowable subscribe()
 * operation.  In addition it shows various Single operations, such as
 * zipArray(), ambArray(), flatMap(), flatMapObservable(),
 * ignoreElement(), subscribeOn(), and a parallel thread pool.  In
 * addition, it demonstrates how to combine the Java Streams framework
 * with the RxJava framework.
 */
public class ObservableEx {
    /**
     * Create a random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * Test BigFraction multiplications by combining the Java streams
     * framework with the RxJava framework.
     */
    public static Completable testFractionMultiplicationsStreams() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationsStreams()\n");

        sb.append("     Printing sorted results:");

        // Process the function in a sequential stream.
        return Stream
            // Generate a stream of random, large, and unreduced big
            // fractions.
            .generate(() ->
                      makeBigFraction(new Random(),
                                      false))

            // Stop after generating sMAX_FRACTIONS big fractions.
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(unreducedBigFraction ->
                 reduceAndMultiplyFraction(unreducedBigFraction,
                                           Schedulers.computation()))

            // Trigger intermediate operation processing and return a
            // Single to a list of big fractions that are being
            // reduced and multiplied asynchronously.
            .collect(toSingle())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .flatMap(list -> BigFractionUtils
                     .sortAndPrintList(list, sb))

            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }

    /**
     * A test of BigFraction multiplication using an asynchronous
     * Observable stream and a blocking Subscriber implementation.
     */
    public static Completable testFractionMultiplicationsBlockingSubscriber() {
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

        Observable<BigFraction> bfF = Observable
            // Use fromArray() to generate a stream of big
            // fractions.
            .fromArray(bigFractionArray);

        Single<BigFraction> bFS = Single
            // Use Single.fromCallable() to "lazily" generate a random
            // BigFraction.
            .fromCallable(() -> BigFractionUtils
                          .makeBigFraction(random,
                                           true));

        bFS
            // Transform the item emitted by this Single into a
            // Publisher and then forward its emissions into the
            // returned Observable.
            .flatMapObservable(bf1 -> bfF
                               // Perform the Project Reactor
                               // flatMap() concurrency idiom.
                               .flatMap(bf2 -> Observable
                                        // Emit bf2 to start a new
                                        // stream.
                                        .just(bf2)

                                        // Arrange to run each element
                                        // in this Flux stream in
                                        // parallel.
                                        .subscribeOn(Schedulers.computation())

                                        // Log contents of the
                                        // computation.
                                        .doOnNext(bf ->
                                                  logBigFraction(bf1, bf2, sb))

                                        // Use map() to multiply each
                                        // element in the stream by
                                        // the value emitted by the
                                        // Single.
                                        .map(___ -> bf2.multiply(bf1))))

            // Convert the Observable to a Flowable so that
            // subscribe() works with a Subscriber (instead of an
            // Observer, which is all Observable provides).
            .toFlowable(BackpressureStrategy.LATEST)

            // Use subscribe() to initiate all the processing and
            // handle the results asynchronously.
            .subscribe(blockingSubscriber);

        // Wait for all the computations to complete.
        blockingSubscriber.await();

        // Return a Completable to indicate to the AsyncTaskBarrier
        // that all the processing is done.
        return sVoidC;
    }

    /**
     * This factory method returns a Single that's signaled after the
     * {@code unreducedFraction} is reduced/multiplied asynchronously
     * in background threads from the given {@code scheduler}.
     */
    private static Single<BigFraction> reduceAndMultiplyFraction
        (BigFraction unreducedFraction,
         Scheduler scheduler) {
        return Single
            // Omit one item that performs the reduction.
            .fromCallable(() ->
                          BigFraction.reduce(unreducedFraction))

            // Perform all processing asynchronously in a pool of
            // background threads.
            .subscribeOn(scheduler)

            // Return a Single to a multiplied big fraction.
            .flatMap(reducedFraction -> Single
                     // Multiply the big fractions
                     .fromCallable(() -> reducedFraction
                                   .multiply(sBigReducedFraction))
                                   
                     // Perform all processing asynchronously in a
                     // pool of background threads.
                     .subscribeOn(scheduler));
    }
}
