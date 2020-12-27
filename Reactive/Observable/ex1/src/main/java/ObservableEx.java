import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.List;
import java.util.Random;

/**
 * This class shows how to apply RxJava features synchronously to
 * perform basic Observable operations, including fromCallable(),
 * repeat(), just(), map(), mergeWith(), and blockingSubscribe().
 */
@SuppressWarnings("ALL")
public class ObservableEx {
    /**
     * Test BigFraction multiplication using a synchronous Observable
     * stream.
     */
    public static Completable testFractionMultiplicationSync1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationSync1()\n");

        Observable
            // Use just() to generate a stream of big fractions.
            .just(BigFraction.valueOf(100, 3),
                  BigFraction.valueOf(100, 4),
                  BigFraction.valueOf(100, 2),
                  BigFraction.valueOf(100, 1))

            // Use map() to multiply each element in the stream by a
            // constant.
            .map(fraction -> {
                    sb.append("    ["
                              + Thread.currentThread().getId()
                              + "] "
                              + BigFractionUtils.sBigReducedFraction.toMixedString()
                              + " x "
                              + fraction.toMixedString()
                              + "\n");

                    // Multiply and return result.
                    return fraction.multiply(BigFractionUtils.sBigReducedFraction);
                })

            // Use blockingSubscribe() to initiate all the processing
            // and handle the results.  This call runs synchronously
            // since the publisher (just()) is synchronous, runs in
            // the calling thread, and blockingSubscribe() doesn't
            // return until the final completion event is received.
            // Subsequent examples show more interesting types of
            // publishers that enable asynchrony.
            .blockingSubscribe(// Handle next event.
                               multipliedFraction ->
                               // Add fraction to the string buffer.
                               sb.append("    ["
                                         + Thread.currentThread().getId()
                                         + "] result = "
                                         + multipliedFraction.toMixedString() 
                                         + "\n"),
                               
                               // Handle error result event.
                               error -> sb.append(error.getMessage()),

                               // Handle final completion event.
                               () ->
                               // Display results when processing is done.
                               BigFractionUtils.display(sb.toString()));

        // Return empty completable to indicate to the AsyncTaskBarrier that all
        // the processing is done.
        return BigFractionUtils.sVoidC;
    }

    /**
     * Another BigFraction multiplication test using a synchronous
     * Observable stream and some local variables.
     */
    public static Completable testFractionMultiplicationSync2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationSync2()\n");

        // Create a list of BigFraction objects.
        List<BigFraction> bigFractionList = List.of
            (BigFraction.valueOf(100, 3),
             BigFraction.valueOf(100, 4),
             BigFraction.valueOf(100, 2),
             BigFraction.valueOf(100, 1));

        // Log the contents.
        Consumer<BigFraction> logContents = bigFraction ->
            sb.append("    ["
                      + Thread.currentThread().getId()
                      + "] "
                      + bigFraction.toMixedString()
                      + " x "
                      + BigFractionUtils.sBigReducedFraction.toMixedString()
                      + "\n");

        // Define a function that multiplies a BigFraction by a large
        // constant.
        Function<BigFraction,
                 BigFraction> multiplyBigFraction = bigFraction -> {
            // Multiply and return result.
            return bigFraction.multiply(BigFractionUtils.sBigReducedFraction);
        };

        Observable
            // Use fromIterable() to generate a stream of big
            // fractions.
            .fromIterable(bigFractionList)

            // Log the contents of the computation.
            .doOnNext(logContents)

            // Use map() to multiply each element in the stream by a
            // constant.
            .map(multiplyBigFraction)

            // Use blockingSubscribe() to initiate all the processing
            // and handle the results.  This call runs synchronously
            // since the publisher (fromIterable()) is synchronous,
            // runs in the calling thread, and blockingSubscribe()
            // doesn't return until the final completion event is
            // received.  Subsequent examples show more interesting
            // types of publishers that enable asynchrony.
            .blockingSubscribe(// Handle next event.
                               multipliedBigFraction ->
                               // Add fraction to the string buffer.
                               sb.append("    ["
                                         + Thread.currentThread().getId()
                                         + "] result = "
                                         + multipliedBigFraction.toMixedString()
                                         + "\n"),
                               
                               // Handle error result event.
                               error -> sb.append(error.getMessage()),

                               // Handle final completion event.
                               () ->
                               // Display results when processing is done.
                               BigFractionUtils.display(sb.toString()));

        // Return empty completable to indicate to the
        // AsyncTaskBarrier that all the processing is done.
        return BigFractionUtils.sVoidC;
    }

    /**
     * Another BigFraction multiplication test using a couple of
     * synchronous Observable streams that are merged together.
     */
    public static Completable testFractionMultiplicationAsync() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationAsync()\n");

        // Random number generator.
        Random random = new Random();

        // Create an array of BigFraction objects.
        BigFraction[] bigFractionArray = {
            BigFraction.valueOf(100, 3),
            BigFraction.valueOf(100, 4),
            BigFraction.valueOf(100, 2),
            BigFraction.valueOf(100, 1)
        };

        // Define a function that multiplies a BigFraction by a large
        // constant.
        Function<BigFraction,
                 BigFraction> multiplyBigFraction = bigFraction -> {
            // Multiply and return result.
            return bigFraction.multiply(BigFractionUtils.sBigReducedFraction);
        };

        // Log the contents.
        Consumer<BigFraction> logContents = bigFraction ->
            sb.append("    ["
                      + Thread.currentThread().getId()
                      + "] "
                      + bigFraction.toMixedString()
                      + " x "
                      + BigFractionUtils.sBigReducedFraction.toMixedString()
                      + "\n");

        Observable<BigFraction> o1 = Observable
            // Use fromArray() to generate a stream of big
            // fractions.
            .fromArray(bigFractionArray);

        Observable<BigFraction> o2 = Observable
            // Use fromCallable() to "lazily" generate a stream of
            // random big fractions.
            .fromCallable(() -> BigFractionUtils.makeBigFraction(random, true))

            // Generate random big fractions 4 times.
            .repeat(4);
        
        o1
            // Flatten the current Observable and another
            // ObservableSource into a single Observable sequence,
            // without any transformations.
            .mergeWith(o2)    

            // Arrange to run this Flux stream in a background
            // thread.
            .subscribeOn(Schedulers.single())

            // Log the contents of the computation.
            .doOnNext(logContents)

            // Use map() to multiply each element in the stream by a
            // constant.
            .map(multiplyBigFraction)

            // Use blockingSubscribe() to initiate all the processing
            // and handle the results.  This call runs in the calling
            // thread, though the rest of the processing runs in the
            // background thread due to subscribeOn().
            .blockingSubscribe(// Handle next event.
                               multipliedFraction ->
                               // Add fraction to the string buffer.
                               sb.append("    ["
                                         + Thread.currentThread().getId()
                                         + "] result "
                                         + multipliedFraction.toMixedString() 
                                         + "\n"),
                               
                               // Handle error result event.
                               error -> sb.append(error.getMessage()),

                               // Handle final completion event.
                               () ->
                               // Display results when processing is done.
                               BigFractionUtils.display(sb.toString()));

        // Return empty mono to indicate to the AsyncTaskBarrier that all
        // the processing is done.
        return BigFractionUtils.sVoidC;
    }
}
