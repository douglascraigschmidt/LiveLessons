import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Stream;

import static utils.BigFractionUtils.*;
import static utils.MonosFluxCollector.toMonoFlux;
import static utils.MonosListCollector.toMonoList;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously to perform a range of Flux operations, including
 * flatMap(), collect(), subscribeOn(), and various types of thread
 * pools.  It also shows various Mono operations, such as when(),
 * firstWithSignal(), materialize(), flatMap(), flatMapMany(),
 * flatMapIterable(), subscribeOn(), and the parallel thread pool.  In
 * addition, it demonstrates how to combine the Java streams framework
 * with the Project Reactor framework.
 */
@SuppressWarnings("ALL")
public class FluxEx {
    /**
     * Create a random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * Test BigFraction multiplications by combining the Java Streams
     * framework with the Project Reactor framework and the Java
     * common fork-join framework.
     */
    public static Mono<Void> testFractionMultiplicationsStreams() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationsStreams()\n");

        sb.append("     Printing sorted results:");

        // Perform processing in a sequential stream and return a
        // Mono<Void>.
        return FluxEx
            // Get a stream of random Mono<BigFraction> objects that
            // are being reduced and multiplied concurrently.
            .getMonoStream(sb)
                
            // Trigger intermediate operation processing and return a
            // Mono to a List of BigFraction objects that are being
            // reduced and multiplied asynchronously.
            .collect(toMonoList())

            // After all asynchronous fraction reductions complete use
            // Mono.flatMap() to sort and print the results.
            .flatMap(list -> BigFractionUtils
                     .sortAndPrintList(list, sb));
    }

    /**
     * Test BigFraction multiplications by combining the Java Streams
     * framework with the Project Reactor framework and the Java
     * common fork-join framework in a slightly different way.
     */
    public static Mono<Void> testFractionMultiplicationsStreamsEx1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationsStreamsEx1()\n");

        sb.append("     Printing sorted results:");

        // Process the function in a sequential stream and return a
        // Flux.
        Flux<BigFraction> bigFractionFlux = FluxEx
            // Get a stream of random Mono<BigFraction> objects that
            // are being reduced and multiplied concurrently.
            .getMonoStream(sb)

            // Trigger intermediate operation processing and return a
            // Mono to a List of BigFraction objects that are being
            // reduced and multiplied asynchronously.
            .collect(toMonoList())

            // Convert the Mono<List<BigFraction>> to a
            // Flux<BigFraction>.
            .flatMapIterable(Function.identity());

        return BigFractionUtils
            // After all asynchronous fraction reductions have
            // completed sort and print the results.
            .sortAndPrintFlux(bigFractionFlux, sb);
    }

    /**
     * Test BigFraction multiplications by combining the Java Streams
     * framework with the Project Reactor framework and the Java
     * common fork-join framework in yet another slightly different
     * way.
     */
    public static Mono<Void> testFractionMultiplicationsStreamsEx2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationsStreamsEx2()\n");

        sb.append("     Printing sorted results:");

        // Perform processing in a sequential stream and return a
        // Flux.
        Flux<BigFraction> bigFractionFlux = FluxEx
            // Get a stream of random Mono<BigFraction> objects that
            // are being reduced and multiplied concurrently.
            .getMonoStream(sb)

            // Trigger intermediate operation processing and return a
            // Mono to a Flux of BigFraction objects that are being
            // reduced and multiplied asynchronously.
            .collect(toMonoFlux());

        return BigFractionUtils
            // After all asynchronous fraction reductions have
            // completed sort and print the results.
            .sortAndPrintFlux(bigFractionFlux, sb);
    }

    /**
     * Generate a {@link Stream} of random {@link Mono<BigFraction>}
     * objects that are being reduced and multiplied concurrently.
     *
     * @param sb The {@link StringBuffer} to store logging messages
     * @return A {@link Stream} of random {@link Mono<BigFraction>}
     *         objects that are being reduced and multiplied
     *         concurrently
     */
    private static Stream<Mono<BigFraction>> getMonoStream(StringBuffer sb) {
        return Stream
            // Generate a stream of random, large, and unreduced
            // big fractions.
            .generate(() -> makeBigFraction(sRANDOM, false))

            // Stop after generating sMAX_FRACTIONS big fractions.
            .limit(sMAX_FRACTIONS)

            // Reduce and multiply these fractions asynchronously.
            .map(unreducedBigFraction ->
                 reduceAndMultiplyFraction(unreducedBigFraction,
                                           Schedulers
                                           // Use the common fork-join pool.
                                           .fromExecutor(ForkJoinPool
                                                         .commonPool()),
                                           sb));
    }

    /**
     * This factory method returns a {@link Mono} that's signaled
     * after the {@code unreducedFraction} is reduced/multiplied
     * asynchronously in background threads from the given {@link
     * Scheduler}.
     *
     * @param unreducedFraction An unreduced {@link BigFraction}
     * @param scheduler The {@link Scheduler} to perform the
     *                  computation in
     * @param sb The {@link StringBuffer} to store logging messages
     * @return A {@link Mono<BigFraction>} that's signaled when the
     *         asynchronous computation completes
     */
    private static Mono<BigFraction> reduceAndMultiplyFraction
        (BigFraction unreducedFraction,
         Scheduler scheduler,
         StringBuffer sb) {
        return Mono
            // Emit one item that performs the reduction.
            .fromCallable(() -> BigFraction
                          .reduce(unreducedFraction))

            // Perform all processing asynchronously in the scheduler.
            .subscribeOn(scheduler)

            // Return a Mono to a multiplied BigFraction.
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
