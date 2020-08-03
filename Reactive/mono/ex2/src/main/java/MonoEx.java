import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply the Project Reactor subscribeOn()
 * method to asynchronously reduce, multiply, and/or display
 * BigFractions via various Mono operations, including fromCallable(),
 * subscribeOn(), map(), doOnSuccess(), blockOptional(), then(), and
 * the Scheduler.single() thread "pool".
 * https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html
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
        BigFraction unreducedFraction = BigFraction
            .valueOf(new BigInteger (sBI1),
                     new BigInteger(sBI2),
                     false);

        // Create a callable lambda expression that
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

        // Create a function lambda that converts an improper big
        // fraction into a mixed big fraction.
        Function<BigFraction, String> convertToMixedString = result -> {
            sb.append("\n     calling BigFraction::toMixedString\n");

            return result.toMixedString();
        };

        return Mono
            // Use fromCallable() to begin the process of
            // asynchronously reducing a big fraction.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#fromCallable-java.util.concurrent.Callable-
            .fromCallable(reduceFraction)

            // Run all the processing in a (single) background thread.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#subscribeOn-reactor.core.scheduler.Scheduler-
            .subscribeOn(Schedulers
                         // https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Schedulers.html#single--
                         .single())

            // After big fraction is reduced return a mono and use
            // map() to call a function that converts the reduced
            // fraction to a mixed fraction string.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#map-java.util.function.Function-
            .map(convertToMixedString)

            // Use doOnSuccess() to print the result after it's been
            // successfully converted to a mixed fraction.  If
            // something goes wrong doOnSuccess() will be skipped.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#doOnSuccess-java.util.function.Consumer-
            .doOnSuccess(result -> MonoEx.printResult(result, sb))

            // Return an empty mono to synchronize with the
            // AsyncTester framework!
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#then--
            .then();
    }

    /**
     * Test hybrid asynchronous BigFraction multiplication using a
     * mono and a callable, where the processing is performed in a
     * background thread and the result is printed in a blocking
     * manner by the main thread.
     */
    public static Mono<Void> testFractionMultiplicationCallable1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCallable1()\n");

        // Create a callable that multiplies two large fractions.
        Callable<BigFraction> call = () -> {
            BigFraction bf1 = new BigFraction(sF1);
            BigFraction bf2 = new BigFraction(sF2);

            // Return the result of multiplying the fractions.
            return bf1.multiply(bf2);
        };

        // Submit the call to a thread pool and store the mono future
        // it returns.
        Mono<BigFraction> mono = Mono
            // Use fromCallable() to begin the process of
            // asynchronously reducing a big fraction.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#fromCallable-java.util.concurrent.Callable-
            .fromCallable(call)

            // Run all the processing in a (single) background thread.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#subscribeOn-reactor.core.scheduler.Scheduler-
            .subscribeOn(Schedulers.single());

        // Block the calling thread until the result is available via
        // the mono future, handling any errors via an optional.
        Optional<BigFraction> result = mono
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#blockOptional--
            .blockOptional();

        sb.append("     Callable.call() = "
                  + result.map(BigFraction::toMixedString)
                  .orElse("error")
                  + "\n");

        // Display the results.
        BigFractionUtils.display(sb.toString());

        // Return an empty mono since the processing is done.
        return sVoidM;
    }

    /**
     * Test asynchronous BigFraction multiplication using a mono and a
     * callable, where the processing and the printing of the result
     * is handled in a non-blocking manner by a background thread.
     */
    public static Mono<Void> testFractionMultiplicationCallable2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCallable2()\n");

        // Create a callable that multiplies two large fractions.
        Callable<BigFraction> call = () -> {
            BigFraction bf1 = new BigFraction(sF1);
            BigFraction bf2 = new BigFraction(sF2);

            // Return the result of multiplying the fractions.
            return bf1.multiply(bf2);
        };

        // Submit the call to a thread pool and process the result it
        // returns asynchronously.
        return Mono
            // Use fromCallable() to begin the process of
            // asynchronously reducing a big fraction.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#fromCallable-java.util.concurrent.Callable-
            .fromCallable(call)

            // Run all the processing in a (single) background thread.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#subscribeOn-reactor.core.scheduler.Scheduler-
            .subscribeOn(Schedulers.single())

            // Use doOnSuccess() to print the result after it's been
            // successfully converted to a mixed fraction.  If an
            // exception is thrown doOnSuccess() will be skipped.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#doOnSuccess-java.util.function.Consumer-
            .doOnSuccess(bigFraction ->
                         MonoEx.printResult(bigFraction, sb))
                         
            // Return an empty mono to synchronize with the
            // AsyncTester framework.
            // https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#then--
            .then();
    }

    /**
     * Print the BigFraction {@code bf} after first reducing it.
     */
    private static void printResult(BigFraction bf,
                                    StringBuilder sb) {
        sb.append("     mixed reduced fraction = "
                  + bf.toMixedString()
                  + "\n");
        BigFractionUtils.display(sb.toString());
    }

    /**
     * Print the {@code mixedString}.
     */
    private static void printResult(String mixedString,
                                    StringBuilder sb) {
        sb.append("     mixed reduced fraction = "
                  + mixedString
                  + "\n");
        BigFractionUtils.display(sb.toString());
    }
}
