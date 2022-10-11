package tests;

import utils.BigFraction;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;
import static utils.FuturesCollector.toFuture;

/**
 * Tests that showcase {@link CompletableFuture} completion stage
 * methods that handle exceptions.
 */
public final class ExceptionTests {
    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the handle() method.
     */
    public static CompletableFuture<Void> testFractionExceptions1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions1()\n");

        // Generate results both with and without exceptions.
        // Convert to a stream.
        return Stream
            .of(true, false)

            // Iterate through the elements.
            .map(throwException -> {
                    // If boolean is true then make the denominator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create and process a BigFraction.
                    return CompletableFuture
                        .supplyAsync(() ->
                                     // Run asynchronously and maybe
                                     // throw ArithmeticException.
                                     BigFraction.valueOf(100, denominator))

                        // Handle outcome of previous stage.
                        .handle((fraction, ex) -> {
                                // If exception occurred convert it to 0.
                                if (fraction == null) {
                                    sb.append("     exception = " + ex.getMessage());
                                    return BigFraction.ZERO;
                                } else
                                    // Multiply fraction by a constant.
                                    return fraction.multiply(sBigReducedFraction);
                            })

                        // When future completes prepare results for output.
                        .thenAccept(fraction ->
                                    sb.append("\n     result = "
                                              + fraction.toMixedString()));
                })

            // Collect to a single future.
            .collect(toFuture())

            // When that future is done display the results.
            .thenCompose(___ -> {
                    // Print results.
                    display(sb.toString());
                    return sCompleted;
                });
    }

    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the exceptionally() method.
     */
    public static CompletableFuture<Void> testFractionExceptions2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions2()\n");

        // Generate results both with and without exceptions.
        // Convert to stream.
        return Stream
            .of(true, false)

            // Iterate through the elements.
            .map(throwException -> {
                    // If boolean is true then make the demoninator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create and process a BigFraction.
                    return CompletableFuture
                        .supplyAsync(() ->
                                     // Run asynchronously and maybe
                                     // throw ArithmeticException.
                                     BigFraction.valueOf(100, denominator))

                        // Multiply fraction by a constant when
                        // previous stage completes.
                        .thenApply(fraction ->
                                   fraction.multiply(sBigReducedFraction))

                        // If exception occurred convert it to 0.
                        .exceptionally(ex -> {
                                sb.append("     exception = " + ex.getMessage());
                                return BigFraction.ZERO;
                            })

                        // When future completes prepare results for output.
                        .thenAccept(fraction ->
                                    sb.append("\n     result = "
                                              + fraction.toMixedString()));
                })

            // Collect to a single future.
            .collect(toFuture())

            // When that future is done display the results.
            .thenCompose(___ -> {
                    // Print results.
                    display(sb.toString());
                    return sCompleted;
                });
    }

    /**
     * Test BigFraction exception handling using CompletableFutures
     * and the whenComplete() method.
     */
    public static CompletableFuture<Void> testFractionExceptions3() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions3()\n");

        return Stream
            // Generate results both with and without exceptions.
            .of(true, false)

            // Handle both true and false elements.
            .map(throwException -> {
                    // If boolean is true then make the demoninator 0
                    // to trigger an exception.
                    int denominator = throwException ? 0 : 1;

                    // Create and process a BigFraction.
                    return CompletableFuture
                        .supplyAsync(() ->
                                     // Run asynchronously and maybe
                                     // throw ArithmeticException.
                                     BigFraction.valueOf(100, denominator))

                        // Multiply fraction by a constant when
                        // previous stage completes.
                        .thenApply(fraction ->
                                   fraction.multiply(sBigReducedFraction))

                        // When future completes prepare results for
                        // output, either normal or exceptional.
                        .whenComplete((fraction, ex) -> {
                                if (fraction != null)
                                    sb.append("\n     result = "
                                              + fraction.toMixedString());
                                else // if (ex != null)
                                    sb.append("     exception = " + ex.getMessage());
                            })

                        // Swallow the exception.
                        .exceptionally((e) -> BigFraction.ZERO);
                })

            // Collect to a single future.
            .collect(toFuture())

            // When that future is done display the results.
            .thenCompose(___ -> {
                    // Print results.
                    display(sb.toString());
                    return sCompleted;
                });
    }
}
