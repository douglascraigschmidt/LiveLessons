package microservices.flightPrice.controller;

import datamodels.TripRequest;
import datamodels.TripResponse;
import microservices.AirlineDBs.AA.AAPriceProxy;
import microservices.AirlineDBs.PriceProxy;
import org.springframework.web.bind.annotation.*;
import microservices.AirlineDBs.SWA.SWAPriceProxy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.function.Supplier;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP POST requests via asynchronous reactive programming.
 * These requests are mapped to methods that return the cost of flight
 * routes in US dollars synchronously and asynchronously.
 *
 * In Spring's approach to building RESTful web services, HTTP
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @GetMapping}, {@code @PostMapping}, {@code @PutMapping} and
 * {@code @DeleteMapping}, which correspond to the HTTP GET, POST,
 * PUT, and DELETE calls, respectively.  These components are
 * identified by the @RestController annotation below.
 *
 * WebFlux uses the {@code @PostMapping} annotation to map HTTP POST
 * requests onto methods in the {@code FlightPriceController}.  POST
 * requests invoked from any HTTP web client (e.g., a web browser or
 * Java app) or command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/flightPrice")
public class FlightPriceController {
    /**
     * A proxy to the SWA price database.
     */
    private SWAPriceProxy mSWAPriceProxy;

    /**
     * A proxy to the AA price database.
     */
    private AAPriceProxy mAAPriceProxy;

    /**
     * A helper class that holds a PriceProxy and a
     * factory for creating a PriceProxy.
     */
    private static class Tuple {
        /**
         * A PricyProxy that references an airline price
         * microservices.
         */
        PriceProxy mProxy;

        /**
         * A factory that creates a new PriceProxy.
         */
        Supplier<PriceProxy> mFactory;

        /**
         * The constructor initializes the fields.
         */
        Tuple(Supplier<PriceProxy> proxySupplier) {
            mProxy = null;
            mFactory = proxySupplier;
        }
    }

    /**
     * A list of PriceProxy objects to airline price microservices.
     */
    List<Tuple> mProxyList =
        new ArrayList<Tuple>() { {
            add(new Tuple(SWAPriceProxy::new));
            add(new Tuple(AAPriceProxy::new));
    } };

    /**
     * Default constructor.
     */
    public FlightPriceController() {
    }

    /**
     * This method simulates a microservice that finds the best price
     * in US dollars for a given {@code trip}.
     *
     * WebFlux maps HTTP POST requests sent to the /_bestPriceAsync
     * endpoint to this method.
     *
     * @param trip Information about the trip, e.g., departure date
     *             and departure/arrival airports
     * @return A Mono that emits best price in US dollars for this
     *         {@code trip}
     */
    @PostMapping("/_bestPriceAsync")
    private Mono<TripResponse> findBestPrice(@RequestBody TripRequest trip) {
        // Initialize the flight proxies if they haven't been
        // initialized by an earlier call.
        initializeProxiesIfNecessary();

        /*
        // Debugging print.
        print("Flight leg is "
        + flightLeg);
        */

        return Flux
            // Convert the list of proxies into a Flux stream.
            .fromIterable(mProxyList)

            // Apply the flatMap() concurrency idiom to find all trips
            // that match the trip param in parallel.
            .flatMap(proxy -> Flux
                     // Factory that emits the current proxy.
                     .just(proxy)

                     // Run this computation in the parallel thread
                     // pool.
                     .subscribeOn(Schedulers.parallel())

                     // Merge all the trips that match the trip param
                     // into a Flux stream.
                     .flatMap(__ -> proxy
                              .mProxy
                              .findTripsAsync(Schedulers.parallel(),
                                              trip)))

            // Sort the output so the lowest price comes first.
            .sort(Comparator.comparingDouble(TripResponse::getPrice))

            // Return the lowest priced trip.
            .next();
    }

    /**
     * Initialize the flight proxies if they haven't already been
     * initialized in an earlier call.
     */
    private void initializeProxiesIfNecessary() {
        // Iterate through all the airline proxies.
        for (Tuple tuple : mProxyList)
            // If a proxy hasn't been initialized yet then initialize
            // it so it will be cached for future calls.
            if (tuple.mProxy == null)
                tuple.mProxy = tuple.mFactory.get();
    }
}
