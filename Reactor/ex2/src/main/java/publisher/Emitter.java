package publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import common.Options;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This Java utility class defines static methods that emit random {@link Integer}s
 * with either backpressure enabled or disabled.
 */
public final class Emitter {
    /**
     * Debugging tag used by the logger.
     */
    private static final String TAG = Emitter
        .class.getSimpleName();

    /**
     * A Java utility class should have a private constructor.
     */
    private Emitter() {}

    /**
     * A factory method that's used to emit a {@link Flux} stream of
     * random integers without concern for backpressure, i.e., as fast
     * as possible.
     *
     * @param iterator {@link Iterator} containing the random integers
     * @param pendingItemCount Count of the number of pending items
     * @param size Number of items to emit
     * @return A {@link Consumer} to a {@link FluxSink} that emits a
     *         {@link Flux} stream of random integers without concern
     *         for backpressure
     */
    public static Consumer<FluxSink<Integer>>
    makeEmitter(Iterator<Integer> iterator,
                AtomicInteger pendingItemCount,
                int size) {
        // Create an emitter that just blasts out random integers.
        return sink -> {
            Options.debug(TAG, "Request size = " + size);

            // Keep going while the iterator is not done.
            while (iterator.hasNext()) {
                // Get the next random integer.
                Integer item = iterator.next();

                // Store current pending item count.
                int pendingItems =
                    pendingItemCount.incrementAndGet();

                Options.debug(TAG,
                              "published item: "
                              + item
                              + ", pending items = "
                              + pendingItems);

                // Only publish an item if the sink hasn't been
                // cancelled.
                if (!sink.isCancelled())
                    // Publish the next item.
                    sink.next(item);
            }

            // We're done publishing.
            sink.complete();
        };
    }
}
