package edu.vandy.pubsub.subscriber;

import edu.vandy.pubsub.common.ClientBeans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class serves as a proxy to the Publisher microservice.
 */
@Component
public class PublisherProxy {
    /**
     * Automatically inject a {@link PublisherAPI} instance.
     */
    @Autowired
    private PublisherAPI mApi;

    /**
     * Initialize the publisher to emit a stream of random {@link
     * Integer} objects.
     *
     * @param count The number of {@link Integer} objects to create
     * @param maxValue The maximum value of the {@link Integer}
     *                 objects
     * @return A {@link Mono} that emits {@link Void} when the call is
     *         done
     */
    public Mono<Void> create(int count,
                             int maxValue) {
        return mApi
            // Forward to the API.
            .create(count, maxValue);
    }

    /**
     * Start publishing a stream of random numbers.
     *
     * @param backpressureEnabled True if backpressure enabled, else
     *                            false
     * @return A {@link Flux} that publishes random {@link Integer}
     *         objects
     */
    public Flux<Integer> start(boolean backpressureEnabled) {
        return mApi
            // Forward to the API.
            .start(backpressureEnabled);
    }

    /**
     * Stop publishing a stream of random numbers.
     *
     * @return A {@link Mono} that emits {@link Void} when the call is
     *         done
     */
    public Mono<Void> stop() {
        return mApi
            // Forward to the API.
            .stop();
    }
}
