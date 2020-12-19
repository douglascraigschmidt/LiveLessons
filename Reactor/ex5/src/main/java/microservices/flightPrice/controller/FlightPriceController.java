package microservices.flightPrice.controller;

import datamodels.Trip;
import microservices.AirlineDBs.AA.AAPriceProxy;
import microservices.AirlineDBs.PriceProxy;
import org.springframework.web.bind.annotation.*;
import microservices.AirlineDBs.SWA.SWAPriceProxy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static utils.ReactorUtils.randomDelay;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET, POST, and DELETE requests via asynchronous
 * reactive programming.  These requests are mapped to methods that
 * return the cost of flight routes in US dollars synchronously and
 * asynchronously.
 *
 * In Spring's approach to building RESTful web services, HTTP
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @GetMapping}, {@code @PostMapping}, {@code @PutMapping} and
 * {@code @DeleteMapping}, which correspond to the HTTP GET, POST,
 * PUT, and DELETE calls, respectively.  These components are
 * identified by the @RestController annotation below.
 *
 * WebFlux uses the {@code @GetMapping} annotation to map HTTP GET
 * requests onto methods in the {@code FlightPriceController}.  GET
 * requests invoked from any HTTP web client (e.g., a web browser) or
 * command-line utility (e.g., Curl or Postman).
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

    class Tuple {
        PriceProxy mProxy;
        Supplier<PriceProxy> mFactory;

        Tuple(Supplier<PriceProxy> proxySupplier) {
            mProxy = null;
            mFactory = proxySupplier;
        }
    }

    /**
     *
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
     * @param trip Information about the trip.
     * @return A Mono that emits best price in US dollars for this {@code trip}.
     */
    @PostMapping("/_bestPriceAsync")
    private Mono<Trip> findBestPrice(@RequestBody Trip trip) {
        initializeProxiesIfNecessary();

        /*
        // Debugging print.
        print("Flight leg is "
              + flightLeg);
        */

         return Flux
                .fromIterable(mProxyList)

                .flatMap(proxy -> Flux
                        .just(proxy)
                        .subscribeOn(Schedulers.parallel())
                        .flatMap(__ -> proxy.mProxy
                                .findPricesAsync(Schedulers.parallel(),
                                        trip)))
            .sort(Comparator.comparingDouble(Trip::getPrice))
            .next();
    }

    /**
     *
     */
    private void initializeProxiesIfNecessary() {
        for (Tuple tuple : mProxyList)
            if (tuple.mProxy == null)
                tuple.mProxy = tuple.mFactory.get();
    }
}
