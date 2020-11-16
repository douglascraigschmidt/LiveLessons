import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import utils.BigFraction;
import utils.BigFractionUtils;

import static utils.BigFractionUtils.sBigReducedFraction;
import static utils.BigFractionUtils.sVoidM;

/**
 * This class shows how to apply RxJava features synchronously to
 * perform basic Observable operations, including just(), map(), and
 * blockingSubscribe().
 */
public class ObservableEx {
    /**
     * Test BigFraction multiplication using a synchronous Observable
     * stream.
     */
    public static Completable testFractionMultiplication() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplication()\n");

        Observable
            // Use just() to generate a stream of big fractions.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#just-T-T-T-T-
            .just(BigFraction.valueOf(100, 3),
                  BigFraction.valueOf(100, 4),
                  BigFraction.valueOf(100, 2),
                  BigFraction.valueOf(100, 1))

            // Use map() to multiply each element in the stream by a
            // constant.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#map-io.reactivex.rxjava3.functions.Function-
            .map(fraction -> {
                    sb.append("     "
                              + fraction.toMixedString()
                              + " x "
                              + sBigReducedFraction.toMixedString());

                    // Multiply and return result.
                    return fraction.multiply(sBigReducedFraction);
                })

            // Use blockingSubscribe() to initiate all the processing
            // and handle the results.  This call runs synchronously
            // since the publisher (just()) is synchronous, runs in
            // the calling thread, and blockingSubscribe() doesn't
            // return until the final completion event is received.
            // Subsequent examples show more interesting types of
            // publishers that enable asynchrony.
            // http://reactivex.io/RxJava/3.x/javadoc/io/reactivex/rxjava3/core/Observable.html#blockingSubscribe-io.reactivex.rxjava3.functions.Consumer-io.reactivex.rxjava3.functions.Consumer-io.reactivex.rxjava3.functions.Action-
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
        return sVoidM;
    }
}
