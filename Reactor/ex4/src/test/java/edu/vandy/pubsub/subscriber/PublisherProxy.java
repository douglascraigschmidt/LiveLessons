package edu.vandy.pubsub.subscriber;

import edu.vandy.pubsub.common.ClientBeans;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class serves as a proxy to the Publisher microservice.
 */
@Component
public class PublisherProxy {
    // @Autowired
    private PublisherAPI mApi = ClientBeans.getPublisherAPI();

    /**
     * Initialize the publisher to emit a stream of random {@link Integer} objects.
     *
     * @param count
     * @param maxValue
     */
    public Mono<Void> create(int count,
                             int maxValue) {
        return mApi.create(count, maxValue);
    }

    /**
     * Start publishing a stream of random numbers.
     *
     * @return A {@link Flux} that publishes random {@link Integer} objects
     */
    public Flux<Integer> start(boolean backpressureEnabled) {
        return mApi.start(backpressureEnabled);
    }

    /**
     * Stop publishing a stream of random numbers.
     */
    public Mono<Void> stop() {
        return mApi.stop();
    }
}
