import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.Callable;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply Project Reactor to asynchronously
 * reduce, multiply, and display BigFractions via various Mono
 * operations, including fromCallable(), subscribeOn(), map(),
 * doOnSuccess(), blockOptional(), first(), then(), and the
 * Scheduler.single() thread "pool".
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class MonoEx {
    /**
     * Test asynchronous BigFraction reduction using a Mono and a
     * pipeline of operations that run in the background (i.e., off
     * the calling thread).
     */
    public static Mono<Void> testFractionReductionAsync() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionReductionAsync()\n");

        // Create a new unreduced big fraction.
        BigFraction unreducedFraction = BigFraction
            .valueOf(new BigInteger (sBI1),
                     new BigInteger(sBI2),
                     false);

        return Mono
            // Use fromCallable() to begin the process of
            // asynchronously reducing a big fraction.
            .fromCallable(() -> BigFraction
                          // Reduce the BigFraction.
                          .reduce(unreducedFraction))

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Use doOnSuccess() to print the BigFraction. If
            // something goes wrong doOnSuccess() will be skipped.
            .doOnSuccess(bf -> 
                         logBigFraction(unreducedFraction, bf, sb))

            // After big fraction is reduced return a mono and use
            // map() to call a function that converts the reduced
            // fraction to a mixed fraction string.
            .map(BigFraction::toMixedString)

            // Use doOnSuccess() to print the result after it's been
            // successfully converted to a mixed fraction.  If
            // something goes wrong doOnSuccess() will be skipped.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))

            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
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

        // Create a callable lambda that multiplies two large fractions.
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
            .fromCallable(call)

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single());

        // Block the calling thread until the result is available via
        // the mono future, handling any errors via an optional.
        Optional<BigFraction> result = mono
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
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationCallable2()\n");

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
            .fromCallable(call)

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Use doOnSuccess() to print the result after it's been
            // successfully converted to a mixed fraction.  If an
            // exception is thrown doOnSuccess() will be skipped.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))
                         
            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }
}
