import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply RxJava to asynchronously reduce,
 * multiply, and display BigFractions via various Single operations,
 * including fromCallable(), subscribeOn(), map(), doOnSuccess(),
 * blockingGet(), ignoreElement(), and the Scheduler.single() thread
 * "pool".
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class SingleEx {
    /**
     * Create a new unreduced BigFraction.
     */
    private static final BigFraction sUnreducedFraction = BigFraction
        .valueOf(new BigInteger (sBI1),
                 new BigInteger(sBI2),
                 false);

    // Create a callable that multiplies two large fractions.
    private static final Callable<BigFraction> sCall = () -> {
        BigFraction bf1 = new BigFraction(sF1);
        BigFraction bf2 = new BigFraction(sF2);

        // Return the result of multiplying the fractions.
        return bf1.multiply(bf2);
    };

    /**
     * The amount of time to wait in the Single.blockingGet() call.
     */
    private static final long sBLOCK_TIME = 500;

    /**
     * Test asynchronous BigFraction reduction using a Single and a
     * pipeline of operations that run in the background (i.e., off
     * the calling thread).
     */
    public static Completable testFractionReductionAsync1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionReductionAsync1()\n");

        return Single
            // Use fromCallable() to begin the process of
            // asynchronously reducing a big fraction.
            .fromCallable(() -> BigFraction
                          // Reduce the BigFraction.
                          .reduce(sUnreducedFraction))

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Use doOnSuccess() to print the BigFraction. If
            // something goes wrong doOnSuccess() will be skipped.
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

            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }

    /**
     * Test asynchronous BigFraction reduction using a Single and a
     * chain of operators that run in the background (i.e., off the
     * calling thread), but the result is printed in a timed-blocking
     * manner by the main thread.
     */
    public static Completable testFractionReductionAsync2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionReductionAsync2()\n");

        String result = Single
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

            // Signal a TimeoutException if the current Single doesn't
            // signal a success value within 500 milliseconds.
            .timeout(sBLOCK_TIME, TimeUnit.MILLISECONDS)

            // Block the calling thread until the result is available
            // via the single future.
            .blockingGet();

        // Append the result.
        sb.append("     result = "
                  + result
                  + "\n");

        // Display the results.
        BigFractionUtils.display(sb.toString());

        // Return an empty Single since the processing is done.
        return sVoidS;
    }

    /**
     * Test hybrid asynchronous BigFraction multiplication using a
     * single and a callable, where the processing is performed in a
     * background thread and the result is printed in a blocking
     * manner by the main thread.
     */
    public static Completable testFractionMultiplicationCallable1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCallable1()\n");

        // Submit the call to a thread pool and store the single
        // future it returns.
        BigFraction result = Single
            // Use fromCallable() to begin the process of
            // asynchronously reducing a big fraction.
            .fromCallable(sCall)

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Block the calling thread until the result is available
            // via the single future.
            .blockingGet();

        // Append the result.
        sb.append("     Callable.call() = "
                  + result.toMixedString()
                  + "\n");

        // Display the results.
        BigFractionUtils.display(sb.toString());

        // Return an empty single since the processing is done.
        return sVoidS;
    }

    /**
     * Test asynchronous BigFraction multiplication using a Single and a
     * callable, where the processing and the printing of the result
     * is handled in a non-blocking manner by a background thread.
     */
    public static Completable testFractionMultiplicationCallable2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationCallable2()\n");

        // Submit the call to a thread pool and process the result it
        // returns asynchronously.
        return Single
            // Use fromCallable() to begin the process of
            // asynchronously reducing a big fraction.
            .fromCallable(sCall)

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Use doOnSuccess() to print the result after it's been
            // successfully converted to a mixed fraction.  If an
            // exception is thrown doOnSuccess() will be skipped.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))
                         
            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }

    /**
     * Test asynchronous BigFraction multiplication using a mono and a
     * callable, where the processing and the printing of the result
     * is handled in a non-blocking manner by a background thread and
     * exceptions are handled gracefully.
     */
    public static Completable testFractionMultiplicationErrorHandling() {
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
                 Single<? extends BigFraction>> errorHandler = t -> {
            // If exception occurred return 0.
            sb.append("     exception = "
                      + t.getMessage()
                      + "\n");

            return Single
            // Convert error to 0.
            .just(BigFraction.ZERO);
        };

        // Submit the call to a thread pool and process the result it
        // returns asynchronously.
        return Single
            // Use fromCallable() to reduce the big fraction (which
            // will thrown ArithmeticException).
            .fromCallable(badCall)

            // Run all the processing in a (single) background thread.
            .subscribeOn(Schedulers.single())

            // Convert ArithmeticException to 0.
            .onErrorResumeNext(errorHandler)

            // Use doOnSuccess() to print the result after it's been
            // converted to the value 0 since the onErrorResume()
            // method catches the exception and returns a 0 value.  If
            // something goes wrong doOnSuccess() is skipped.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))
                         
            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }
}
