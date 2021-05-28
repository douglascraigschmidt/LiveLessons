import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously to perform a range of Flux operations, including
 * fromIterable(), generate(), map(), flatMap(), onErrorResume(),
 * collectList(), collect(), reduce(), take(), filter(), and various
 * types of thread pools.  It also shows various Mono operations, such
 * as flatMap(), firstWithSignal(), subscribeOn(), and the parallel
 * thread pool.
 */
@SuppressWarnings("ALL")
public class FluxEx {
    /**
     * Create a random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * Test BigFraction exception handling using a synchronous Flux
     * stream (with asynchrony only at the end of the stream).
     */
    public static Mono<Void> testFractionException1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionException1()\n");

        // Create a list of denominators, including 0 that will
        // trigger an ArithmeticException.
        List<Integer> denominators = List.of(3, 4, 2, 1, 0, 1);

        // Create a function lambda to handle an ArithmeticException.
        Function<Throwable,
                 Flux<BigFraction>> errorHandler = t -> {
            // Record the exception message.
            sb.append("     exception = "
                      + t.getMessage()
                      + "\n");

            // Return an empty Flux when an exception occurs.
            return Flux.empty();
        };

        return Flux
            // Generate a Flux stream from the denominators list.
            .fromIterable(denominators)

            // Generate a random BigFraction.
            .map(denominator -> BigFraction
                 // Throws ArithmeticException if
                 // denominator is 0.
                 .valueOf(Math.abs(sRANDOM.nextInt()),
                          denominator))

            // Catch ArithmeticException and return an empty Flux.
            .onErrorResume(errorHandler)

            // Collect the non-empty BigFractions into a list.
            .collectList()

            // Process the collected list and return a mono used to
            // synchronize with the AsyncTaskBarrier framework.
            .flatMap(list ->
                     // Sort and print the results after all sync
                     // fraction reductions complete.
                     BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Test BigFraction exception handling using an asynchronous Flux
     * stream and a pool of threads.
     */
    public static Mono<Void> testFractionException2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionException2()\n");

        // Create a function lambda to handle an ArithmeticException.
        Function<Throwable,
            Mono<? extends BigFraction>> errorHandler = t -> {
            // Record the exception message.
            sb.append("     exception = "
                      + t.getMessage()
                      + "\n");

            return Mono
            // Convert the exception to 0.
            .just(BigFraction.ZERO);
        };

        // Create a list of denominators, including 0 that will
        // trigger an ArithmeticException.
        List<Integer> denominators = List.of(3, 4, 2, 0, 1);

        return Flux
            // Generate a Flux stream from the denominators list.
            .fromIterable(denominators)

            // Iterate through the elements using the flatMap()
            // concurrency idiom.
            .flatMap(denominator -> Mono
                     // Create/process each denominator asynchronously
                     // via an "inner publisher".
                     .fromCallable(() ->
                                   // Throws ArithmeticException if
                                   // denominator is 0.
                                   BigFraction.valueOf(Math.abs(sRANDOM.nextInt()),
                                                       denominator))

                     // Run all the processing in a pool of
                     // background threads.
                     .subscribeOn(Schedulers.parallel())

                     // Convert ArithmeticException to 0.
                     .onErrorResume(errorHandler)

                     // Log the BigFractions.
                     .doOnNext(bf ->
                               logBigFraction(bf,
                                              sBigReducedFraction,
                                              sb))

                     // Perform a multiplication.
                     .map(bf ->
                          bf.multiply(sBigReducedFraction)))

            // Remove any big fractions that are <= 0.
            .filter(fraction -> fraction.compareTo(0) > 0)

            // Collect the BigFractions into a list.
            .collectList()

            // Process the collected list and return a mono used to
            // synchronize with the AsyncTaskBarrier framework.
            .flatMap(list ->
                     // Sort and print the results after all async
                     // fraction reductions complete.
                     BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Test an asynchronous Flux stream consisting of generate(),
     * take(), flatMap(), collect(), and a pool of threads to perform
     * BigFraction reductions and multiplications.
     */
    public static Mono<Void> testFractionMultiplications1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications1()\n");

        sb.append("     Printing sorted results:");

        // Process the function in a flux stream.
        return Flux
            // Generate a stream of random, large, and unreduced big
            // fractions.
            .generate((SynchronousSink<BigFraction> sink) -> sink
                      // Emit a random big fraction every time a
                      // request is made.
                      .next(BigFractionUtils.makeBigFraction(sRANDOM,
                                                             false)))

            // Stop after generating sMAX_FRACTIONS big fractions.
            .take(sMAX_FRACTIONS)

            // Reduce and multiply these big fractions asynchronously
            // using the flatMap() concurrency idiom.
            .flatMap(unreducedFraction ->
                     reduceAndMultiplyFraction(unreducedFraction,
                                               Schedulers.parallel(),
                                               sb))

            // Collect the results into a Mono<List<BigFraction>>.
            .collect(toList())

            // Process the results of the collected list and return a
            // mono that's used to synchronize with the
            // AsyncTaskBarrier framework.
            .flatMap(list ->
                     // Sort and print the results after all async
                     // fraction reductions complete.
                     BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Test an asynchronous Flux stream consisting of fromIterable(),
     * flatMap(), reduce(), and a pool of threads to perform
     * BigFraction reductions and multiplications.
     */
    public static Mono<Void> testFractionMultiplications2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications2()\n");

        // Create a list of BigFraction objects.
        List<BigFraction> bigFractions = List.of(BigFraction.valueOf(1000, 30),
                                                 BigFraction.valueOf(1000, 40),
                                                 BigFraction.valueOf(1000, 20),
                                                 BigFraction.valueOf(1000, 10));

        return Flux
            // Emit a stream of big fractions.
            .fromIterable(bigFractions)

            // Reduce and multiply these big fractions asynchronously
            // using the flatMap() concurrency idiom.
            .flatMap(unreducedFraction ->
                     reduceAndMultiplyFraction(unreducedFraction,
                                               Schedulers.parallel(),
                                               sb))

            // Reduce the results into one Mono<BigFraction>.
            .reduce(BigFraction::add)

            // Display the results if all goes well.
            .doOnSuccess(bf -> displayMixedBigFraction(bf, sb))

            // Return a Mono<Void> to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }

    /**
     * This factory method returns a mono that's signaled after the
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

            // Log the results.
            .doOnNext(result ->
                          logBigFractionResult(unreducedFraction,
                                               sBigReducedFraction,
                                               result,
                                               sb))

            // Return a mono to a multiplied big fraction.
            .map(reducedFraction -> reducedFraction
                 // Multiply the big fractions
                 .multiply(sBigReducedFraction));
    }
}
