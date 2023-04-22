import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.BigFraction;
import utils.BigFractionUtils;
import utils.BackpressureSubscriber;
import utils.Emitter;

import java.util.Random;

import static utils.BigFractionUtils.*;

/**
 * This class shows how to apply Project Reactor features
 * asynchronously to perform a range of Flux operations, including
 * fromArray(), flatMap(), and subscribe().  It also shows various
 * Mono operations, such as fromSupplier(), repeat(), flatMap(),
 * flatMapMany(), flatMapIterable(), subscribeOn(), and the parallel
 * thread pool.  It also shows how to implement a blocking subscriber
 * that uses various type of backpressure mechanisms.
 */
@SuppressWarnings("ALL")
public class FluxEx {
    /**
     * Create a random number generator.
     */
    private static final Random sRANDOM = new Random();

    /**
     * A test of BigFraction multiplication using an asynchronous Flux
     * stream and a blocking Subscriber implementation that doesn't
     * apply backpressure.
     */
    public static Mono<Void> testFractionMultiplicationsBlockingSubscriber1() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationsBlockingSubscriber1()\n");

        // Create an array of BigFraction objects.
        BigFraction[] bigFractionArray = {
            BigFraction.valueOf(100, 6),
            BigFraction.valueOf(100, 5),
            BigFraction.valueOf(100, 7),
            BigFraction.valueOf(100, 8),
            BigFraction.valueOf(100, 3),
            BigFraction.valueOf(100, 4),
            BigFraction.valueOf(100, 2),
            BigFraction.valueOf(100, 1)
        };

        // Create a blocking subscriber that processes various
        // types of signals have this subscribe not be aware
        // of backpressure and it logs the results.
        BackpressureSubscriber<BigFraction> backpressureSubscriber =
            makeBlockingSubscriber(sb,
                                   Long.MAX_VALUE,
                                   true);

        Mono
            // Generate a random large BigFraction.
            .fromSupplier(() -> BigFractionUtils
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
            .subscribe(backpressureSubscriber);

        // Wait for all async computations to complete and return an
        // empty mono to indicate to the AsyncTaskBarrier that all the
        // processing is done.
        return backpressureSubscriber.await();
    }

    /**
     * A test of BigFraction multiplication using an asynchronous Flux
     * stream and a blocking Subscriber implementation that does apply
     * backpressure.
     */
    public static Mono<Void> testFractionMultiplicationsBlockingSubscriber2() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationsBlockingSubscriber2()\n");

        // Create a blocking subscriber that processes various
        // types of signals and is "backpressure aware."
        BackpressureSubscriber<BigFraction> backpressureSubscriber =
            makeBlockingSubscriber(sb,
                                   2,
                                   true);

        Mono
            // Generate a random large BigFraction.
            .fromSupplier(() -> BigFractionUtils
                          .makeBigFraction(sRANDOM, true))

            // Generate a total of sMAX_FRACTIONS BigFraction objects.
            .repeat(sMAX_FRACTIONS - 1)

            // Use the "flatMap() concurrency idiom" to transform the items
            // emitted by this Flux into a Publisher and then forward its
            // emissions into the returned Flux.
            .flatMap(bf1 -> BigFractionUtils
                     .multiplyFraction(bf1,
                                       sBigReducedFraction,
                                       Schedulers.parallel(),
                                       sb))

            // Use subscribe() to initiate all the processing and
            // handle the results asynchronously.
            .subscribe(backpressureSubscriber);

        // Wait for all async computations to complete and return an
        // empty mono to indicate to the AsyncTaskBarrier that all the
        // processing is done.
        return backpressureSubscriber.await();
    }

    /**
     * A test of BigFraction multiplication using an asynchronous Flux
     * stream and a blocking Subscriber implementation that applies
     * a backpressure strategy.
     */
    public static Mono<Void> testFractionMultiplicationsOverflowStrategy() {
        StringBuffer sb =
            new StringBuffer(">> Calling testFractionMultiplicationsBackpressureStrategy()\n");

        // Create a blocking subscriber that processes various
        // types of signals and is backpressure "unaware".
        BackpressureSubscriber<BigFraction> backpressureSubscriber =
            makeBlockingSubscriber(sb,
                                   Integer.MAX_VALUE,
                                   true);

        // Set the count to something reasonably large.
        var count = sMAX_FRACTIONS * 100;

        Flux
            // Make the publisher generation more BigFraction objects
            // than the consumer can handled without dropping events.
            .create(Emitter.makeEmitter(count,sb),
                    // Generate an exception when overflow occurs.
                    FluxSink.OverflowStrategy.ERROR)

            // Transform the item emitted by this Mono into a
            // Publisher and then forward its emissions into the
            // returned Flux.
            .flatMap(bf1 -> BigFractionUtils
                     .multiplyFraction(bf1,
                                       sBigReducedFraction,
                                       Schedulers.parallel(),
                                       sb))

            // Use subscribe() to initiate all the processing and
            // handle the results asynchronously.
            .subscribe(backpressureSubscriber);

        // Wait for all async computations to complete and return an
        // empty mono to indicate to the AsyncTaskBarrier that all the
        // processing is done.
        return backpressureSubscriber.await();
    }
}
