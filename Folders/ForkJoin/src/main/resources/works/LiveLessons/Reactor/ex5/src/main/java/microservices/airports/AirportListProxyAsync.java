package microservices.airports;

import datamodels.AirportInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.function.Function;

/**
 * This class serves as a proxy to the asynchronous AirportList
 * microservice that provides a list of airport codes and associated
 * airport names.
 */
public class AirportListProxyAsync 
       extends AirportListProxyBase {
    /**
     * The URI that denotes the remote method to obtain the list of
     * airport codes/names asynchronously.
     */
    private final String mFindAirportListsURIAsync =
            "/microservices/AirportListAsync/_getAirportList";

    /**
     * Constructor initializes the super class.
     */
    public AirportListProxyAsync() {
        super();
    }

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
            .fromCallable(() -> mAirportLists
                          // Create an HTTP POST request.
                          .get()

                          // Add the uri to the baseUrl.
                          .uri(mFindAirportListsURIAsync)

                          // Retrieve the response.
                          .retrieve()

                          // Convert it to a Flux of AirportInfo
                          // objects.
                          .bodyToFlux(AirportInfo.class))

            // Schedule this to run on the given scheduler.
            .subscribeOn(scheduler)

            // De-nest the result so it's a Flux<AirportInfo>.
            .flatMapMany(Function.identity());
    }
}
