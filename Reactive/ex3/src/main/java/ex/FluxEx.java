package ex;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;
import static utils.MonosCollector.toMono;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously to perform a range of Flux operations, including
 * fromCallable(), map(), flatMap(), collectList(), zipWith(),
 * first(), take(), when(), onErrorResult(), subscribeOn(), create(),
 * and various types of thread pools.  It also demonstrates how to
 * combine the Java streams framework with the Project Reactor
 * framework.
 */
public class FluxEx {
    /**
     * Test BigFraction exception handling using an asynchronous Flux
     * stream and a pool of threads.
     */
    public static Mono<Void> testFractionExceptions() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionExceptions1()\n");

        return Flux
            // Use a Flux to generate a stream of denominators.
            .just(3, 4, 2, 0, 1)

            // Iterate through the elements using the flatMap()
            // concurrency idiom.
            .flatMap(denominator -> {
                    // Create/process a BigFraction synchronously.
                    return Mono
                        .fromCallable(() ->
                                      // May throw
                                      // ArithmeticException.
                                      BigFraction.valueOf(100,
                                                          denominator))

                        // Run all the processing in a pool of
                        // background threads.
                        .subscribeOn(Schedulers.parallel())

                        // Handle exception and convert result to 0.
                        .onErrorResume(t -> {
                                // If exception occurred return 0.
                                sb.append("     exception = "
                                          + t.getMessage()
                                          + "\n");

                                // Convert error to 0.
                                return Mono.just(BigFraction.ZERO);
                            })

                        // Perform a multiplication.
                        .map(fraction -> {
                                sb.append("     "
                                          + fraction.toMixedString()
                                          + " x "
                                          + sBigReducedFraction.toMixedString()
                                          + "\n");
                                // When mono completes multiply it.
                                return fraction.multiply(sBigReducedFraction);
                            });
                })

            // Collect the results into a list.
            .collectList()

            // Process results of the collected list and return a mono
            // used to synchronize with the AsyncTester framework.
            .flatMap(list ->
                     // Sort and print the results after all async
                     // fraction reductions complete.
                     BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Test BigFraction multiplications using a stream of monos and a
     * pipeline of operations, including flatMap(), collectList(), and
     * first().
     */
    public static Mono<Void> testFractionMultiplications1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications1()\n");

        sb.append("     Printing sorted results:");

        // Process the function in a flux stream.
        return Flux
            // Generate a stream of random, large, and unreduced big
            // fractions.
            .<BigFraction>
            create(sink ->
                   sink.onRequest(size ->
                                  sink.next(BigFractionUtils
                                            .makeBigFraction(new Random(),
                                                             false))))

            // Stop after generating sMAX_FRACTIONS big fractions.
            .take(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .flatMap(unreducedFraction ->
                 reduceAndMultiplyFraction(unreducedFraction,
                                           Schedulers.parallel()))

            // Collect the results into a list.
            .collectList()

            // Process the results of the collected list and return a
            // mono that's used to synchronize with the AsyncTester
            // framework.
            .flatMap(list ->
                     // Sort and print the results after all async
                     // fraction reductions complete.
                     BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Test BigFraction multiplications by combining the Java streams
     * framework with the Project Reactor framework and the Java
     * common fork-join framework.
     */
    public static Mono<Void> testFractionMultiplications2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications2()\n");

        sb.append("     Printing sorted results:");

        // Process the function in a sequential stream.
        return Stream
            // Generate a stream of random, large, and unreduced big
            // fractions.
            .generate(() ->
                      BigFractionUtils.makeBigFraction(new Random(),
                                                       false))

            // Stop after generating sMAX_FRACTIONS big fractions.
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(unreducedBigFraction ->
                 reduceAndMultiplyFraction(unreducedBigFraction,
                                           Schedulers
                                           .fromExecutor(ForkJoinPool
                                                         .commonPool())))

            // Trigger intermediate operation processing and return a
            // mono to a list of big fractions that are being reduced
            // and multiplied asynchronously.
            .collect(toMono())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .flatMap(list -> BigFractionUtils.sortAndPrintList(list,
                                                               sb));
    }

    /**
     * This factory method returns a mono that's signaled after the
     * {@code unreducedFraction} is reduced/multiplied asynchronously
     * in background threads from the given {@code scheduler}.
     */
    private static Mono<BigFraction>
        reduceAndMultiplyFraction(BigFraction unreducedFraction,
                                  Scheduler scheduler) {
        return Mono
            // Omit one item that performs the reduction.
            .fromCallable(() ->
                          BigFraction.reduce(unreducedFraction))

            // Perform all processing asynchronously in a pool of
            // background threads.
            .subscribeOn(scheduler);

            // Return a mono to a multiplied big fraction.
            .flatMap(reducedFraction -> Mono
                     // Multiply the big fractions
                     .fromCallable(() -> reducedFraction
                                   .multiply(sBigReducedFraction))
                                   
                     // Perform all processing asynchronously in a
                     // pool of background threads.
                     .subscribeOn(scheduler));

    }
}
