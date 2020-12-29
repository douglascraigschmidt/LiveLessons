import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static utils.BigFractionUtils.sBigReducedFraction;
import static utils.BigFractionUtils.sMAX_FRACTIONS;
import static utils.MonosCollector.toMono;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously to perform a range of Flux operations, including
 * fromIterable(), create(), map(), flatMap(), collectList(),
 * collect(), take(), filter(), and various types of thread pools.  It
 * also shows various Mono operations, such as first(), when(),
 * flatMap(), subscribeOn(), and the parallel thread pool.  It also
 * demonstrates how to combine the Java streams framework with the
 * Project Reactor framework.
 */
@SuppressWarnings("ALL")
public class FluxEx {
    /**
     * Create a random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * Test BigFraction exception handling using an asynchronous Flux
     * stream and a pool of threads.
     */
    public static Mono<Void> testFractionExceptions() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionExceptions1()\n");

        // Create a function lambda to handle an ArithmeticException.
        Function<Throwable,
                 Mono<? extends BigFraction>> errorHandler = t -> {
            // If exception occurred return 0.
            sb.append("     exception = "
                      + t.getMessage()
                      + "\n");

            // Convert error to 0.
            return Mono
            .just(BigFraction.ZERO);
        };

        // Create a function lambda that multiplies big fractions.
        Function<BigFraction,
                 BigFraction> multiplyBigFractions = fraction -> {
            sb.append("     "
                      + fraction.toMixedString()
                      + " x "
                      + sBigReducedFraction.toMixedString()
                      + "\n");
            // When mono completes multiply it.
            return fraction.multiply(sBigReducedFraction);
        };

        // Create a list of denominators, including 0 that
        // will trigger an ArithmeticException.
        List<Integer> denominators = List.of(3, 4, 2, 0, 1);

        return Flux
            // Use a Flux to generate a stream from the denominators list.
            .fromIterable(denominators)

            // Iterate through the elements using the flatMap()
            // concurrency idiom.
            .flatMap(denominator -> {
                    // Create/process each denominator asynchronously via an
                    // "inner publisher".
                    return Mono
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

                        // Perform a multiplication.
                        .map(multiplyBigFractions);
                })

            // Remove any big fractions that are <= 0.
            .filter(fraction -> fraction.compareTo(0) > 0)

            // Collect the BigFractions into a list.
            .collectList()

            // Process the collected list and return a mono used
            // to synchronize with the AsyncTaskBarrier framework.
            .flatMap(list ->
                     // Sort and print the results after all async
                     // fraction reductions complete.
                     BigFractionUtils.sortAndPrintList(list, sb));
    }

    /**
     * Test BigFraction multiplications using a stream of monos and a
     * pipeline of operations, including create(), take(), flatMap(),
     * collectList(), and first().
     */
    public static Mono<Void> testFractionMultiplications1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications1()\n");

        sb.append("     Printing sorted results:");

        // A consumer that emits a stream of random big fractions.
        Consumer<FluxSink<BigFraction>> bigFractionEmitter = sink -> sink
            .onRequest(size -> sink
                       // Emit a random big fraction every time a request is made.
                       .next(BigFractionUtils.makeBigFraction(sRANDOM,
                                                              false)));

        // Process the function in a flux stream.
        return Flux
            // Generate a stream of random, large, and unreduced big
            // fractions.
            .create(bigFractionEmitter)

            // Stop after generating sMAX_FRACTIONS big fractions.
            .take(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .flatMap(unreducedFraction ->
                     reduceAndMultiplyFraction(unreducedFraction,
                                               Schedulers.parallel()))

            // Collect the results into a Mono<List>.
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
     * Use an asynchronous Observable stream and a pool of threads to
     * perform BigFraction multiplications and additions.
     */
    public static Mono<Void> testFractionMultiplications2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications2()\n");

        // Create a list of BigFraction objects.
        List<BigFraction> bigFractions = List.of(BigFraction.valueOf(1000, 30),
                                                 BigFraction.valueOf(1000, 40),
                                                 BigFraction.valueOf(1000, 20),
                                                 BigFraction.valueOf(1000, 10));

        // Create a Function that multiplies big fractions.
        Function<BigFraction, BigFraction> multiplyBigFractions = bf -> {
            // Multiply bf by a constant.
            BigFraction result = bf.multiply(sBigReducedFraction);

            sb.append("     "
                      + bf.toMixedString()
                      + " x "
                      + sBigReducedFraction.toMixedString()
                      + " = "
                      + result.toMixedString()
                      + "\n");
            return result;
        };

        // Display the results.
        Consumer<? super BigFraction> displayResults = result -> {
            sb.append("    sum of BigFractions = "
                    + result
                    + "\n");
            BigFractionUtils.display(sb.toString());
        };

        return Flux
            // Emit a stream of big fractions.
            .fromIterable(bigFractions)

            // Iterate thru the elements using Project Reactor's
            // flatMap() concurrency idiom to multiply these big
            // fractions asynchronously in a thread pool.
            .flatMap(bf -> Flux
                     // Create/process each BigFraction asynchronously
                     // via an "inner publisher".  However, just()
                     // emits the BigFraction in the "assembly thread"
                     // and *not* a background thread.
                     .just(bf)
                     
                     // Perform the processing asynchronously in a
                     // background thread from the given scheduler.
                     .subscribeOn(Schedulers.parallel())

                     // Perform the multiplication in a background
                     // thread.
                     .map(multiplyBigFractions))

            // Reduce the results into one Mono<BigFraction>.
            .reduce(BigFraction::add)

            // Display the results if all goes well.
            .doOnSuccess(displayResults)

            // Return a Mono<Void> to synchronize with the
            // AsyncTaskBarrier framework.
            .then();
    }

    /**
     * Test BigFraction multiplications by combining the Java streams
     * framework with the Project Reactor framework and the Java
     * common fork-join framework.
     */
    public static Mono<Void> testFractionMultiplications3() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplications3()\n");

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
            .flatMap(list ->
                     BigFractionUtils.sortAndPrintList(list, sb));
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
            .subscribeOn(scheduler)

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
