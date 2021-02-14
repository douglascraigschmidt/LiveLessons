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

import static java.util.stream.Collectors.toList;

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
     * This method finds the best price in US dollars for a given
     * {@code tripRequest} request.
     *
     * WebFlux maps HTTP POST requests sent to the /_bestPriceAsync
     * endpoint to this method.
     *
     * @param tripRequest Information about the trip, e.g., departure date
     *             and departure/arrival airports
     * @return A Mono that emits the best price in US dollars for this
     *         {@code trip}
     */
    @PostMapping("/_bestPriceAsync")
    private Mono<TripResponse> findBestPriceAsync(@RequestBody TripRequest tripRequest) {
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
                                              tripRequest)))

            // Sort the output so the lowest price comes first.
            .sort(Comparator.comparingDouble(TripResponse::getPrice))

            // Return the lowest priced trip.
            .next();
    }

    /**
     * This method finds the best price in US dollars for a given
     * {@code tripRequest} request.
     *
     * WebFlux maps HTTP POST requests sent to the /_bestPriceSync
     * endpoint to this method.
     *
     * @param tripRequest Information about the trip, e.g., departure date
     *             and departure/arrival airports
     * @return A TripResponse that contains the best price in US dollars for this
     *         {@code trip}
     */
    @PostMapping("/_bestPriceSync")
    private TripResponse findBestPriceSync(@RequestBody TripRequest tripRequest) {
        System.out.println("findBestPriceSync");

        // Initialize the flight proxies if they haven't been
        // initialized by an earlier call.
        initializeProxiesIfNecessary();


        // Convert the list of proxies into a parallel stream.
        // Sort the output so the lowest price comes first.
        // Return the lowest priced trip.
        return mProxyList
                // Convert the list of proxies into a parallel stream.
                .parallelStream()

                .map(tuple -> tuple.mProxy
                        .findTripsSync(tripRequest))

                .flatMap(List::stream)
                .min(Comparator.comparingDouble(TripResponse::getPrice))
                .orElse(null);
    }

    /**
     * This method finds all matching responses a given {@code
     * tripRequest} request.
     *
     * WebFlux maps HTTP POST requests sent to the /_findFlightsAsync
     * endpoint to this method.
     *
     * @param tripRequest Information about the trip, e.g., departure date
     *                    and departure/arrival airports
     * @return A Flux that emits all responses for a given
     *         {@code tripRequest} request.
     */
    @PostMapping("/_findFlightsAsync")
    private Flux<TripResponse> findFlightsAsync(@RequestBody TripRequest tripRequest) {
        // Initialize the flight proxies if they haven't been
        // initialized by an earlier call.
        initializeProxiesIfNecessary();

        return findFlightsAsyncImpl(tripRequest);
    }

    /**
     * This method finds all matching responses a given {@code
     * tripRequest} request.
     *
     * WebFlux maps HTTP POST requests sent to the /_findFlightsSync
     * endpoint to this method.
     *
     * @param tripRequest Information about the trip, e.g., departure date
     *                    and departure/arrival airports
     * @return A List that contains all responses for a given
     *         {@code tripRequest} request.
     */
    @PostMapping("/_findFlightsSync")
    private List<TripResponse> findFlightsSync(@RequestBody TripRequest tripRequest) {
        // Initialize the flight proxies if they haven't been
        // initialized by an earlier call.
        initializeProxiesIfNecessary();

        return findFlightsSyncImpl(tripRequest);
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

    /**
     * This method finds all matching responses a given {@code
     * tripRequest} request.
     *
     * @param tripRequest Information about the trip, e.g., departure date
     *                    and departure/arrival airports
     * @return A Flux that emits all responses for a given
     *         {@code tripRequest} request.
     */
    private Flux<TripResponse> findFlightsAsyncImpl(TripRequest tripRequest) {
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
                                              tripRequest)));
    }

    /**
     * This method finds all matching responses a given {@code
     * tripRequest} request.
     *
     * @param tripRequest Information about the trip, e.g., departure date
     *                    and departure/arrival airports
     * @return A List that contains all responses for a given
     *         {@code tripRequest} request.
     */
    private List<TripResponse> findFlightsSyncImpl(TripRequest tripRequest) {
        return mProxyList
                // Convert the list of proxies into a parallel stream.
                .parallelStream()

                .map(tuple -> tuple.mProxy
                                .findTripsSync(tripRequest))

                .flatMap(List::stream)

                .collect(toList());
    }

}
