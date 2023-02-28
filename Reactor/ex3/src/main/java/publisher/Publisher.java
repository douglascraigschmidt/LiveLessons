package publisher;

import common.Options;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        var emitter = Options.instance()
            .backPressureEnabled()
            // Emit integers using backpressure.
            ? Emitters.makeBackpressureEmitter(randomIntegers.iterator(),
                                               sPendingItemCount)
            // Emit integers not using backpressure.
            : Emitters.makeNonBackpressureEmitter(randomIntegers.iterator(),
                                                  sPendingItemCount,
                                                  randomIntegers.size());

        // This consumer emits a flux stream of random integers.
        return Flux
            // Emit a flux stream of random integers.
            .create(emitter,
                    // Set the overflow strategy.
                    Options.instance().overflowStrategy())

            // Subscribe on the given scheduler.
            .subscribeOn(scheduler);
    }
}
