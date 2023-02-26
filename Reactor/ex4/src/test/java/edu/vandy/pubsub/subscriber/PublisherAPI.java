package edu.vandy.pubsub.subscriber;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static edu.vandy.pubsub.common.Constants.EndPoint.*;

/**
 * This interface provides the contract for the RESTful {@code
 * PublisherController} API used in conjunction with the {@code
 * PublisherApplication}.  It defines the HTTP POST, GET, and DELETE
 * methods that can be used to interact with the {@code
 * PublisherController} API, along with the expected request and
 * response parameters for each method.
 *
 * This interface uses Retrofit annotations that provide metadata
 * about the API, such as the type of HTTP request (i.e., {@code GET},
 * {@code POST}, or {@code DELETE}), the parameter types (which are
 * annotated with {@code Query} tags), and the expected response
 * format (which are all wrapped in {@link Mono} and {@link Flux}
 * objects).  Retrofit uses these annotations and method signatures to
 * generate an implementation of the interface that the client uses to
 * make HTTP requests to the API.
 */
public interface PublisherAPI {
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
    @GET(GET_START)
    Flux<Integer> start(@Query("count") int count,
                        @Query("maxValue") int maxValue,
                        @Query("backpressureEnabled") boolean backpressureEnabled);


    /**
     * Stop publishing a stream of random numbers.
     *
     * @return A {@link Mono} that emits {@link Void} when the call is
     *         done
     */
    @DELETE(DELETE_STOP)
    Mono<Void> stop();
}
