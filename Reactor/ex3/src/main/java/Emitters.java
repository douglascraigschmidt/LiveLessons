import reactor.core.publisher.FluxSink;
import utils.Options;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This Java utility class defines static methods that emit random {@link Integer}s
 * with either backpressure enabled or disabled.
 */
public final class Emitters {
    /**
     * Debugging tag used by the logger.
     */
    private static final String TAG = Emitters.class.getSimpleName();

    /**
     * A factory method that's used to emit a flux stream of random
     * integers using a hybrid push/pull backpressure model.
     *
     * @param iterator Iterator containing the random integers
     * @param pendingItemCount Count of the number of pending items
     * @return A consumer to a flux sink that emits a flux stream of
     *         random integers using a hybrid push/pull backpressure
     *         model
     */
    static Consumer<FluxSink<Integer>>
        makeBackpressureEmitter(Iterator<Integer> iterator,
                                AtomicInteger pendingItemCount) {
        // Create an emitter that uses the hybrid push/pull
        // backpressure model.
        return sink -> sink
            // Hook method called when request is made to sink.
            .onRequest(size -> {
                    Options.debug(TAG, "Request size = " + size);

                    // Try to publish size # of items.
                    for (int i = 0;
                         i < size;
                         ++i) {
                        // Keep going if iterator's not done.
                        if (iterator.hasNext()) {
                            // Get the next item.
                            Integer item = iterator.next();

                            // Store current pending item count.
                            int pendingItems =
                                pendingItemCount.incrementAndGet();

                            Options.debug(TAG,
                                          "published item: "
                                          + item
                                          + ", pending items = "
                                          + pendingItems);

                            // Publish the next item.
                            sink.next(item);
                        } else {
                            // We're done publishing.
                            sink.complete();
                            break;
                        }
                    }
                });
    }

    /**
     * A factory method that's used to emit a flux stream of random
     * integers without concern for backpressure, i.e., as fast as possible.
     *
     * @param iterator Iterator containing the random integers
     * @param pendingItemCount Count of the number of pending items
     * @param size Number of items to emit.
     * @return A consumer to a flux sink that emits a flux stream
     *         of random integers without concern for backpressure
     */
    static Consumer<FluxSink<Integer>>
        makeNonBackpressureEmitter(Iterator<Integer> iterator,
                                   AtomicInteger pendingItemCount,
                                   int size) {
        // Create an emitter that just blasts out random integers.
        return sink -> {
            Options.debug(TAG, "Request size = " + size);

            // Keep going if iterator is not done.
            while (iterator.hasNext()) {
                // Get the next item.
                Integer item = iterator.next();

                // Store current pending item count.
                int pendingItems =
                    pendingItemCount.incrementAndGet();

                Options.debug(TAG,
                              "published item: "
                              + item
                              + ", pending items = "
                              + pendingItems);

                // Only publish an item if the sink hasn't been cancelled.
                if (!sink.isCancelled())
                    // Publish the next item.
                    sink.next(item);
            }

            // We're done publishing.
            sink.complete();
        };
    }
}
