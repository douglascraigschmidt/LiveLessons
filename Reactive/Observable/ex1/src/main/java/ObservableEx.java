import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.Random;

/**
 * This class shows how to apply RxJava features synchronously to
 * perform basic Observable operations, including fromCallable(),
 * repeat(), just(), map(), mergeWith(), and blockingSubscribe().
 */
public class ObservableEx {
    /**
     * Test BigFraction multiplication using a synchronous Observable
     * stream.
     */
    public static Completable testFractionMultiplication1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplication1()\n");

        Observable
            // Use just() to generate a stream of big fractions.
            .just(BigFraction.valueOf(100, 3),
                  BigFraction.valueOf(100, 4),
                  BigFraction.valueOf(100, 2),
                  BigFraction.valueOf(100, 1))

            // Use map() to multiply each element in the stream by a
            // constant.
            .map(fraction -> {
                    sb.append("     "
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
                               sb.append(" = " 
                                         + multipliedFraction.toMixedString() 
                                         + "\n"),
                               
                               // Handle error result event.
                               error -> sb.append("error"),

                               // Handle final completion event.
                               () ->
                               // Display results when processing is done.
                               BigFractionUtils.display(sb.toString()));

        // Return empty mono to indicate to the AsyncTester that all
        // the processing is done.
        return BigFractionUtils.sVoidS;
    }

    /**
     * Another BigFraction multiplication test using a couple of
     * synchronous Observable streams that are merged together.
     */
    public static Completable testFractionMultiplication2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplication2()\n");

        // Random number generator.
        Random random = new Random();

        Observable<BigFraction> o1 = Observable
            // Use just() to "eagerly" generate a stream of big fractions.
            .just(BigFraction.valueOf(100, 3),
                  BigFraction.valueOf(100, 4),
                  BigFraction.valueOf(100, 2),
                  BigFraction.valueOf(100, 1));

        Observable<BigFraction> o2 = Observable
            // Use fromCallable() to "lazily" generate a stream of random big fractions.
            .fromCallable(() -> BigFractionUtils.makeBigFraction(random, true))

            .repeat(4);
        
        o1
            // Flatten the current Observable and another
            // ObservableSource into a single Observable sequence,
            // without any transformations.
            .mergeWith(o2)    

            // Use map() to multiply each element in the stream by a
            // constant.
            .map(fraction -> {
                    sb.append("     "
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
                               sb.append(" = " 
                                         + multipliedFraction.toMixedString() 
                                         + "\n"),
                               
                               // Handle error result event.
                               error -> sb.append("error"),

                               // Handle final completion event.
                               () ->
                               // Display results when processing is done.
                               BigFractionUtils.display(sb.toString()));

        // Return empty mono to indicate to the AsyncTester that all
        // the processing is done.
        return BigFractionUtils.sVoidS;
    }
}
