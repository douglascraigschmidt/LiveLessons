package ex;

import reactor.core.publisher.Mono;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import static utils.BigFractionUtils.sBI1;
import static utils.BigFractionUtils.sBI2;

/**
 * This class shows how to apply Project Reactor features
 * synchronously to perform basic Mono operations, including
 * fromCallable(), map(), doOnSuccess(), and then().
 */
public class MonoEx {
    /**
     * Test synchronous BigFraction reduction using a mono and a
     * pipeline of operations that run on the calling thread.
     */
    public static Mono<Void> testFractionReductionSync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionReductionSync()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction =
            BigFraction.valueOf(new BigInteger(sBI1),
                                new BigInteger(sBI2),
                                false);

        Callable<BigFraction> reduceFraction = () -> {
            // Reduce the big fraction.
            BigFraction reducedFraction = BigFraction
            .reduce(unreducedFraction);

            sb.append("     unreducedFraction "
                      + unreducedFraction.toString()
                      + "\n     reduced improper fraction = "
                      + reducedFraction.toString());

            // Return the reduction.
            return reducedFraction;
        };

        Function<BigFraction, String> convertToMixedString = result -> {
            sb.append("\n     calling BigFraction::toMixedString\n");

            return result.toMixedString();
        };

        // Create a consumer to print the mixed reduced result.
        Consumer<String> printResult = result -> {
            sb.append("     mixed reduced fraction = " + result + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return Mono
            // Use fromCallable() to begin synchronously reducing a
            // big fraction in the calling thread.
            .fromCallable(reduceFraction)

            // After big fraction is reduced return a mono and use
            // map() to call a function that converts the reduced
            // fraction to a mixed fraction string.
            .map(convertToMixedString)

            // Use doOnSuccess() to print the result after it's been
            // successfully converted to a mixed fraction.  If
            // something goes wrong doOnSuccess() will be skipped.
            .doOnSuccess(printResult)

            // Return an empty mono to synchronize with the
            // AsyncTester framework.
            .then();
    }
}
