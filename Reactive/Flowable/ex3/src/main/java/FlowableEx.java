import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import static utils.BigFractionUtils.logBigFraction;
import static utils.BigFractionUtils.sBigReducedFraction;

/**
 * This class shows how to multiply and add big fractions
 * asynchronously and concurrently using RxJava {@link Flowable}
 * operators, including fromArray() and parallel(), and {@link
 * ParallelFlowable} operators, including runOn(), flatMap(),
 * reduce(), and firstElement(), as well as the Schedulers.computation()
 * thread pool.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class FlowableEx {
    /**
     * Use a {@link ParallelFlowable} stream and a pool of threads to
     * perform {@link BigFraction} multiplications and additions in parallel.
     */
    public static Completable testFractionMultiplications() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications()\n");

        // Create an array of reduced BigFraction objects.
        BigFraction[] bigFractionArray = {
            BigFraction.valueOf(1000, 30),
            BigFraction.valueOf(1000, 40),
            BigFraction.valueOf(1000, 20),
            BigFraction.valueOf(1000, 10)
        };

        // Display the results.
        Consumer<? super BigFraction> displayResults = result -> {
            sb.append("["
                      + Thread.currentThread().getId()
                      + "] sum of BigFractions = "
                      + result
                      + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return Flowable
            // Emit a stream of reduced big fractions.
            .fromArray(bigFractionArray)

            // Convert the Flowable to a ParallelFlowable.
            .parallel()

            // Run subsequent processing in the computation pool.
            .runOn(Schedulers.computation())

            // Use RxJava's ParallelFlowable mechanism to multiply
            // these BigFraction objects in parallel in a thread pool.
            .map(bf -> bf
                 .multiply(sBigReducedFraction))

            // Log the BigFractions.
            .doOnNext(bf ->
                      logBigFraction(bf, sBigReducedFraction, sb))

            // Reduce all values within a 'rail' and across 'rails' via
            // BigFraction::add into a Flowable sequence with one element.
            .reduce(BigFraction::add)

            // Return a Maybe that emits the one and only element in the Flowable.
            .firstElement()

            // Display the results if all goes well.
            .doOnSuccess(displayResults)

            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }
}
