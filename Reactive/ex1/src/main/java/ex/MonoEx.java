package ex;

import reactor.core.publisher.Mono;
import utils.BigFraction;
import utils.BigFractionUtils;
import utils.ReactorUtils;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import static utils.BigFractionUtils.*;

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
            sb.append("     mixed reduced fraction = " + result);
            BigFractionUtils.display(sb.toString());
        };

        return Mono
            // Synchronously reduce the unreduced big fraction.
            .fromCallable(reduceFraction)

            // After big fraction is reduced return a mono to a
            // function that converts it to a mixed fraction string.
            .map(convertToMixedString)

            // Print result after it's converted to a mixed fraction.
            .doOnSuccess(printResult)

            // Return an empty mono.
            .then();
    }

    /**
     * Test asynchronous BigFraction reduction using a Mono and a
     * pipeline of operations that run off the calling thread.
     */
    public static Mono<Void> testFractionReductionAsync() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionReductionAsync()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction = 
            BigFraction.valueOf(new BigInteger (sBI1),
                                new BigInteger(sBI2),
                                false);

        Callable<BigFraction> reduceFraction = () -> {
            // Reduce the big fraction.
            BigFraction reducedFraction =
            BigFraction.reduce(unreducedFraction);

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
            sb.append("     mixed reduced fraction = "
                      + result);
            BigFractionUtils.display(sb.toString());
        };

        return ReactorUtils
            // Asynchronously reduce the unreduced big fraction.
            .fromCallableConcurrent(reduceFraction)

            // After big fraction is reduced return a mon to a
            // function that converts it a mixed fraction string.
            .map(convertToMixedString)

            // Print result after it's converted to a mixed fraction.
            .doOnSuccess(printResult)

            // Return an empty mono.
            .then();
    }

    /**
     * Test asynchronous BigFraction multiplication using a mono,
     * callable, and the common fork-join pool.
     */
    public static Mono<Void> testFractionMultiplicationCallable() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCallable()\n");

        // Create a callable that multiplies two large fractions.
        Callable<BigFraction> call = () -> {
            BigFraction bf1 = new BigFraction(sF1);
            BigFraction bf2 = new BigFraction(sF2);

            // Return the result of multiplying the fractions.
            return bf1.multiply(bf2);
        };

        // Submit the call to the common fork-join pool and store
        // the future it returns.
        Mono<BigFraction> mono = ReactorUtils
            .fromCallableConcurrent(call);

        // Block until the result is available.
        BigFraction result = mono.block();

        assert result != null;
        sb.append("     Callable.call() = "
                  + result.toMixedString());
        BigFractionUtils.display(sb.toString());

        // Return an empty mono.
        return sVoidM;
    }

    /**
     * Test asynchronous BigFraction multiplication and addition using
     * zipWith().
     */
    public static Mono<Void> testFractionCombine() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionCombine()\n");

        // A random number generator.
        Random random = new Random();

        // Create a random BigFraction and reduce/multiply it
        // asynchronously.
        Mono<BigFraction> m1 = ReactorUtils
            .fromCallableConcurrent(() -> BigFractionUtils.makeBigFraction(random, true)
                                    .multiply(sBigReducedFraction));

        // Create another random BigFraction and reduce/multiply it
        // asynchronously.
        Mono<BigFraction> m2 = ReactorUtils
            .fromCallableConcurrent(() -> BigFractionUtils.makeBigFraction(random, true)
                                    .multiply(sBigReducedFraction));
        
        // Create a consumer that prints the result as a mixed
        // fraction after it's added together.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction
            -> { 
            sb.append("     combined result = " 
                      + bigFraction.toMixedString());
            BigFractionUtils.display(sb.toString());
        };

        return m1
            // Wait until m1 and m2 are complete and then add the
            // results.
            .zipWith(m2,
                     BigFraction::add)

            // Print result after converting it to a mixed fraction.
            .doOnSuccess(mixedFractionPrinter)

            // Return an empty mono.
            .then();
    }
}
