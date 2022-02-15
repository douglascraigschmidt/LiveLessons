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

        // Create an array of BigFraction objects.
        BigFraction[] bigFractionArray = {
            BigFraction.valueOf(100, 3),
            BigFraction.valueOf(100, 4),
            BigFraction.valueOf(100, 2),
            BigFraction.valueOf(100, 1)
        };

        // Create a blocking subscriber that processes various
        // types of signals.
        BlockingSubscriber<BigFraction> blockingSubscriber = 
            new BlockingSubscriber<>
            (bf ->
             // Add fraction to the string buffer.
             sb.append("Result = " + bf.toMixedString() + "\n"),
             t -> {
                // Append the error message to the StringBuilder.
                sb.append(t.getMessage());

                // Display results when processing is done.
                BigFractionUtils.display(sb.toString());
             },
             () -> {
                 // Add some useful diagnostic output.
                 sb.append("["
                           + Thread.currentThread().getId()
                           + "] "
                           + " Async computations complete.\n");
                 // Display results when processing is done.
                 BigFractionUtils.display(sb.toString());
             },
             // There is no backpressure required.
             Long.MAX_VALUE);

        Mono
            // Generate a random large BigFraction.
            .fromCallable(() -> BigFractionUtils
                          .makeBigFraction(sRANDOM, true))

            // Transform the item emitted by this Mono into a
            // Publisher and then forward its emissions into the
            // returned Flux.
            .flatMapMany(bf1 -> Flux
                         // Generate a stream of BigFractions.
                         .fromArray(bigFractionArray)
                         
                         // Perform the Project Reactor
                         // flatMap() concurrency idiom.
                         .flatMap(bf2 ->
                                  multiplyFraction(bf1,
                                                   bf2,
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
     * Test BigFraction multiplications by combining the Java Streams
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
            .flatMap(list -> BigFractionUtils
                     .sortAndPrintList(list, sb));
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
            .flatMap(reducedFraction ->
                     multiplyFraction(reducedFraction,
                                      sBigReducedFraction,
                                      scheduler,
                                      sb));
    }

    /**
     * @return A {@link Mono} that's signaled after the {@link
     * BigFraction} is multiplied asynchronously in a background
     * thread from the given {@link Scheduler}
     */
    private static Mono<BigFraction> multiplyFraction(BigFraction bf1,
                                                      BigFraction bf2,
                                                      Scheduler scheduler,
                                                      StringBuffer sb) {
        return Mono
            // Return a Mono to a multiplied big fraction.
            .fromCallable(() -> bf1
                          // Multiply the big fractions
                          .multiply(bf2))

            // Perform processing asynchronously in a pool of
            // background threads.
            .subscribeOn(scheduler);
    }
}
