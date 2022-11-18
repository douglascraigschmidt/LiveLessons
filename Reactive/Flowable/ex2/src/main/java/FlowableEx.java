import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;
import static utils.BigFractionUtils.*;

/**
 * This class shows how to reduce and/or multiply big fractions
 * asynchronously and concurrently using RxJava {@link Flowable}
 * operators, including fromArray() and parallel(), and {@link
 * ParallelFlowable} operators, including runOn(), flatMap(),
 * sequential(), and reduce(), as well as the Schedulers.computation()
 * thread pool.
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public class FlowableEx {
    /**
     * Test a {@link ParallelFlowable} stream consisting of fromArray(),
     * parallel(), runOf(), flatMap(), reduce(), and a pool of threads
     * to perform BigFraction multiplications.
     */
    public static Completable testFractionMultiplications() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications()\n");

        // Create an array of reduced BigFraction objects.
        BigFraction[] bigFractionList = {
            BigFraction.valueOf(1000, 30),
            BigFraction.valueOf(1000, 40),
            BigFraction.valueOf(1000, 20),
            BigFraction.valueOf(1000, 10)
        };

        // Display the results.
        Consumer<? super BigFraction> displayResults = result -> {
            sb.append("    sum of BigFractions = "
                      + result
                      + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return Flowable
            // Emit a stream of reduced big fractions.
            .fromArray(bigFractionList)

            // Convert the Flowable to a ParallelFlowable.
            .parallel()

            // Run subsequent processing in the computation pool.
            .runOn(Schedulers.computation())

            // Use RxJava's flatMap() concurrency idiom to multiply
            // these BigFractions asynchronously in a thread pool.
            .map(bf -> bf
                 .multiply(sBigReducedFraction))

            // Log the BigFractions.
            .doOnNext(bf ->
                      logBigFraction(bf, sBigReducedFraction, sb))
s
            // Convert the ParallelFlowable back into a Flowable.
            .sequential()

            // Reduce the results into one Maybe<BigFraction>.
            .reduce(BigFraction::add)

            // Display the results if all goes well.
            .doOnSuccess(displayResults)

            // Return a Completable to synchronize with the
            // AsyncTaskBarrier framework.
            .ignoreElement();
    }
}
