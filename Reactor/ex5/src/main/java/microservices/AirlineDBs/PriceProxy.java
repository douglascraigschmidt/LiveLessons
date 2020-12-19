package microservices.AirlineDBs;

import datamodels.Trip;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

/**
 *
 */
public interface PriceProxy {
    /**
     *
     */
    Flux<Trip> findPricesAsync(Scheduler scheduler,
                               Trip trip);
}
