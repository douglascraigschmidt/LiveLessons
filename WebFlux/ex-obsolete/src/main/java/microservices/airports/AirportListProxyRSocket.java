package microservices.airports;

import datamodels.AirportInfo;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

/**
 * This class serves as a proxy to the asynchronous AirportList
 * microservice that uses the RSocket framework to provide a list of
 * airport codes and associated airport names.
 */
public class AirportListProxyRSocket {
    /**
     * The message name that denotes the remote method to obtain the
     * list of airport codes/names asynchronously.
     */
    private final String mFindAirportListMessage =
        "_getAirportList";

    /**
     * Initialize the RSocketRequestor.
     */
    private final Mono<RSocketRequester> rSocketRequester = Mono
        .just(RSocketRequester.builder()
              .rsocketConnector(rSocketConnector -> rSocketConnector
                                .reconnect(Retry.fixedDelay(2,
                                                            Duration.ofSeconds(2))))
              .dataMimeType(MediaType.APPLICATION_CBOR)
              .rsocketStrategies(RSocketStrategies.builder()
                                 .encoders(encoders ->
                                           encoders.add(new Jackson2CborEncoder()))
                                 .decoders(decoders ->
                                           decoders.add(new Jackson2CborDecoder()))
                                 .build())
              .tcp("localhost", 8090));

    /**
     * Returns a Flux that emits {@code AirportInfo} objects.
     *
     * @param scheduler The Scheduler context in which to run the operation
     * @return A Flux that emits {@code AirportInfo} objects
     */
    public Flux<AirportInfo> findAirportInfo(Scheduler scheduler) {
        return Mono
            // Return a Flux containing the list of airport
            // information.
            .fromCallable(() -> rSocketRequester
                          // Create the data to send to the server.
                          .map(r -> r
                               .route(mFindAirportListMessage))

                          // Get the result back from the server as a
                          // Flux<AirportInfo>.
                          .flatMapMany(r -> r.retrieveFlux(AirportInfo.class)))

            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Flux<AirportInfo>.
            .flatMapMany(Function.identity());
    }
}
