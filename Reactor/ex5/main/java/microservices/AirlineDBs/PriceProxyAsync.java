package microservices.AirlineDBs;

import datamodels.TripRequest;
import datamodels.TripResponse;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import java.util.List;

/**
 * This interface provides the basis for asynchronously querying
 * airline flight price databases.
 */
public interface PriceProxyAsync {
    /**
     * Returns a Flux that emits {@code TripResponse} objects that
     * match the {@code trip} param.
     *
     * @param scheduler Thread pool used to perform the computations
     * @param trip The trip to search for price information
     * @return A Flux that emits {@code TripResponse} objects that
     *         match the {@code trip} param
     */
    Flux<TripResponse> findTrips(Scheduler scheduler,
                                 TripRequest trip);

}
