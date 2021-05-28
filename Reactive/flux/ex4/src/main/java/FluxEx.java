import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;
import utils.BlockingSubscriber;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;
import static utils.MonosCollector.toMono;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously to perform a range of Flux operations, including
 * fromArray(), map(), flatMap(), collect(), subscribeOn(), and
 * various types of thread pools.  It also shows various Mono
 * operations, such as when(), firstWithSignal(), materialize(),
 * flatMap(), flatMapMany(), subscribeOn(), and the parallel thread
 * pool.  In addition, it demonstrates how to combine the Java streams
 * framework with the Project Reactor framework.
 */
@SuppressWarnings("ALL")
public class FluxEx {
    /**
     * Create a random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * Test BigFraction multiplications by combining the Java streams
     * framework with the Project Reactor framework and the Java
     * common fork-join framework.
     */
    public static Mono<Void> testFractionMultiplicationsStreams() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationsStreams()\n");

        sb.append("     Printing sorted results:");

        // Process the function in a sequential stream.
        return Stream
            // Generate a stream of random, large, and unreduced big
            // fractions.
            .generate(() -> BigFractionUtils
                      .makeBigFraction(sRANDOM,false))

            // Stop after generating sMAX_FRACTIONS big fractions.
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(unreducedBigFraction ->
                 reduceAndMultiplyFraction(unreducedBigFraction,
                                           Schedulers
                                           // Use the common fork-join pool.
                                           .fromExecutor(ForkJoinPool
                                                         .commonPool()),
                                           sb))

            // Trigger intermediate operation processing and return a
            // mono to a list of big fractions that are being reduced
            // and multiplied asynchronously.
            .collect(toMono())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .flatMap(list ->
                     BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * A test of BigFraction multiplication using an asynchronous Flux
     * stream and a blocking Subscriber implementation.
     */
    public static Mono<Void> testFractionMultiplicationsBlockingSubscriber() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationsBlockingSubscriber()\n");

        // Add some useful diagnostic output.
        sb.append("["
                  + Thread.currentThread().getId()
                  + "] "
                  + " Starting async processing.\n");

        // Create a blocking subscriber that processes various
        // types of signals.
        BlockingSubscriber<BigFraction> blockingSubscriber = 
            new BlockingSubscriber<>
            (bf ->
             // Add fraction to the string buffer.
             sb.append(" = " + bf.toMixedString() + "\n"),
             t -> {
                // Append the error message to the StringBuilder.
                sb.append(t.getMessage());

                // Display results when processing is done.
                BigFractionUtils.display(sb.toString());
             },
             // Display results when processing is done.
             () -> BigFractionUtils.display(sb.toString())
             ,
             Long.MAX_VALUE);

        Flux
            // "Loop" for sMAX_FRACTIONS iterations.
            .range(1, sMAX_FRACTIONS)

            // Reduce and multiply random large big fractions using
            // the flatMap() concurrency idiom.
            .flatMap(__ -> Mono
                     // Generate a random large BigFraction.
                     .fromCallable(() -> BigFractionUtils
                                   .makeBigFraction(sRANDOM, true))

                     // Transform the BigFraction emitted by this Mono
                     // into a Publisher, then forward its emissions
                     // into the returned Flux.
                     .flatMapMany(unreducedBigFraction ->
                                  // Reduce and multiply the
                                  // BigFraction asynchronously.
                                  reduceAndMultiplyFraction(unreducedBigFraction,
                                                            Schedulers.parallel(),
                                                            sb)))

            // Use subscribe() to initiate all the processing and
            // handle the results asynchronously.
            .subscribe(blockingSubscriber);

        // Add some useful diagnostic output.
        sb.append("["
                  + Thread.currentThread().getId()
                  + "] "
                  + " Waiting for async computations to complete.\n");

        // Wait for all async computations to complete and return an empty mono
        // to indicate to the AsyncTaskBarrier that all the processing is done.
        return blockingSubscriber.await();
    }

    /**
     * This factory method returns a Mono that's signaled after the
     * {@code unreducedFraction} is reduced/multiplied asynchronously
     * in background threads from the given {@link Scheduler}.
     */
    private static Mono<BigFraction> reduceAndMultiplyFraction
        (BigFraction unreducedFraction,
         Scheduler scheduler,
         StringBuffer sb) {
        return Mono
            // Omit one item that performs the reduction.
            .fromCallable(() -> BigFraction
                          .reduce(unreducedFraction))

            // Perform all processing asynchronously in a pool of
            // background threads.
            .subscribeOn(scheduler)

            // Return a Mono to a multiplied big fraction.
            .map(reducedFraction -> reducedFraction
                 // Multiply the big fractions
                 .multiply(sBigReducedFraction));
    }
}
