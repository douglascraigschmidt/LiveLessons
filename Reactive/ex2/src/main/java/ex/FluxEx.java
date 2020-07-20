package ex;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import utils.BigFraction;
import utils.BigFractionUtils;
import utils.ReactorUtils;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;
import static utils.MonosCollector.toMono;

public class FluxEx {
    /**
     * Test BigFraction exception handling using a synchronous Flux
     * stream.
     */
    public static Mono<Void> testFractionExceptions1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionExceptions1()\n");

        // Create a synchronizer to manage completion.
        CountDownLatch latch = new CountDownLatch(1);

        Flux
            // Use a Flux to generate a stream of denominators.
            .just(3, 4, 2, 0, 1)

            // Iterate through the elements.
            .flatMap(denominator -> {
                    // Create/process a BigFraction synchronously.
                    return Mono
                        .fromCallable(() ->
                                      // May throw
                                      // ArithmeticException.
                                      BigFraction.valueOf(100,
                                                          denominator))

                        // Handle exception and convert result to 0.
                        .onErrorResume(t -> {
                                // If exception occurred return 0.
                                sb.append("     exception = "
                                          + t.getMessage()
                                          + "\n");

                                // Convert error to 0.
                                return Mono.just(BigFraction.ZERO);
                            })

                        // Perform a multiplication.
                        .map(fraction -> {
                                sb.append("     "
                                          + fraction.toMixedString()
                                          + " x "
                                          + sBigReducedFraction.toMixedString());
                                // When mono completes multiply and
                                // store it in output.
                                return fraction.multiply(sBigReducedFraction);
                            });
                })

            // Start all the processing in motion.
            .subscribe(fraction ->
                       // Add next fraction to the string buffer.
                       sb.append(" = " + fraction.toMixedString() + "\n"),

                       // Handle error result.
                       error -> sb.append("error"),

                       // Handle final completion.
                       () -> {
                           // Display results when all processing is done.
                           BigFractionUtils.display(sb.toString());

                           // Release the latch.
                           latch.countDown();
                       });

        try {
            // Wait for the flux to complete all its processing.
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Return empty mono.
        return sVoidM;
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
