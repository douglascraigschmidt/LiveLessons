import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.util.concurrent.Callable;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply RxJava features synchronously to
 * reduce, multiply, and display BigFractions via basic Single
 * operations, including just(), fromCallable(), map(), doOnSuccess(),
 * and ignoreElement().
 */
@SuppressWarnings("ALL")
public class SingleEx {
    /**
     * Create a new unreduced big fraction.
     */
    private static final BigFraction sUnreducedFraction = BigFraction
        .valueOf(new BigInteger(sBI1),
                 new BigInteger(sBI2),
                 false);

    /**
     * Test synchronous BigFraction reduction using a Single and a
     * pipeline of operations that run on the calling thread.
     */
    public static Completable testFractionReductionSync1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionReductionSync1()\n");

        return Single
            // Use just() to begin synchronously reducing a big
            // fraction in the calling thread.
            .just(BigFraction
                  // Reduce the BigFraction.
                  .reduce(sUnreducedFraction))

            // Use doOnSuccess() to print the BigFraction. If
            // something goes wrong doOnSuccess() will be skipped.
            .doOnSuccess(bigFraction -> sb
                         .append("     unreducedFraction "
                                 + sUnreducedFraction.toString()
                                 + "\n     reduced improper fraction = "
                                 + bigFraction.toString()
                                 + "\n     calling BigFraction::toMixedString\n"))

            // After big fraction is reduced return a Single and use
            // map() to call a function that converts the reduced
            // fraction to a mixed fraction string.
            .map(BigFraction::toMixedString)

            // Use doOnSuccess() to print the result after it's been
            // successfully converted to a mixed fraction.  If an
            // exception is thrown doOnSuccess() will be skipped.
            .doOnSuccess(result -> {
                    sb.append("     mixed reduced fraction = " + result + "\n");
                    // Display the result.
                    display(sb.toString());
                })

            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }

    /**
     * Test synchronous BigFraction reduction using a Single and a
     * pipeline of operations that run on the calling thread using
     * slightly different operators and helper methods.
     */
    public static Completable testFractionReductionSync2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionReductionSync2()\n");

        return Single
            // Use fromCallable() to begin synchronously reducing a
            // big fraction in the calling thread.
            .fromCallable(() -> BigFraction
                          // Reduce the BigFraction.
                          .reduce(sUnreducedFraction))

            // Use doOnSuccess() to print the BigFraction. If
            // something goes wrong doOnSuccess() will be skipped.
            .doOnSuccess(bf -> 
                         logBigFraction(sUnreducedFraction, bf, sb))

            // After big fraction is reduced return a Single and use
            // map() to call a function that converts the reduced
            // fraction to a mixed fraction string.
            .map(BigFraction::toMixedString)

            // Use doOnSuccess() to print the result after it's been
            // successfully converted to a mixed fraction.  If
            // something goes wrong doOnSuccess() will be skipped.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))

            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }
}
