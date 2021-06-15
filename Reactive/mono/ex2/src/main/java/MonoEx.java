import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply Project Reactor to asynchronously
 * reduce, multiply, and display BigFractions via Project Reactor Mono
 * operators, including fromCallable(), just(), map(), doOnSuccess(),
 * blockOptional(), onErrorResume(), then(), subscribeOn(), and the
 * Scheduler.single() thread "pool".
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class MonoEx {
    /**
     * Create a new unreduced BigFraction.
     */
    private static final BigFraction sUnreducedFraction = BigFraction
        .valueOf(new BigInteger (sBI1),
                 new BigInteger(sBI2),
                 false);

    /**
     * Create a callable lambda that multiplies two large fractions.
     */
    private static final Callable<BigFraction> sCall = () -> {
        BigFraction bf1 = new BigFraction(sF1);
        BigFraction bf2 = new BigFraction(sF2);

        // Return the result of multiplying the fractions.
        return bf1.multiply(bf2);
    };

    /**
     * The amount of time to wait in the Mono.block() call.
     */
    private static final Duration sBLOCK_TIME = Duration.ofMillis(500);

    /**
     * Test asynchronous BigFraction reduction using a Mono and a
     * chain of operators that run in the background (i.e., off
     * the calling thread).
     */
    public static Mono<Void> testFractionReductionAsync1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionReductionAsync1()\n");

        return Mono
            // Use fromCallable() to reduce the big fraction.
            .fromCallable(() -> BigFraction
                          // Reduce the BigFraction.
                          .reduce(sUnreducedFraction))

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Use doOnSuccess() to print the BigFraction.  If
            // something goes wrong doOnSuccess() is skipped.
            .doOnSuccess(bf -> 
                         logBigFraction(sUnreducedFraction, bf, sb))

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
     * Test asynchronous BigFraction reduction using a Mono and a
     * chain of operators that run in the background (i.e., off
     * the calling thread), but the result is printed in a
     * timed-blocking manner by the main thread.
     */
    public static Mono<Void> testFractionReductionAsync2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionReductionAsync2()\n");

        String result = Mono
            // Use fromCallable() to reduce the big fraction.
            .fromCallable(() -> BigFraction
                          // Reduce the BigFraction.
                          .reduce(sUnreducedFraction))

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Use doOnSuccess() to print the BigFraction. If
            // something goes wrong doOnSuccess() is skipped.
            .doOnSuccess(bf -> logBigFraction(sUnreducedFraction, bf, sb))

            // After big fraction is reduced return a mono and use
            // map() to call a function that converts the reduced
            // fraction to a mixed fraction string.
            .map(BigFraction::toMixedString)

            // Block the calling thread until the result is available
            // via the mono or the timeout elapses.
            .block(sBLOCK_TIME);

        // Append the result.
        sb.append("     result = " + result + "\n");

        // Display the results.
        BigFractionUtils.display(sb.toString());

        // Return an empty mono since the processing is done.
        return sVoidM;
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

        // Submit the call to a thread pool and store an Optional that
        // it returns.
        Optional<BigFraction> result = Mono
            // Use fromCallable() to reduce the big fraction.
            .fromCallable(sCall)

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Block the calling thread until the result is available
            // via the mono, handling any errors via an optional.
            .blockOptional();

        sb.append("     Callable.call() = "
                  + result
                  .map(BigFraction::toMixedString)
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

        // Submit the call to a thread pool and process the result it
        // returns asynchronously.
        return Mono
            // Use fromCallable() to reduce the big fraction.
            .fromCallable(sCall)

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Use doOnSuccess() to display the result (which
            // displayMixedBigFraction() converts to a mixed
            // fraction). If something goes wrong doOnSuccess() is
            // skipped.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))
                         
            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Test asynchronous BigFraction multiplication using a mono and a
     * callable, where the processing and the printing of the result
     * is handled in a non-blocking manner by a background thread and
     * exceptions are handled gracefully.
     */
    public static Mono<Void> testFractionMultiplicationErrorHandling() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationErrorHandling()\n");

        // Create a callable that multiplies two large fractions,
        // but which will throw the ArithmeticException.
        Callable<BigFraction> badCall = () -> {
            BigFraction numerator = new BigFraction(sF1);
            // Make the denominator invalid!
            BigFraction denominator = new BigFraction("0");

            // Return the result of dividing the fractions.
            return numerator.divide(denominator);
        };

        // Create a function lambda to handle an ArithmeticException.
        Function<Throwable,
            Mono<? extends BigFraction>> errorHandler = t -> {
            // If exception occurred return 0.
            sb.append("     exception = "
                      + t.getMessage()
                      + "\n");

            return Mono
            // Convert error to 0.
            .just(BigFraction.ZERO);
        };

        // Submit the call to a thread pool and process the result it
        // returns asynchronously.
        return Mono
            // Use fromCallable() to reduce the big fraction (which
            // will thrown ArithmeticException).
            .fromCallable(badCall)

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Convert ArithmeticException to 0.
            .onErrorResume(errorHandler)

            // Use doOnSuccess() to print the result after it's been
            // converted to the value 0 since the onErrorResume()
            // method catches the exception and returns a 0 value.  If
            // something goes wrong doOnSuccess() is skipped.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))
                         
            // Return an empty mono to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }
}
