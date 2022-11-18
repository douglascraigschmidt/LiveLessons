import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;
import static utils.BigFractionUtils.*;

/**
 * This class shows how to reduce and/or multiply big fractions
 * asynchronously using the RxJava flatMap() concurrency idiom and
 * many RxJava {@link Observable} operators, including fromArray(),
 * map(), generate(), take(), flatMap(), fromCallable(), filter(),
 * reduce(), collectInto(), subscribeOn(), onErrorReturn(),
 * onErrorResumeNext(), and Schedulers.computation().  It also shows
 * how these operators can be used together with RxJava {@link Single}
 * and {@link Maybe} operators, including fromCallable(), ambArray(),
 * flatMapCompletable(), subscribeOn(), ignoreElement(), and
 * doOnSuccess().
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class ObservableEx {
    /**
     * Create a random number generator to generate BigFractions.
     */
    private static final Random sRANDOM = new Random();

    /**
     * Test Observable exception handling via onErrorReturn().
     */
    public static Completable testFractionException1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionException1()\n");

        // Create a function to handle an ArithmeticException.
        Function<Throwable,
                 ? extends BigFraction> errorHandler = t -> {
            // If exception occurred return 0.
            sb.append("     exception = "
                      + t.getMessage()
                      + "\n");

            if (t instanceof ArithmeticException)
                // Convert ArithmeticException to 0.
                return BigFraction.ZERO;
            else
                // Rethrow exception!
                throw t;
        };

        // Create an array of denominators, including 0, which
        // triggers an ArithmeticException.
        Integer[] denominators = new Integer[]{3, 4, 2, 0, 1};

        return Observable
            // Generate a stream from the denominators array.
            .fromArray(denominators)

            // Use RxJava's flatMap() concurrency idiom to reduce and
            // multiply big fractions in the computation thread pool.
            .flatMap(denominator -> Observable
                     // Run asynchronously via an "inner publisher" in
                     // a background thread from the given scheduler.
                     .fromCallable(() -> BigFraction
                                   // Emit a random BigFraction that
                                   // throws ArithmeticException if
                                   // denominator is 0.
                                   .valueOf(Math.abs(sRANDOM.nextInt()),
                                            denominator))

                     // Run computations in a background thread from
                     // the computation scheduler.
                     .subscribeOn(Schedulers.computation())

                     // Convert any ArithmeticException to 0.
                     .onErrorReturn(errorHandler)

                     // Log the BigFractions.
                     .doOnNext(bf ->
                               logBigFraction(bf, sBigReducedFraction, sb))

                     // Perform a BigFraction multiplication.
                     .map(bf -> bf.multiply(sBigReducedFraction)))

            // Remove any BigFraction results that are <= 0.
            .filter(fraction -> fraction.compareTo(0) > 0)

            // Collect the results into a List.
            .collect(toList())
            
            // Print the List and return a Completable that
            // synchronizes with the AsyncTaskBarrier framework.
            .flatMapCompletable(list -> BigFractionUtils
                                .printList(list, sb));
    }

    /**
     * Test Observable exception handling via onErrorResumeNext().
     */
    public static Completable testFractionException2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionException2()\n");

        // Create a list of denominators, including 0, which triggers
        // an ArithmeticException.
        List<Integer> denominators = List.of(3, 4, 2, 0, 1, 5);

        // Create a Function lambda to handle an ArithmeticException.
        Function<Throwable,
                 Observable<BigFraction>> logAndReturnEmptyObservable = t -> {
            // Record the exception message.
            sb.append("     exception = "
                      + t.getMessage());

            // Return an empty Observable when an exception occurs.
            return Observable.empty();
        };

        return Observable
            // Generate an Observable stream from the denominators
            // list.
            .fromIterable(denominators)

            // Generate a random BigFraction.
            .map(denominator -> BigFraction
                 // Throws ArithmeticException if
                 // denominator is 0.
                 .valueOf(Math.abs(sRANDOM.nextInt()),
                          denominator))

            // Catch ArithmeticException and return an empty
            // Observable, which terminates the stream at that point.
            .onErrorResumeNext(logAndReturnEmptyObservable)

            // Collect the non-empty BigFractions into a list.
            .collect(toList())

            // Print the List and return a Completable that
            // synchronizes with the AsyncTaskBarrier framework.
            .flatMapCompletable(list -> BigFractionUtils
                                .printList(list, sb));
    }

    /**
     * Test an asynchronous {@link Observable} stream consisting of
     * fromIterable(), flatMap(), reduce(), and a pool of threads to
     * perform BigFraction reductions and multiplications.
     */
    public static Completable testFractionMultiplications() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications()\n");

        // Create an array of reduced BigFraction objects.
        List<BigFraction> bigFractionList = List
            .of(BigFraction.valueOf(1000, 30),
                BigFraction.valueOf(1000, 40),
                BigFraction.valueOf(1000, 20),
                BigFraction.valueOf(1000, 10));

        // Display the results.
        Consumer<? super BigFraction> displayResults = result -> {
            sb.append("    sum of BigFractions = "
                      + result
                      + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return Observable
            // Emit a stream of reduced big fractions.
            .fromIterable(bigFractionList)

            // Use RxJava's flatMap() concurrency idiom to multiply
            // these BigFractions asynchronously in a thread pool.
            .flatMap(bf -> 
                     multiplyFractions(bf, Schedulers.computation()))

            // Log the BigFractions.
            .doOnNext(bf -> 
                      logBigFraction(bf, sBigReducedFraction, sb))

            // Reduce the results into one Maybe<BigFraction>.
            .reduce(BigFraction::add)

            // Display the results if all goes well.
            .doOnSuccess(displayResults)

            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }

    /**
     * Test an asynchronous Observable stream consisting of
     * generate(), take(), flatMap(), collect(), flatMapCompletable(),
     * and a pool of threads to perform BigFraction reductions and
     * multiplications.
     */
    public static Completable testFractionReductionMultiplications() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionReductionMultiplications()\n");

        sb.append("     Printing sorted results:");

        // Process the function in a observable stream.
        return Observable
            // Generate a stream of random unreduced big fractions.
            .generate((Emitter<BigFraction> emit) -> emit
                      // Emit a random unreduced big fraction.
                      .onNext(BigFractionUtils.makeBigFraction(sRANDOM, false)))

            // Limit the # of random unreduced big fractions.
            .take(sMAX_FRACTIONS)

            // Use RxJava's flatMap() concurrency idiom to reduce and
            // multiply each fraction in the computation thread pool.
            .flatMap(unreducedFraction ->
                     reduceAndMultiplyFraction(unreducedFraction,
                                               Schedulers.computation()))

            // Collect results into an ArrayList.
            .collectInto(new ArrayList<BigFraction>(),
                         List::add)

            // Sort and print the List and return a Completable that
            // synchronizes with the AsyncTaskBarrier framework.
            .flatMapCompletable(list -> BigFractionUtils.
                                // Sort/print results after all async
                                // fraction operations complete.
                                sortAndPrintList(list, sb));
    }

    /**
     * @return An {@link Observable} that's signaled after the {@code
     *         unreducedFraction} is reduced/multiplied asynchronously
     *         in background threads from the given {@code scheduler}
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
            .flatMap(reducedFraction ->
                     multiplyFractions(reducedFraction, scheduler));
    }

    /**
     * @return An {@link Observable} that's signaled after the {@link
     *         BigFraction} is multiplied asynchronously in a
     *         background thread from the given {@code scheduler}
     */
    private static Observable<BigFraction> multiplyFractions
        (BigFraction bigFraction,
         Scheduler scheduler) {
        return Observable
            // Perform multiplication in a background thread.
            .fromCallable(() -> bigFraction
                          .multiply(sBigReducedFraction))
                     
            // Perform the processing asynchronously in a background
            // thread from the given scheduler.
            .subscribeOn(scheduler);
    }
}
