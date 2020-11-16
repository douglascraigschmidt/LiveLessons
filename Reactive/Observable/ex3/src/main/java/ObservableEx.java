import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static utils.BigFractionUtils.sBigReducedFraction;
import static utils.BigFractionUtils.sMAX_FRACTIONS;

/**
 * This class shows how to reduce and/or multiply big fractions
 * asynchronously and concurrently using many advanced RxJava
 * Observable operations, including fromIterable(), map(), create(),
 * flatMap(), flatMapCompletable(), filter(), collectInto(),
 * subscribeOn(), take(), and various types of thread pools.  It also
 * shows advanced RxJava Single operations, such as first(), when(),
 * flatMap(), subscribeOn(), and the parallel thread pool.
 */
@SuppressWarnings({"StringConcatenationInsideStringBufferAppend", "ResultOfMethodCallIgnored"})
public class ObservableEx {
    /**
     * Create a random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * Test BigFraction exception handling using an asynchronous Observable
     * stream and a pool of threads.
     */
    public static Completable testFractionExceptions() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionExceptions1()\n");

        // Create a function to handle an ArithmeticException.
        Function<Throwable,
            ? extends BigFraction> errorHandler = t -> {
            // If exception occurred return 0.
            sb.append("     exception = "
                      + t.getMessage()
                      + "\n");

            // Convert error to 0.
            return BigFraction.ZERO;
        };

        // Create a function that multiplies big fractions.
        Function<BigFraction,
            BigFraction> multiplyBigFractions = fraction -> {
            sb.append("     "
                      + fraction.toMixedString()
                      + " x "
                      + sBigReducedFraction.toMixedString()
                      + "\n");
            // When Single completes multiply it.
            return fraction.multiply(sBigReducedFraction);
        };

        // Create a list of denominators, including 0 that will
        // trigger an ArithmeticException.
        List<Integer> denominators = List.of(3, 4, 2, 0, 1);

        return Observable
            // Use an Observable to generate a stream from the
            // denominators list.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#fromIterable-java.lang.Iterable-
            .fromIterable(denominators)

            // Iterate thru the elements using RxJava's flatMap()
            // concurrency idiom to reduce and multiply big fractions
            // asynchronously in a thread pool.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#flatMap-io.reactivex.rxjava3.functions.Function-
            .flatMap(denominator -> {
                    // Create/process each denominator asynchronously
                    // via an "inner publisher".
                    return Observable
                        .fromCallable(() ->
                                      // Throws ArithmeticException if
                                      // denominator is 0.
                                      BigFraction.valueOf(Math.abs(sRANDOM.nextInt()),
                                                          denominator))

                        // Run all the processing in a pool of
                        // background threads.
                        .subscribeOn(Schedulers.computation())

                        // Convert ArithmeticException to 0.
                        .onErrorReturn(errorHandler)

                        // Perform a multiplication.
                        .map(multiplyBigFractions);
                })

            // Remove any big fractions that are <= 0.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#filter-io.reactivex.rxjava3.functions.Predicate-
            .filter(fraction -> fraction.compareTo(0) > 0)

            // Collect the results into an ArrayList.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#collectInto-U-io.reactivex.rxjava3.functions.BiConsumer-
            .collectInto(new ArrayList<BigFraction>(), List::add)

            // Process the ArrayList and return a Completable that
            // synchronizes with the AsyncTester framework.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#flatMapCompletable-io.reactivex.rxjava3.functions.Function-
            .flatMapCompletable(list ->
                                // Sort/print results after all async
                                // fraction operations complete.
                                BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Test BigFraction reductions/multiplications using an Observable
     * and its operations, including create(), flatMap(),
     * collectInto(), flatMapComplete(), and ambArray().
     */
    public static Completable testFractionMultiplications1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications1()\n");

        sb.append("     Printing sorted results:");

        // Process the function in a observable stream.
        return Observable
            // Emit a stream of random unreduced big fractions.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#create-io.reactivex.rxjava3.core.ObservableOnSubscribe-
            .create(ObservableEx::bigFractionEmitter)

            // Iterate thru the elements using RxJava's flatMap()
            // concurrency idiom to reduce and multiply these
            // fractions asynchronously in a thread pool.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#flatMap-io.reactivex.rxjava3.functions.Function-
            .flatMap(unreducedFraction ->
                     reduceAndMultiplyFraction(unreducedFraction,
                                               Schedulers.computation()))

            // Collect the results into an ArrayList.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#collectInto-U-io.reactivex.rxjava3.functions.BiConsumer-
            .collectInto(new ArrayList<BigFraction>(), List::add)

            // Process the ArrayList and return a Completable that
            // synchronizes with the AsyncTester framework.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#flatMapCompletable-io.reactivex.rxjava3.functions.Function-
            .flatMapCompletable(list ->
                                // Sort/print results after all async
                                // fraction operations complete.
                                BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Emit a stream of random unreduced big fractions.
     */
    private static void bigFractionEmitter(ObservableEmitter<BigFraction> emitter) {
        Observable
            // Generate sMAX_FRACTIONS.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#range-int-int-
            .range(1, sMAX_FRACTIONS)

            // Emit random numbers until the range is complete.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#subscribe-io.reactivex.rxjava3.functions.Consumer-io.reactivex.rxjava3.functions.Consumer-io.reactivex.rxjava3.functions.Action-
            .subscribe(__ -> emitter
                       // Generate a random unreduced big fraction.
                       .onNext(BigFractionUtils.makeBigFraction(sRANDOM,
                                                                false)),
                       t -> emitter.onComplete(),
                       emitter::onComplete);
    }

    /**
     * This factory method returns an Observable that's signaled after the
     * {@code unreducedFraction} is reduced/multiplied asynchronously
     * in background threads from the given {@code scheduler}.
     */
    private static Observable<BigFraction> reduceAndMultiplyFraction
        (BigFraction unreducedFraction,
         Scheduler scheduler) {
        return Observable
            // Omit one item that performs the reduction.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#fromCallable-java.util.concurrent.Callable-
            .fromCallable(() ->
                          BigFraction.reduce(unreducedFraction))

            // Perform all processing asynchronously in a pool of
            // background threads.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#subscribeOn-io.reactivex.rxjava3.core.Scheduler-
            .subscribeOn(scheduler)

            // Return an Observable to a multiplied big fraction.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#flatMap-io.reactivex.rxjava3.functions.Function-
            .flatMap(reducedFraction -> Observable
                     // Multiply the big fraction

                     .fromCallable(() -> reducedFraction
                                   .multiply(sBigReducedFraction))
                                   
                     // Perform all processing asynchronously in a
                     // pool of background threads.
                     .subscribeOn(scheduler));
    }
}
