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
 * handle HTTP POST requests via object-oriented programming.  These
 * requests are mapped to methods that return the cost of flight
 * routes in US dollars synchronously.
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
 * requests onto methods in the {@code FlightPriceControllerSync}.
 * POST requests invoked from any HTTP web client (e.g., a web browser
 * or Java app) or command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/flightPriceSync")
public class FlightPriceControllerSync {
    /**
     * Default constructor.
     */
    public FlightPriceControllerSync() {
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
    @PostMapping("/_findBestPrice")
    private TripResponse findBestPrice(@RequestBody TripRequest tripRequest) {
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
     * WebFlux maps HTTP POST requests sent to the /_findFlights
     * endpoint to this method.
     *
     * @param tripRequest Information about the trip, e.g., departure date
     *                    and departure/arrival airports
     * @return A List that contains all responses for a given
     *         {@code tripRequest} request.
     */
    @PostMapping("/_findFlights")
    private List<TripResponse> findFlights(@RequestBody TripRequest tripRequest) {
        // Initialize the flight proxies if they haven't been
        // initialized by an earlier call.
        initializeProxiesIfNecessary();

        return findFlightsImpl(tripRequest);
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
    private List<TripResponse> findFlightsImpl(TripRequest tripRequest) {
        return mProxyList
                // Convert the list of proxies into a parallel stream.
                .parallelStream()

                .map(tuple -> tuple.mProxy
                                .findTripsSync(tripRequest))

                .flatMap(List::stream)

                .collect(toList());
    }

}
