package publisher;

import common.Options;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This publisher generates a {@link Flux} stream of
 * random {@link Integer} objects.
 */
public class Publisher {
    /**
     * Count the # of pending items between publisher and subscriber.
     */
    public static final AtomicInteger sPendingItemCount =
        new AtomicInteger(0);

    /**
     * Publish a stream of random numbers.
     *
     * @param scheduler {@link Scheduler} to publish the random
     *                  numbers on
     * @return Return a {@link Flux} that publishes random numbers
     */
    public static Flux<Integer> publishIntegers
        (Scheduler scheduler,
         List<Integer> randomIntegers) {
        // Create the designated emitter.
        var emitter =
            getEmitter(randomIntegers);

        // This consumer emits a flux stream of random Integer objects.
        return Flux
            // Emit a flux stream of random integers.
            .create(emitter)

            // Subscribe on the given scheduler.
            .subscribeOn(scheduler);
    }

    /**
     * Return a {@link Consumer} that emits {@link Integer} objects.
     *
     * @param randomIntegers The {@link List} of random {@link Integer} objects
     * @return A {@link Consumer} that emits {@link Integer} objects
     */
    private static Consumer<FluxSink<Integer>> getEmitter
        (List<Integer> randomIntegers) {
        return // Emit integers not using backpressure.
               Emitter.makeEmitter(randomIntegers.iterator(),
                                   sPendingItemCount,
                                   randomIntegers.size());
    }
}
