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
public class SingleEx {
    /**
     * Test synchronous BigFraction reduction using a Single and a
     * pipeline of operations that run on the calling thread.
     */
    public static Completable testFractionReductionSync1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionReductionSync1()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction = BigFraction
            .valueOf(new BigInteger(sBI1),
                     new BigInteger(sBI2),
                     false);

        return Single
            // Use just() to begin synchronously reducing a big
            // fraction in the calling thread.
            .just(BigFraction.reduce(unreducedFraction))

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
            // AsyncTester framework.
            .ignoreElement();
    }

    /**
     * Test synchronous BigFraction reduction using a Single and a
     * pipeline of operations that run on the calling thread.
     * Combines Single with Java functional programming features.
     */
    public static Completable testFractionReductionSync2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionReductionSync2()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction = BigFraction
            .valueOf(new BigInteger(sBI1),
                     new BigInteger(sBI2),
                     false);

        // Create a callable lambda expression that
        // reduces an unreduced big fraction.
        Callable<BigFraction> reduceFraction = () -> {
            // Reduce the big fraction.
            BigFraction reducedFraction = BigFraction
                .reduce(unreducedFraction);

            sb.append("     unreducedFraction "
                      + unreducedFraction.toString()
                      + "\n     reduced improper fraction = "
                      + reducedFraction.toString());

            // Return the reduced big fraction.
            return reducedFraction;
        };

        // Create a lambda function that converts a reduced improper
        // big fraction to a mixed big fraction.
        Function<BigFraction, String> convertToMixedString = result -> {
            sb.append("\n     calling BigFraction::toMixedString\n");

            return result.toMixedString();
        };

        // Create a consumer to print the mixed big fraction result.
        Consumer<String> printResult = result -> {
            sb.append("     mixed reduced fraction = " + result + "\n");
            // Display the result.
            BigFractionUtils.display(sb.toString());
        };

        return Single
            // Use fromCallable() to begin synchronously reducing a
            // big fraction in the calling thread.
            .fromCallable(reduceFraction)

            // After big fraction is reduced return a Single and use
            // map() to call a function that converts the reduced
            // fraction to a mixed fraction string.
            .map(convertToMixedString)

            // Use doOnSuccess() to print the result after it's been
            // successfully converted to a mixed fraction.  If
            // something goes wrong doOnSuccess() will be skipped.
            .doOnSuccess(printResult)

            // Return a Completable to synchronize with the
            // AsyncTester framework.
            .ignoreElement();
    }
}
