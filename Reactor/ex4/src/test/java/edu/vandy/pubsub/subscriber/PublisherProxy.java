package edu.vandy.pubsub.subscriber;

import edu.vandy.pubsub.common.ClientBeans;
import edu.vandy.pubsub.publisher.PublisherApplication;

import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestTemplate;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static edu.vandy.pubsub.common.Constants.EndPoint.DELETE_STOP;
import static edu.vandy.pubsub.common.Constants.SERVER_BASE_URL;
import static edu.vandy.pubsub.common.Constants.EndPoint.GET_START;

/**
 * This class serves as a proxy to the {@link PublisherApplication}
 * microservice.
 */
@Component
public class PublisherProxy {
    /**
     * Automatically inject a {@link PublisherAPI} instance.
     */
    /*
      @Autowired
      private PublisherAPI mApi;
    */

    @Autowired
    RestTemplate mRestTemplate;

    /**
     * The WebClient provides the means to access the APIGateway
     * microservice.
     */
    final WebClient mApi = WebClient
        // Start building.
        .builder()

        // The URL where the server is running.
        .baseUrl(SERVER_BASE_URL)

        // Build the webclient.
        .build();

    /**
     * Start publishing a stream of random numbers.
     *
     * @param count The number of {@link Integer} objects to create
     * @param maxValue The maximum value of the {@link Integer}
     *                 objects
     * @param backpressureEnabled True if backpressure enabled, else
     *                            false
     * @return A {@link Flux} that publishes random {@link Integer}
     *         objects
     */
    public Flux<Integer> start(int count,
                               int maxValue,
                               boolean backpressureEnabled) {
        var uri = UriComponentsBuilder
            // Add the base path.
            .fromPath(GET_START)

            // Add query parameters.
            .queryParam("count",
                        count)
            .queryParam("maxValue",
                        maxValue)
            .queryParam("backpressureEnabled",
                        backpressureEnabled)

            // Create a UriComponents object.
            .build()

            // Convert to a Uri string.
            .toUriString();

        // System.out.println(uri);

        return mApi
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(uri)

            // Retrieve the response.
            .retrieve()
            
            // Convert to a Flux.
            .bodyToFlux(Integer.class);

        /*
          return mApi
          // Forward to the API.
          .start(count,
          maxValue,
          backpressureEnabled);
        */
    }

    /**
     * Stop publishing a stream of random numbers.
     *
     * @return A {@link Mono} that emits {@link Void} when the call is
     *         done
     */
    public Mono<Void> stop() {
        return mApi
            // Create an HTTP POST request.
            .delete()

            // Add the uri to the baseUrl.
            .uri(DELETE_STOP)

            // Retrieve the response.
            .retrieve()

            .bodyToMono(Void.class);

        /*
          return mApi
          // Forward to the API.
          .stop();
        */
    }
}
