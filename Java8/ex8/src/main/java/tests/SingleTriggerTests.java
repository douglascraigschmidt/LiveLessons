package tests;

import utils.BigFraction;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static utils.BigFractionUtils.*;

/**
 * Tests that showcase {@link CompletableFuture} completion stage
 * methods that trigger after a single future completes.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public final class SingleTriggerTests {
    /**
     * Test BigFraction reduction using a CompletableFuture and a
     * chain of completion stage "normal" (i.e., non-*Async())
     * methods.
     */
public static CompletableFuture<Void> testFractionReduction() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionReduction()\n");

        Supplier<BigFraction> reduceFraction = () -> {
            // Reduce the big fraction.
            BigFraction reducedFraction = BigFraction
            .reduce(sUnreducedFraction);

            sb.append("     unreducedFraction "
                      + sUnreducedFraction
                      + "\n     reduced improper fraction = "
                      + reducedFraction);

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
            display(sb.toString());
        };

        return CompletableFuture
            // Asynchronously reduce the unreduced big fraction.
            .supplyAsync(reduceFraction)

            // After the big fraction is reduced then return a future
            // to a computation that converts it into a string in
            // mixed fraction format.
            .thenApply(convertToMixedString)

            // Print result after converting it to a mixed fraction.
            .thenAccept(printResult);
    }

    /**
     * Test BigFraction reduction using a CompletableFuture and a
     * chain of completion stage *Async() methods.
     */
    public static CompletableFuture<Void> testFractionReductionAsync() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionReductionAsync()\n");

        Supplier<BigFraction> reduceFraction = () -> {
            // Reduce the big fraction.
            BigFraction reducedFraction =
                BigFraction.reduce(sUnreducedFraction);

            sb.append("     unreducedFraction "
                      + sUnreducedFraction
                      + "\n     reduced improper fraction = "
                      + reducedFraction);

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
            display(sb.toString());
        };

        return CompletableFuture
            // Asynchronously reduce the unreduced big fraction.
            .supplyAsync(reduceFraction)

            // After the big fraction is reduced then return a future
            // to a computation that converts it into a string in
            // mixed fraction format.
            .thenApplyAsync(convertToMixedString)

            // Print result after converting it to a mixed fraction.
            .thenAcceptAsync(printResult);
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and its
     * runAsync() and join() methods.
     */
    public static CompletableFuture<Void> testFractionMultiplicationRunAsync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationRunAsync()\n");

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        CompletableFuture
            // Initiate an async task whose supplier multiplies two
            // large fractions.
            .runAsync(() -> {
                    BigFraction bf1 = new BigFraction(sF1);
                    BigFraction bf2 = new BigFraction(sF2);
                    
                    // Complete the future once the computation is
                    // finished.
                    future.complete(bf1.multiply(bf2));
                });
        // Do something interesting here...

        // Print the result, blocking until it's ready.
        sb.append("     runAsync() result = "
                  + future.join().toMixedString());
        display(sb.toString());

        return sCompleted;
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and
     * its supplyAsync() factory method and join() method.
     */
    public static CompletableFuture<Void> testFractionMultiplicationSupplyAsync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationSupplyAsync()\n");

        // Create a future that completes when the supplier submitted
        // to the common fork-join pool completes.
        var future = CompletableFuture
            // Initiate an async task whose supplier multiplies two
            // large fractions.
            .supplyAsync(() -> {
                    BigFraction bf1 = new BigFraction(sF1);
                    BigFraction bf2 = new BigFraction(sF2);
                    
                    // Return the result of multiplying the fractions.
                    return bf1.multiply(bf2);
                });
        // Do something interesting here...

        // Print the result, blocking until it's ready.
        sb.append("     supplyAsync() result = " 
                  + future.join().toMixedString());
        display(sb.toString());

        return sCompleted;
    }

    /**
     * Test BigFraction multiplication using a CompletableFuture and
     * its completeAsync() factory method and join() method.
     */
    public static CompletableFuture<Void> testFractionMultiplicationCompleteAsync() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCompleteAsync()\n");

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        // Register an action that appends a string when run.
        future
            .thenRun(() -> sb.append("     completeAsync() result = "));

        future
            // Complete this future with the result of multiplying two
            // large fractions together.
            .completeAsync(() -> {
                    // Multiply two large fractions.
                    BigFraction bf1 = new BigFraction(sF1);
                    BigFraction bf2 = new BigFraction(sF2);

                    // Return the result of multiplying the fractions.
                    return bf1.multiply(bf2);
                });

        // Append the result, blocking until it's ready.
        sb.append(future.join().toMixedString());

        // Print the result.
        display(sb.toString());
        return sCompleted;
    }
}
