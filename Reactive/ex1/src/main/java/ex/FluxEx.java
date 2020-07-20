package ex;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import utils.BigFraction;
import utils.BigFractionUtils;
import utils.ReactorUtils;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;
import static utils.MonosCollector.toMono;

public class FluxEx {
    /**
     * Test BigFraction exception handling using Mono methods.
     */
    public static Mono<Void> testFractionExceptions1() {
        // Use StringBuffer to avoid race conditions.
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions1()\n");

        return Flux
            // Generate results both with and without exceptions.
            .just(true, false)

            // Iterate through the elements.
            .flatMap(throwException -> {
                    // If boolean is true then make the demoninator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create/process a BigFraction asynchronously.
                    return ReactorUtils
                        .fromCallableConcurrent(() ->
                                                // May throw
                                                // ArithmeticException.
                                                BigFraction.valueOf(100,
                                                                    denominator))

                        // Handle an exception.
                        .onErrorResume(t -> {
                                // If exception occurred return 0.
                                sb.append("\n     exception = " 
                                          + t.getMessage()
                                          + "\n");

                                // Convert error to 0.
                                return Mono.just(BigFraction.ZERO);
                            })

                        // Handle success.
                        .doOnSuccess(fraction -> {
                                // When mono completes multiply and
                                // store it in output.
                                fraction.multiply(sBigReducedFraction);
                                sb.append("     result = "
                                          + fraction.toMixedString());
                            });
                })

            // Convert the flux stream into a mono list.
            .collectList()

            // Display results when all processing is done.
            .flatMap(___ -> {
                    // Print results.
                    BigFractionUtils.display(sb.toString());

                    // Return empty mono.
                    return sVoidM;
                });
    }

    /**
     * Test BigFraction multiplications using a stream of monos and a
     * pipeline of operations, including flatMap(), collectList(), and
     * first().
     */
    public static Mono<Void> testFractionMultiplications1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications1()\n");

        // This function reduces/multiplies a big fraction asynchronously.
        Function<BigFraction, Mono<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> ReactorUtils
            // Perform the reduction asynchronously.
            .fromCallableConcurrent(() -> BigFraction.reduce(unreducedFraction))

            // Return a mono to a multiplied big fraction.
            .flatMap(reducedFraction -> ReactorUtils
                     // Multiply BigFractions asynchronously since it
                     // may run for a long time.
                     .fromCallableConcurrent(() -> reducedFraction
                                             .multiply(sBigReducedFraction)));

        sb.append("     Printing sorted results:");

        // Process the function in a flux stream.
        return ReactorUtils
            // Generate sMAX_FRACTIONS random, large, and unreduced BigFractions.
            .generate(() -> BigFractionUtils.makeBigFraction(new Random(),
                                                             false))
            .take(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .flatMap(reduceAndMultiplyFraction)

            // Collect the results into a list.
            .collectList()

            // Process the results of the collected list.
            .flatMap(list ->
                     // Sort and print the results after all async
                     // fraction reductions complete.
                     BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Test BigFraction multiplications by combining the Java streams
     * framework with the Reactor framework.
     */
    public static Mono<Void> testFractionMultiplications2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications2()\n");

        // Function asynchronously reduces/multiplies a big fraction.
        Function<BigFraction, Mono<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> ReactorUtils
            // Perform the reduction asynchronously.
            .fromCallableConcurrent(() -> BigFraction.reduce(unreducedFraction))

            // Return a mono to a big fraction that's multiplied
            // asynchronously since it may run for a long time.
            .flatMap(reducedFraction -> ReactorUtils
                     // Multiply BigFractions asynchronously since it
                     // may run for a long time.
                     .fromCallableConcurrent(() -> reducedFraction
                                             .multiply(sBigReducedFraction)));

        sb.append("     Printing sorted results:");

        // Process the function in a sequential stream.
        return Stream
            // Generate sMAX_FRACTIONS random, large, and unreduced BigFractions.
            .generate(() -> BigFractionUtils.makeBigFraction(new Random(),
                                                             false))
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(reduceAndMultiplyFraction)

            // Trigger intermediate operation processing and return a
            // mono to a list of big fractions that are being reduced
            // and multiplied asynchronously.
            .collect(toMono())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .flatMap(list -> BigFractionUtils.sortAndPrintList(list,
                                                               sb));
    }
}
