package edu.vandy.pubsub.subscriber;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static edu.vandy.pubsub.common.Constants.EndPoint.*;

/**
 *
 */
public interface PublisherAPI {
    /**
     *
     * @param count
     * @param maxValue
     * @return
     */
    @POST(POST_CREATE)
    Mono<Void> create(@Query("count") int count,
                      @Query("maxValue") int maxValue);

    /**
     *
     * @param backpressureEnabled
     * @return
     */
    @GET(GET_START)
    Flux<Integer> start(@Query("backpressureEnabled") boolean backpressureEnabled);

    /**
     *
     * @return
     */
    @DELETE(DELETE_STOP)
    Mono<Void> stop();
}
