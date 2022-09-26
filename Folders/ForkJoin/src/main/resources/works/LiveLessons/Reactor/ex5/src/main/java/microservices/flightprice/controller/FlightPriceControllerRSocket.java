package microservices.flightprice.controller;

import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.airlines.AA.AAPriceProxyAsync;
import microservices.airlines.PriceProxyAsync;
import microservices.airlines.SWA.SWAPriceProxyAsync;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle RSocket requests via reactive programming.  These requests
 * are mapped to methods that return the cost of flight routes in US
 * dollars asynchronously.
 *
 * In Spring's approach to building RSocket services, message
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @MessageMapping}.  These components are
 * identified by the @Controller annotation below.
 *
 * WebFlux uses the {@code @MessageMapping} annotation to map RSocket
 * requests onto methods in the {@code FlightPriceControllerRSocket}.
 */
@Controller
public class FlightPriceControllerRSocket {
    /**
     * This method finds the best price in US dollars for a given
     * {@code tripRequest} request.
     *
     * WebFlux maps RSocket requests sent to the _findBestPrice
     * endpoint to this method.
     *
     * @param tripRequestM A Mono that emits information about the
     *                     trip, e.g., departure date and
     *                     departure/arrival airports
     * @return A Mono that emits the best price in US dollars for this
     *         {@code trip}
     */
    @MessageMapping("_findBestPrice")
    private Mono<TripResponse> findBestPrice(Mono<TripRequest> tripRequestM) {
        // Initialize the flight proxies if they haven't been
        // initialized by an earlier call.
        initializeProxiesIfNecessary();

        // Forward to the implementation method.
        return findBestPriceImpl(tripRequestM);
    }

    /**
     * This method finds the best price in US dollars for a given
     * {@code tripRequest} request.
     *
     * @param tripRequestM A Mono that emits information about the
     *                     trip, e.g., departure date and
     *                     departure/arrival airports
     * @return A Mono that emits the best price in US dollars for this
     *         {@code trip}
     */
    private Mono<TripResponse> findBestPriceImpl(Mono<TripRequest> tripRequestM) {
        return tripRequestM
            .flatMap(tripRequest -> Flux
                     // Convert the list of proxies into a Flux
                     // stream.
                     .fromIterable(mProxyList)

                     // Apply the flatMap() concurrency idiom to find
                     // all trips that match the trip param in
                     // parallel.
                     .flatMap(proxy -> Flux
                              // Factory that emits the current proxy.
                              .just(proxy)

                              // Run this computation in the parallel
                              // thread pool.
                              .subscribeOn(Schedulers.parallel())

                              // Merge all the trips that match the
                              // trip param into a Flux stream.
                              .flatMap(__ -> proxy
                                       .mProxy
                                       .findTrips(Schedulers.parallel(),
                                                  tripRequest)))

                     // Sort the output so the lowest price comes
                     // first.
                     .sort(Comparator.comparingDouble(TripResponse::getPrice))

                     // Return the lowest priced trip.
                     .next());
    }

    /**
     * This method finds all matching responses a given {@code
     * tripRequest} request.
     *
     * WebFlux maps RSocket requests sent to the _findFlight endpoint
     * to this method.
     *
     * @param tripRequestM A Mono that emits information about the
     *                     trip, e.g., departure date and
     *                     departure/arrival airports
     * @return A Flux that emits all responses for a given {@code
     *         tripRequest} request.
     */
    @MessageMapping("_findFlights")
    private Flux<TripResponse> findFlights(Mono<TripRequest> tripRequestM) {
        // Initialize the flight proxies if they haven't been
        // initialized by an earlier call.
        initializeProxiesIfNecessary();

        // Forward to the implementation method.
        return findFlightsImpl(tripRequestM);
    }

    /**
     * This method finds all matching responses a given {@code
     * tripRequest} request.
     *
     * @param tripRequestM A Mono that emits information about the
     *                     trip, e.g., departure date and
     *                     departure/arrival airports
     * @return A Flux that emits all responses for a given
     *         {@code tripRequest} request.
     */
    private Flux<TripResponse> findFlightsImpl(Mono<TripRequest> tripRequestM) {
        return tripRequestM
            .flatMapMany(tripRequest -> Flux
                         // Convert the list of proxies into a Flux stream.
                         .fromIterable(mProxyList)

                         // Apply the flatMap() concurrency idiom to
                         // find all trips that match the trip param
                         // in parallel.
                         .flatMap(proxy -> Flux
                                  // Factory that emits the current
                                  // proxy.
                                  .just(proxy)

                                  // Run this computation in the
                                  // parallel thread pool.
                                  .subscribeOn(Schedulers.parallel())

                                  // Merge all the trips that match
                                  // the trip param into a Flux
                                  // stream.
                                  .flatMap(__ -> proxy
                                           .mProxy
                                           .findTrips(Schedulers.parallel(),
                                                      tripRequest))));
    }

    /**
     * An async proxy to the SWA price database.
     */
    private SWAPriceProxyAsync mSWAPriceProxyAsync;

    /**
     * An async proxy to the AA price database.
     */
    private AAPriceProxyAsync mAAPriceProxyAsync;

    /**
     * A helper class that holds a PriceProxy and a
     * factory for creating a PriceProxy.
     */
    static class Tuple {
        /**
         * A PricyProxy that references an airline price
         * microservices.
         */
        PriceProxyAsync mProxy;

        /**
         * A factory that creates a new PriceProxy.
         */
        Supplier<PriceProxyAsync> mFactory;

        /**
         * The constructor initializes the fields.
         */
        Tuple(Supplier<PriceProxyAsync> proxySupplier) {
            mProxy = null;
            mFactory = proxySupplier;
        }
    }

    /**
     * A list of PriceProxy objects to airline price microservices.
     */
    List<Tuple> mProxyList =
        new ArrayList<Tuple>() { {
            add(new Tuple(SWAPriceProxyAsync::new));
            add(new Tuple(AAPriceProxyAsync::new));
    } };

    /**
     * Initialize the flight proxies if they haven't already been
     * initialized in an earlier call.
     */
    void initializeProxiesIfNecessary() {
        // Iterate through all the airline proxies.
        for (Tuple tuple : mProxyList)
            // If a proxy hasn't been initialized yet then initialize
            // it so it will be cached for future calls.
            if (tuple.mProxy == null)
                tuple.mProxy = tuple.mFactory.get();
    }
}
