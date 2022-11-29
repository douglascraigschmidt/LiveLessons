package common;

import io.reactivex.rxjava3.core.Emitter;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Java utility class defines a static method that emits random {@link Integer} \
 * objects.
 */
public final class BackpressureEmitter {
    /**
     * Debugging tag used by the logger.
     */
    private static final String TAG = BackpressureEmitter.class.getSimpleName();

    /**
     * A Java utility class should have a private constructor.
     */
    private BackpressureEmitter() {}

    /**
     * Keeps track of the number of {@link Integer} objects
     * emitted thus far.
     */
    private static int sIntegersEmitted = 0;

    /**
     * A factory method that's used to emit a {@link Flowable} stream of
     * random integers.
     *
     * @param count The number of random Integers to generate
     * @param maxValue The maximum value of the random {@link Integer} objects
     * @param pendingItemCount Count of the number of pending items
     * @return A {@link FlowableOnSubscribe} that emits a
     *         {@link Flowable} stream of random integers
     */
    public static Consumer<Emitter<Integer>>
    makeEmitter(int count,
                int maxValue,
                AtomicInteger pendingItemCount) {
        // Create a new random number generator.
        Random random = new Random();

        // Emit random integers.
        return (Emitter<Integer> emitter) -> {
            if (sIntegersEmitted++ < count) {
                // Get the next random number.
                var item = random.nextInt(maxValue);

                // Increment the count atomically.
                pendingItemCount.incrementAndGet();

                if (Options.instance().printIteration(sIntegersEmitted))
                    Options.debug("["
                            + sIntegersEmitted
                            + "] published "
                            + item
                            + " with "
                            + pendingItemCount.get()
                            + " pending");

                // Publish the next item.
                emitter.onNext(item);
            } else
                emitter.onComplete();
        };
    }
}
