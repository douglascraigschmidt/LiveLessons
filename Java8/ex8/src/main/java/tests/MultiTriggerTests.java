package tests;

import utils.BigFraction;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;
import static utils.FuturesCollector.toFuture;

/**
 * Tests that showcase {@link CompletableFuture} completion stage
 * methods that trigger after multiple futures complete.
 */
public final class MultiTriggerTests {
    /**
     * Test big fraction multiplication and addition using a
     * supplyAsync() and thenCombine().
     */
    public static CompletableFuture<Void> testFractionCombine() {
        StringBuilder sb = 
            new StringBuilder(">> Calling testFractionCombine()\n");

        // A random number generator.
        Random random = new Random();

        // Create a random BigFraction and multiply it.
        CompletableFuture<BigFraction> cf1 = CompletableFuture
            // This code runs asynchronously.
            .supplyAsync(() -> makeBigFraction(random, true)
                         // Multiply a random fraction with a constant
                         // fraction.
                         .multiply(sBigReducedFraction));

        // Create another random BigFraction and multiply it.
        CompletableFuture<BigFraction> cf2 = CompletableFuture
            // This code runs asynchronously.
            .supplyAsync(() -> makeBigFraction(random, true)
                         // Multiply a random fraction with a constant
                         // fraction.
                         .multiply(sBigReducedFraction));
        
        // Create a consumer that prints the result as a mixed
        // fraction after it's reduced.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction
            -> { 
            sb.append("     combined result = " 
                      + bigFraction.toMixedString());
            display(sb.toString());
        };

        return cf1
            // Wait until cf1 and cf2 are complete and then add the
            // results.
            .thenCombine(cf2,
                         BigFraction::add)

            // Print result after converting it to a mixed fraction.
            .thenAccept(mixedFractionPrinter);
    }

    /**
     * Test BigFraction multiplications using a stream of
     * CompletableFutures and a chain of completion stage methods
     * involving supplyAsync(), thenCompose(), and acceptEither().
     */
    public static CompletableFuture<Void> testFractionMultiplications1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications1()\n");

        // Lambda asynchronously reduces/multiplies a big fraction.
        Function<BigFraction,
                         CompletableFuture<BigFraction>> reduceAndMultiplyFraction =
            sUnreducedFraction -> CompletableFuture
            // Perform the reduction asynchronously.
            .supplyAsync(() -> BigFraction.reduce(sUnreducedFraction))

            // thenCompose() is like flatMap(), i.e., it returns a
            // completable future to a multiplied big fraction.
            .thenCompose(reducedFraction -> CompletableFuture
                         // Multiply BigFractions asynchronously since
                         // it may run for a long time.
                         .supplyAsync(() -> reducedFraction
                                      .multiply(sBigReducedFraction)));

        sb.append("     Printing sorted results:");

        // Process reduceAndMultiplyFraction in a sequential stream.
        return Stream
            // Generate sMAX_FRACTIONS random unreduced BigFractions.
            .generate(() -> makeBigFraction(new Random(), false))
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(reduceAndMultiplyFraction)

            // Trigger intermediate operation processing and return a
            // future to a list of big fractions that are being
            // reduced and multiplied asynchronously.
            .collect(toFuture())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .thenCompose(list -> sortAndPrintList(list,
                                                  sb));
    }

    /**
     * Test BigFraction multiplications using a stream of
     * CompletableFutures and a chain of completion stage methods
     * involving supplyAsync(), thenComposeAsync(), and
     * acceptEither().
     */
    public static CompletableFuture<Void> testFractionMultiplications2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications2()\n");

        // Function asynchronously reduces/multiplies a big fraction.
        Function<BigFraction,
            CompletableFuture<BigFraction>> reduceAndMultiplyFraction =
            sUnreducedFraction -> CompletableFuture
            // Perform the reduction asynchronously.
            .supplyAsync(() -> BigFraction.reduce(sUnreducedFraction))

            // thenApplyAsync() returns a completable future to a big
            // fraction that's multiplied asynchronously since it may
            // run for a long time.
            .thenApplyAsync(reducedFraction -> reducedFraction
                            .multiply(sBigReducedFraction));

        sb.append("     Printing sorted results:");

        // Process the two lambdas in a sequential stream.
        return Stream
            // Generate sMAX_FRACTIONS random unreduced BigFractions.
            .generate(() -> makeBigFraction(new Random(), false))
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(reduceAndMultiplyFraction)

            // Trigger intermediate operation processing and return a
            // future to a list of big fractions that are being
            // reduced and multiplied asynchronously.
            .collect(toFuture())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .thenCompose(list -> sortAndPrintList(list,
                                                  sb));
    }
}
