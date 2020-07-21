package ex;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
 * asynchronously to perform advanced Flux operations, including
 * flatMap(), collectList(), zipWith(), first(), take(), when(),
 * subscribeOn(), create(), and various thread pools.
 */
public class FluxEx {
    /**
     * Test BigFraction exception handling using an asynchronous Flux
     * stream and a pool of threads.
     */
    public static Mono<Void> testFractionExceptions() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionExceptions1()\n");

        // Create a synchronizer to manage completion.
        CountDownLatch latch = new CountDownLatch(1);

        Flux
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
                                          + sBigReducedFraction.toMixedString());
                                // When mono completes multiply and
                                // store it in output.
                                return fraction.multiply(sBigReducedFraction);
                            });
                })

            // Start all the processing in motion.
            .subscribe(fraction ->
                       // Add next fraction to the string buffer.
                       sb.append(" = " + fraction.toMixedString() + "\n"),

                       // Handle error result.
                       error -> sb.append("error"),

                       // Handle final completion.
                       () -> {
                           // Display results when all processing is done.
                           BigFractionUtils.display(sb.toString());

                           // Release the latch.
                           latch.countDown();
                       });

        try {
            // Wait for the flux to complete all its processing.
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Return empty mono.
        return sVoidM;
    }

    /**
     * Test BigFraction multiplications using a stream of monos and a
     * pipeline of operations, including flatMap(), collectList(), and
     * first().
     */
    public static Mono<Void> testFractionMultiplications1() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications1()\n");

        // This function reduces/multiplies a big fraction asynchronously.
        Function<BigFraction, Mono<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> Mono
            // Omit one item that performs the reduction.
            .fromCallable(() -> BigFraction.reduce(unreducedFraction))

            // Perform all processing asynchronously in a pool of
            // background threads.
            .subscribeOn(Schedulers.parallel())

            // Return a mono to a multiplied big fraction.
            .flatMap(reducedFraction -> Mono
                     // Multiply the big fractions
                     .fromCallable(() -> reducedFraction
                                   .multiply(sBigReducedFraction))
                                   
                     // Perform all processing asynchronously in a
                     // pool of background threads.
                     .subscribeOn(Schedulers.parallel()));

        sb.append("     Printing sorted results:");

        // Process the function in a flux stream.
        return Flux
                // Generate a stream of random, large, and unreduced
                // big fractions.
                .<BigFraction>create(sink ->
                        sink.onRequest(size ->
                                sink.next(BigFractionUtils
                                        .makeBigFraction(new Random(),
                                                         false))))

            // Stop after generating sMAX_FRACTIONS big fractions.
            .take(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .flatMap(reduceAndMultiplyFraction)

            // Collect the results into a list.
            .collectList()

            // Process the results of the collected list.
            .flatMap(list ->
                     // Sort and print the results after all async
                     // fraction reductions complete.
                     BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Test BigFraction multiplications by combining the Java streams
     * framework with the Reactor framework and the common fork-join pool.
     */
    public static Mono<Void> testFractionMultiplications2() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications2()\n");

        // Function asynchronously reduces/multiplies a big fraction.
        // This function reduces/multiplies a big fraction asynchronously.
        Function<BigFraction, Mono<BigFraction>> reduceAndMultiplyFraction =
            unreducedFraction -> Mono
            // Omit one item that performs the reduction.
            .fromCallable(() -> BigFraction.reduce(unreducedFraction))

            // Perform all processing asynchronously in the common
            // fork-join pool.
            .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()))

            // Return a mono to a multiplied big fraction.
            .flatMap(reducedFraction -> Mono
                     // Multiply the big fractions
                     .fromCallable(() -> reducedFraction
                                   .multiply(sBigReducedFraction))
                                   
                     // Perform all processing asynchronously in the
                     // common fork-join pool.
                     .subscribeOn(Schedulers
                                  .fromExecutor(ForkJoinPool.commonPool())));

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
            .map(reduceAndMultiplyFraction)

            // Trigger intermediate operation processing and return a
            // mono to a list of big fractions that are being reduced
            // and multiplied asynchronously.
            .collect(toMono())

            // After all the asynchronous fraction reductions have
            // completed sort and print the results.
            .flatMap(list -> BigFractionUtils.sortAndPrintList(list,
                                                               sb));
    }
}
