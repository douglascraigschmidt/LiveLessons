package utils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static utils.BigFractionUtils.makeBigFraction;

/**
 * This Java utility class defines static methods that emit random
 * {@link Integer} objects with backpressure disabled.
 */
public final class Emitter {
    /**
     * Debugging tag used by the logger.
     */
    private static final String TAG = Emitter
        .class.getSimpleName();

    /**
     * Create a {@link Random} number generator.
     */
    private static final Random mRandom = new Random();

    /**
     * A Java utility class should have a private constructor.
     */
    private Emitter() {
    }

    /**
     * A factory method that's used to emit a {@link Flux} stream of
     * random integers without concern for backpressure, i.e., as fast
     * as possible.
     *
     * @param count Number of items to emit
     * @return A {@link Consumer} to a {@link FluxSink} that emits a
     *         {@link Flux} stream of random integers without concern
     *         for backpressure
     */
    public static Consumer<FluxSink<BigFraction>>
    makeEmitter(int count,
                StringBuffer sb) {
        // Create an emitter that just blasts out random integers.
        return sink -> {
            // Keep going while the iterator is not done.
            for (int i = 0; i < count; ++i) {
                // Get the next random integer.
                var item = makeBigFraction(mRandom, false);

                // Periodically print out a diagnostic.
                if (i % 20 == 0)
                    sb.append("Publishing items "
                              + i
                              + ".."
                              + (i + 20)
                              + "\n");

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
