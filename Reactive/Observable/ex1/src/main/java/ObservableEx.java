import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.List;
import java.util.Random;

import static utils.BigFractionUtils.logBigFraction;
import static utils.BigFractionUtils.sBigReducedFraction;

/**
 * This class shows how to apply RxJava features synchronously to
 * perform basic Observable operations, including just(),
 * fromCallable(), fromArray(), fromCallable(), doOnNext(), map(),
 * mergeWith(), repeat(), and blockingSubscribe().
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

            // Log the contents of the computation.
            .doOnNext(bigFraction ->
                          sb.append("    ["
                                        + Thread.currentThread().getId()
                                        + "] "
                                        + sBigReducedFraction.toMixedString()
                                        + " x "
                                        + bigFraction.toMixedString()
                                        + "\n"))

            // Use map() to multiply each element in the stream by a
            // constant.
            .map(bigFraction ->
                     // Multiply and return result.
                     bigFraction.multiply(sBigReducedFraction))

            // Use blockingSubscribe() to initiate all the processing
            // and handle the results.  This call runs synchronously
            // since the publisher (just()) is synchronous, runs in
            // the calling thread, and blockingSubscribe() doesn't
            // return until the final completion event is received.
            // Subsequent examples show more interesting types of
            // publishers that enable asynchrony.
            .blockingSubscribe(// Handle next event.
                               multipliedBigFraction ->
                                   // Add fraction to the string buffer.
                                   sb.append("    ["
                                                 + Thread.currentThread().getId()
                                                 + "] result = "
                                                 + multipliedBigFraction.toMixedString()
                                                 + "\n"),

                               // Handle error result event.
                               error -> {
                                   // Append the exception name.
                                   sb.append(error.getMessage());

                                   // Display results when processing is done.
                                   BigFractionUtils.display(sb.toString());
                               },

                               // Handle final completion event.
                               () ->
                                   // Display results when processing is done.
                                   BigFractionUtils.display(sb.toString()));

        // Return a Completable to indicate to the AsyncTaskBarrier
        // that all the processing is done.
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

        Observable
            // Use fromIterable() to generate a stream of big
            // fractions.
            .fromIterable(bigFractionList)

            // Log the contents of the computation.
            .doOnNext(bf -> logBigFraction(sBigReducedFraction, bf, sb))

            // Use map() to multiply each element in the stream by a
            // constant.
            .map(fraction -> fraction.multiply(sBigReducedFraction))

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
                               error -> {
                                   // Append the exception name.
                                   sb.append(error.getMessage());

                                   // Display results when processing is done.
                                   BigFractionUtils.display(sb.toString());
                               },

                               // Handle final completion event.
                               () ->
                                   // Display results when processing is done.
                                   BigFractionUtils.display(sb.toString()));

        // Return a Completable to indicate to the AsyncTaskBarrier
        // that all the processing is done.
        return BigFractionUtils.sVoidC;
    }

    /**
     * A test of BigFraction multiplication using an synchronous
     * Observable stream that merges results together.
     */
    public static Completable testFractionMultiplicationSync3() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationSync3()\n");

        // Random number generator.
        Random random = new Random();

        // Create an array of BigFraction objects.
        BigFraction[] bigFractionArray = {
            BigFraction.valueOf(100, 3),
            BigFraction.valueOf(100, 4),
            BigFraction.valueOf(100, 2),
            BigFraction.valueOf(100, 1)
        };

        Observable<BigFraction> o1 = Observable
            // Use fromArray() to generate a stream of big
            // fractions.
            .fromArray(bigFractionArray);

        Observable<BigFraction> o2 = Observable
            // Use from() and Mono.fromCallable() to "lazily" generate
            // a stream of random big fractions.
            .fromCallable(() ->
                          BigFractionUtils.makeBigFraction(random,
                                                           true))

            // Generate random big fractions 4 times.
            .repeat(4);

        o1
            // Flatten Observable o1 and o2 into a single Observable
            // sequence, without any transformations.
            .mergeWith(o2)

            // Log the contents of the computation.
            .doOnNext(bf -> logBigFraction(sBigReducedFraction, bf, sb))

            // Use map() to multiply each element in the stream by a
            // constant.
            .map(fraction -> fraction.multiply(sBigReducedFraction))

            // Use blockingSubscribe() to initiate all the processing
            // and handle the results synchronously.
            .blockingSubscribe(// Handle next event.
                               fraction ->
                               sb.append(" = " + fraction.toMixedString() + "\n"),
                               // Handle the error.
                               t -> {
                                   // Append the error message to the
                                   // StringBuilder.
                                   sb.append(t.getMessage());

                                   // Display results when processing is done.
                                   BigFractionUtils.display(sb.toString());
                               },
                               () ->
                               // Display results when processing is done.
                               BigFractionUtils.display(sb.toString()));

        // Return a Completable to indicate to the AsyncTaskBarrier that
        // all the processing is done.
        return BigFractionUtils.sVoidC;
    }
}
