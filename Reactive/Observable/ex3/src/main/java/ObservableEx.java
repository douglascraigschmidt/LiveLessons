import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Consumer;
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
 * generate(), take(), flatMap(), flatMapCompletable(),
 * fromCallable(), filter(), reduce(), collectInto(), subscribeOn(),
 * onErrorReturn(), and Schedulers.computation().  It also shows
 * advanced RxJava Single and Maybe operations, such as ambArray(),
 * subscribeOn(), and doOnSuccess().
 */
@SuppressWarnings({"StringConcatenationInsideStringBufferAppend", "ResultOfMethodCallIgnored"})
public class ObservableEx {
    /**
     * Create a random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * Use an asynchronous Observable stream and a pool of threads to
     * showcase exception handling of BigFraction objects.
     */
    public static Completable testFractionExceptions() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions()\n");

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

        // Create a Function that multiplies big fractions.
        Function<BigFraction, BigFraction> multiplyBigFractions = bf -> {
            // Multiply bf by a constant.
            BigFraction result = bf.multiply(sBigReducedFraction);

            sb.append("     "
                      + bf.toMixedString()
                      + " x "
                      + sBigReducedFraction.toMixedString()
                      + " = "
                      + result.toMixedString()
                      + "\n");
            return result;
        };

        // Create a list of denominators, including 0 that will
        // trigger an ArithmeticException.
        List<Integer> denominators = List.of(3, 4, 2, 0, 1);

        return Observable
            // Use an Observable to generate a stream from the
            // denominators list.
            .fromIterable(denominators)

            // Iterate thru the elements using RxJava's flatMap()
            // concurrency idiom to reduce and multiply big fractions
            // asynchronously in the computation thread pool.
            .flatMap(denominator -> Observable
                     // Create/process each denominator asynchronously
                     // via an "inner publisher" that runs in a
                     // background thread from the given scheduler.
                     .fromCallable(() ->
                                   // Emit a random BigFraction, but
                                   // will throw ArithmeticException
                                   // if denominator is 0.
                                   BigFraction.valueOf(Math.abs(sRANDOM.nextInt()),
                                                       denominator))

                     // Perform processing asynchronously in a
                     // background thread from the computation
                     // scheduler.
                     .subscribeOn(Schedulers.computation())

                     // Convert ArithmeticException to 0.
                     .onErrorReturn(errorHandler)

                     // Perform a multiplication.
                     .map(multiplyBigFractions))

            // Remove any big fractions that are <= 0.
            .filter(fraction -> fraction.compareTo(0) > 0)

            // Collect the non-0 results into an ArrayList.
            .collectInto(new ArrayList<BigFraction>(), List::add)

            // Process the ArrayList and return a Completable that
            // synchronizes with the AsyncTester framework.
            .flatMapCompletable(list ->
                                // Sort/print results after all async
                                // fraction operations complete.
                                BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Use an asynchronous Observable stream and a pool of threads to
     * perform BigFraction reductions and multiplications.
     */
    public static Completable testFractionMultiplications1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications1()\n");

        sb.append("     Printing sorted results:");

        // Emit a random unreduced big fraction.
        Consumer<Emitter<BigFraction>> emitter = emit ->
            emit.onNext(BigFractionUtils.makeBigFraction(sRANDOM,false));

        // Process the function in a observable stream.
        return Observable
            // Generate a stream of random unreduced big fractions.
            .generate(emitter)

            // Limit the number of generated random unreduced big
            // fractions.
            .take(sMAX_FRACTIONS)

            // Iterate thru the elements using RxJava's flatMap()
            // concurrency idiom to reduce and multiply each fraction
            // asynchronously in the computation thread pool.
            .flatMap(unreducedFraction ->
                     reduceAndMultiplyFraction(unreducedFraction,
                                               Schedulers.computation()))

            // Collect the results into an ArrayList.
            .collect(ArrayList<BigFraction>::new, List::add)

            // Process the ArrayList and return a Completable that
            // synchronizes with the AsyncTester framework.
            .flatMapCompletable(list ->
                                // Sort/print results after all async
                                // fraction operations complete.
                                BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Use an asynchronous Observable stream and a pool of threads to
     * perform BigFraction multiplications and additions.
     */
    public static Completable testFractionMultiplications2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications2()\n");

        // Create a list of BigFraction objects.
        List<BigFraction> bigFractions = List.of(BigFraction.valueOf(1000, 30),
                                                 BigFraction.valueOf(1000, 40),
                                                 BigFraction.valueOf(1000, 20),
                                                 BigFraction.valueOf(1000, 10));

        // Create a Function that multiplies big fractions.
        Function<BigFraction, BigFraction> multiplyBigFractions = bf -> {
            // Multiply bf by a constant.
            BigFraction result = bf.multiply(sBigReducedFraction);

            sb.append("     "
                      + bf.toMixedString()
                      + " x "
                      + sBigReducedFraction.toMixedString()
                      + " = "
                      + result.toMixedString()
                      + "\n");
            return result;
        };

        // Display the results.
        Consumer<? super BigFraction> displayResults = result -> {
            sb.append("    sum of BigFractions = "
                    + result
                    + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return Observable
            // Emit a stream of big fractions.
            .fromIterable(bigFractions)

            // Iterate thru the elements using RxJava's flatMap()
            // concurrency idiom to multiply these big fractions
            // asynchronously in a thread pool.
            .flatMap(bf -> Observable
                     // Create/process each BigFraction asynchronously
                     // via an "inner publisher".  However, just()
                     // emits the BigFraction in the "assembly thread"
                     // and *not* a background thread.
                     .just(bf)
                     
                     // Perform the processing asynchronously in a
                     // background thread from the given scheduler.
                     .subscribeOn(Schedulers.computation())

                     // Perform the multiplication in a background
                     // thread.
                     .map(multiplyBigFractions))

            // Reduce the results into one Maybe<BigFraction>.
            .reduce(BigFraction::add)

            // Display the results if all goes well.
            .doOnSuccess(displayResults)

            // Return a Completable to synchronize with the
            // AsyncTester framework.
            .ignoreElement();
    }

    /**
     * Emit a stream of random unreduced big fractions.
     */
    private static void bigFractionEmitter(ObservableEmitter<BigFraction> emitter) {
        Observable
            // Generate sMAX_FRACTIONS.
            .range(1, sMAX_FRACTIONS)

            // Emit random numbers until the range is complete.
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
            // Omit one item that performs the reduction, which runs
            // in a background thread.
            .fromCallable(() ->
                          BigFraction.reduce(unreducedFraction))

            // Perform the processing asynchronously in a background
            // thread from the given scheduler.
            .subscribeOn(scheduler)

            // Return an Observable to a multiplied big fraction using
            // the RxJava flatMap() concurrency idiom.
            .flatMap(reducedFraction -> Observable
                     // Multiply the big fraction, which runs in a
                     // background thread.
                     .fromCallable(() -> reducedFraction
                                   .multiply(sBigReducedFraction))
                                   
                     // Perform the processing asynchronously in a
                     // background thread from the given scheduler.
                     .subscribeOn(scheduler));
    }
}
