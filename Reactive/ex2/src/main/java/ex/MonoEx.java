package ex;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously to perform advanced Mono operations, including
 * subscribeOn(), and various thread pools.
 */
public class MonoEx {
    /**
     * Test asynchronous BigFraction reduction using a Mono and a
     * pipeline of operations that run in the background (i.e., off
     * the calling thread).
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
                      + result
                      + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return Mono
            // Use fromCallable() to begin the process of
            // asynchronously reducing a big fraction.
            .fromCallable(reduceFraction)

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

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

        // Submit the call to a thread pool and store the future it
        // returns.
        Mono<BigFraction> mono = Mono
            .fromCallable(call)

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single());

        // Block until the result is available, handling any errors
        // via an optional.
        Optional<BigFraction> result = mono.blockOptional();

        sb.append("     Callable.call() = "
                  + result.map(BigFraction::toMixedString)
                          .orElse("error")
                  + "\n");

        // Display the results.
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
        Mono<BigFraction> m1 = Mono
            .fromCallable(() ->
                          BigFractionUtils.makeBigFraction(random, true)
                          .multiply(sBigReducedFraction))

            // Run all the processing in a thread pool.
            .subscribeOn(Schedulers.parallel());

        // Create another random BigFraction and reduce/multiply it
        // asynchronously.
        Mono<BigFraction> m2 = Mono
            .fromCallable(() ->
                          BigFractionUtils.makeBigFraction(random, true)
                          .multiply(sBigReducedFraction))

            // Run all the processing in a thread pool.
            .subscribeOn(Schedulers.parallel());
        
        // Create a consumer that prints the result as a mixed
        // fraction after it's added together.
        Consumer<BigFraction> mixedFractionPrinter = bigFraction
            -> { 
            sb.append("     combined result = " 
                      + bigFraction.toMixedString()
                      + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return m1
            // Wait until m1 and m2 both complete and then add the
            // results.
            .zipWith(m2,
                     BigFraction::add)

            // Print result after converting it to a mixed fraction.
            .doOnSuccess(mixedFractionPrinter)

            // Return an empty mono to synchronize with the AsyncTester.
            .then();
    }
}
