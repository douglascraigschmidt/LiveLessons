package publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import utils.Options;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

/**
 * This publisher generates a flux stream from a micro-service that's
 * connected to the subscriber via WebFlux mechanisms.
 */
public class Publisher {
    /**
     * Debugging tag used by the logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * A list of randomly-generated integers.
     */
    private final List<Integer> mRandomIntegers;

    /**
     * The thread pool used to run the publisher.
     */
    private Scheduler mPublisherScheduler;

    /**
     * Constructor initializes the list of random integers.
     *
     * @param count The number of random integers to generate
     * @param maxValue The max value of the random integers
     */
    public Publisher(int count, int maxValue) {
        // Generate a list of random integers.
        mRandomIntegers = new Random()
            // Generate "count" random ints.
            .ints(count,
                  // Try to generate duplicates.
                  maxValue - count, 
                  maxValue)

            // Convert each primitive int to Integer.
            .boxed()    
                   
            // Trigger intermediate operations and collect into list.
            .collect(toList());
    }

    /**
     * Publish a stream of random numbers.
     *
     * @return Return a flux that publishes random numbers
     */
    public Flux<Integer> publish(Boolean backpressureEnabled) {
        // Run the publisher in a single thread.
        mPublisherScheduler = Schedulers
            .newParallel("publisher", 1);

        // This consumer emits a flux stream of random integers.
        return Flux
            // Emit a flux stream of random integers.
            .create(backpressureEnabled
                    // Emit integers using backpressure.
                    ? makeBackpressureEmitter(mRandomIntegers.iterator())
                    // Emit integers not using backpressure.
                    : makeNonBackpressureEmitter(mRandomIntegers.iterator()),
                    // Set the overflow strategy.
                    Options.instance().overflowStrategy())

            // Subscribe on the given scheduler.
            .subscribeOn(mPublisherScheduler);
    }

    /**
     * A factory method that's used to emit a flux stream of random
     * integers using a hybrid push/pull backpressure model.
     *
     * @param iterator Iterator containing the random integers
     * @return A consumer to a flux sink that emits a flux stream of
     *         random integers using a hybrid push/pull backpressure
     *         model
     */
    private Consumer<FluxSink<Integer>>
        makeBackpressureEmitter(Iterator<Integer> iterator) {
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


                            Options.debug(TAG,
                                          "published item: "
                                          + item);

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
     * integers without concern for backpressure.
     *
     * @param iterator Iterator containing the random integers
     * @return A consumer to a flux sink that emits a flux stream
     *         of random integers without concern for backpressure
     */
    private Consumer<FluxSink<Integer>>
        makeNonBackpressureEmitter(Iterator<Integer> iterator) {
        // Create an emitter that just blasts out random integers.
        return sink -> {
            Options.debug(TAG, "Request size = "
                          + mRandomIntegers.size());

            // Keep going if iterator's not done.
            while (iterator.hasNext()) {
                // Get the next item.
                Integer item = iterator.next();

                Options.debug(TAG,
                              "published item: "
                              + item);

                // Only publish an item if the sink hasn't been cancelled.
                if (!sink.isCancelled())
                    // Publish the next item.
                    sink.next(item);
            }

            // We're done publishing.
            sink.complete();
        };
    }

    /**
     * Stop generating the stream of random integers.
     *
     * @return An empty mono.
     */
    public Mono<Void> dispose() {
        // Shutdown the publisher's scheduler.
        mPublisherScheduler.dispose();

        // Return an empty mono.
        return Mono.empty();
    }
}

