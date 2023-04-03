package publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import common.Options;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * This Java utility class defines static methods that emit random {@link Integer}s
 * with backpressure enabled.
 */
public final class Emitter {
    /**
     * Debugging tag used by the logger.
     */
    private static final String TAG = Emitter.class.getSimpleName();

    /**
     * A Java utility class should have a private constructor.
     */
    private Emitter() {
    }

    /**
     * A factory method that's used to emit a {@link Flux} stream of
     * random integers using a hybrid push/pull backpressure model.
     *
     * @param iterator         {@link Iterator} containing the random integers
     * @param pendingItemCount Count of the number of pending items
     * @return A {@link Consumer} to a {@link FluxSink} that emits a
     * {@link Flux} stream of random integers using a hybrid
     * push/pull backpressure model
     */
    public static Consumer<FluxSink<Integer>>
        makeBackpressureEmitter(Iterator<Integer> iterator,
                                AtomicInteger pendingItemCount) {
        // Create an emitter that uses the hybrid push/pull
        // backpressure model.
        return sink -> sink
            // Hook method called when request is made to sink.
            .onRequest(size -> {
                Options.debug(TAG,
                    "Request size = "
                        + size);

                // Try to publish size # of items.
                for (int i = 0;
                     i < size;
                     ++i) {
                    // Keep going if the iterator isn't done.
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
}
