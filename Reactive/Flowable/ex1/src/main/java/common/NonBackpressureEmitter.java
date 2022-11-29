package common;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Java utility class defines a static method that emits random
 * {@link Integer} objects.
 */
public final class NonBackpressureEmitter {
    /**
     * Debugging tag used by the logger.
     */
    private static final String TAG =
        NonBackpressureEmitter.class.getSimpleName();

    /**
     * A Java utility class should have a private constructor.
     */
    private NonBackpressureEmitter() {}

    /**
     * A factory method that's used to emit a {@link Flowable} stream
     * of random integers without concern for backpressure.
     *
     * @param count The number of random Integers to generate
     * @param maxValue The maximum value of the random {@link Integer}s
     * @param pendingItemCount Count of the number of pending items
     * @return A {@link FlowableOnSubscribe} that emits a
     *         {@link Flowable} stream of random integers
     */
    public static FlowableOnSubscribe<Integer>
    makeEmitter(int count,
                int maxValue,
                AtomicInteger pendingItemsCount) {
        Random random = new Random();

        // Emit random integers without concern for backpressure.
        return emitter -> Flowable
            // Generate a stream of Integer objects from 1 to count, a
            // la a reactive for loop!
            .range(1, count)

            // Generate the results.
            .subscribe(iteration -> {
                    // Increment the current pending item count.
                    pendingItemsCount.incrementAndGet();

                    var item = random.nextInt(maxValue);

                    if (Options.instance().printIteration(iteration))
                        Options.debug("["
                                      + iteration
                                      + "] published "
                                      + item
                                      + " with "
                                      + pendingItemsCount.get()
                                      + " pending");

                    // Only publish an item if the emitter hasn't been
                    // cancelled.
                    if (!emitter.isCancelled())
                        // Publish the next item.
                        emitter.onNext(item);
                },
                emitter::onError,
                emitter::onComplete)

            // Dispose of the Flowable when the stream is finished.
            .dispose();
    }
}
