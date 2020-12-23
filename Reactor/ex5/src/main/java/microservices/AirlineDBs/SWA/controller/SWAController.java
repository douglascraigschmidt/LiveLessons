package microservices.AirlineDBs.SWA.controller;

import datamodels.TripRequest;
import datamodels.TripResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import utils.TestDataFactory;

import java.util.List;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET, POST, and DELETE requests via asynchronous
 * reactive programming.  These requests are mapped to methods that
 * return the flights available on Southwest airlines for certain dates
 * and certain cities.
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
@RequestMapping("/microservices/AirlineDBs/SWA")
public class SWAController {
    /**
     * The list of Trips and their associated prices.
     */
    private final List<TripResponse> mTrips;

    /**
     * Constructor initializes the field.
     */
    SWAController() {
        mTrips = TestDataFactory
            .getTripList("SWA.txt");
    }

    /**
     * This method finds all the trips on given departure
     * date and the flight leg for Southwest Airlines.
     *
     * WebFlux maps HTTP POST requests sent to the
     * /_bestPrice endpoint to this method.
     *
     * @param trip Information about the trip, i.e., date and flight leg.
     * @return A Flux that emits all the trips for the given departure date and flight leg.
     */
    @PostMapping("/_getTripPrices")
    private Flux<TripResponse> getTripPrices(@RequestBody TripRequest trip) {
        return Flux
            // Convert the list of trips into a Flux stream.
            .fromIterable(mTrips)

            // Select only those trips that equal the trip param.
            .filter(trip::equals);
    }
}
