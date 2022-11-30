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
 * fromCallable(), doOnNext(), map(), flatMap(), subscribeOn(),
 * toFlowable(), subscribe(), and a parallel thread pool.  It also
 * shows the Flowable subscribe() operation.  In addition it shows
 * various Single operations, such as zipArray(), ambArray(),
 * subscribeOn(), flatMapObservable(), flatMapCompletable(),
 * ignoreElement(), flatMap(), and a parallel thread pool.  It also
 * shows how to combine the Java Streams framework with the RxJava
 * framework.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ObservableEx {
    /**
     * Create a random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * A test of BigFraction multiplication using an asynchronous
     * Observable stream and a blocking Subscriber implementation.
     */
    public static Completable testFractionMultiplicationsBlockingSubscriber() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationsBlockingSubscriber()\n");

        // Add some useful diagnostic output.
        sb.append("["
                  + Thread.currentThread().getId()
                  + "] "
                  + " Starting async processing.\n");

        // Create an array of BigFraction objects.
        BigFraction[] bigFractionArray = {
            BigFraction.valueOf(100, 3),
            BigFraction.valueOf(100, 4),
            BigFraction.valueOf(100, 2),
            BigFraction.valueOf(100, 1)
        };

        // Create a blocking subscriber that processes various
        // types of signals.
        BlockingSubscriber<BigFraction> blockingSubscriber = 
            new BlockingSubscriber<>
            (bf ->
             // Add fraction to the string buffer.
             sb.append("Result = " + bf.toMixedString() + "\n"),
             t -> {
                // Append the error message to the StringBuilder.
                sb.append(t.getMessage());

                // Display results when processing is done.
                BigFractionUtils.display(sb.toString());
             },
             () -> {
                 // Add some useful diagnostic output.
                 sb.append("["
                           + Thread.currentThread().getId()
                           + "] "
                           + " Async computations complete.\n");
                 // Display results when processing is done.
                 BigFractionUtils.display(sb.toString());
             },
             Long.MAX_VALUE);

        Single
            // Generate a random large BigFraction.
            .fromCallable(() -> BigFractionUtils
                          .makeBigFraction(sRANDOM, true))

            // Transform the item emitted by this Single into a
            // Publisher and then forward its emissions into the
            // returned Observable.
            .flatMapObservable(bf1 -> Observable
                               // Generate a stream of BigFractions.
                               .fromArray(bigFractionArray)

                               // Perform the RxJava flatMap()
                               // concurrency idiom.
                               .flatMap(bf2 -> Observable
                                        // Multiply bf1 by each value bf1.
                                        .fromCallable(() -> bf2.multiply(bf1)))

                                        // Arrange to run each element
                                        // in parallel.
                                        .subscribeOn(Schedulers.computation()))

            // Convert Observable to Flowable so subscribe() works
            // with a Subscriber (instead of an Observer, which is all
            // that Observable provides).
            .toFlowable(BackpressureStrategy.BUFFER)

            // Use subscribe() to initiate all processing and handle
            // results asynchronously.
            .subscribe(blockingSubscriber);

        // Wait for computations to complete and return a Completable
        // to inform the AsyncTaskBarrier that all processing is done.
        return blockingSubscriber.await();
    }

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
            .generate(() -> makeBigFraction(sRANDOM, false))

            // Stop after generating sMAX_FRACTIONS big fractions.
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(unreducedBigFraction ->
                 reduceAndMultiplyFraction(unreducedBigFraction,
                                           Schedulers.computation(),
                                           sb))

            // Trigger intermediate operation processing and return a
            // Single to a list of big fractions that are being
            // reduced and multiplied asynchronously.
            .collect(toSingle())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .flatMapCompletable(list -> BigFractionUtils
                                .sortAndPrintList(list, sb));
    }

    /**
     * @return A {@link Single} that's signaled after the
     * {@code unreducedFraction} is reduced/multiplied asynchronously
     * in background threads from the given {@link Scheduler}.
     */
    private static Single<BigFraction> reduceAndMultiplyFraction
        (BigFraction unreducedFraction,
         Scheduler scheduler,
         StringBuffer sb) {
        return Single
            // Omit one item that performs the reduction.
            .fromCallable(() -> BigFraction
                          .reduce(unreducedFraction))

            // Perform all processing asynchronously in a pool of
            // background threads.
            .subscribeOn(scheduler)

            // Return a Single to a multiplied big fraction.
            .flatMap(reducedFraction ->
                     multiplyFraction(reducedFraction,
                                      scheduler,
                                      sb));
    }

    /**
     * @return A {@link Single} that's signaled after the {@link
     * BigFraction} is multiplied asynchronously in a background
     * thread from the given {@link Scheduler}
     */
    private static Single<BigFraction> multiplyFraction(BigFraction bigFraction,
                                                        Scheduler scheduler,
                                                        StringBuffer sb) {
        return Single
            // Return a Single to a multiplied big fraction.
            .fromCallable(() -> bigFraction
                          // Multiply the big fractions
                          .multiply(sBigReducedFraction))

            // Perform processing asynchronously in a pool of
            // background threads.
            .subscribeOn(scheduler);
    }
}
