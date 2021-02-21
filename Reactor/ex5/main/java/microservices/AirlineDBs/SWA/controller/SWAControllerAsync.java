package microservices.airlines.SWA.controller;

import datamodels.TripRequest;
import datamodels.TripResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import utils.DataFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP POST DELETE requests via reactive programming.  These
 * requests are mapped to methods that return the flights available on
 * Southwest airlines for certain dates and certain cities
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
 * WebFlux uses the {@code @PostMapping} annotation to map HTTP POST
 * requests onto methods in the {@code FlightPriceControllerAsync}.
 * POST requests invoked from any HTTP web client (e.g., a web
 * browser) or command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/AirlineDBs/SWAAsync")
public class SWAControllerAsync {
    /**
     * The list of TripResponse objects and their associated prices.
     */
    private final List<TripResponse> mTrips;

    /**
     * Constructor initializes the field.
     */
    SWAControllerAsync() {
        mTrips = DataFactory
            // Initialize the list of TripResponse objects and their
            // associated prices from the SWA.txt file.
            .getTripList("airlineDBs/SWA.txt");
    }

    /**
     * This method finds all the trips on given departure date and the
     * flight leg for Southwest Airlines asynchronously.
     *
     * WebFlux maps HTTP POST requests sent to the /_getTripPrices
     * endpoint to this method.
     *
     * @param tripRequest Information about the trip, i.e., date and
     *         flight leg  
     * @return A Flux that emits all the trips for the given departure
     *         date and flight leg
     */
    @PostMapping("/_getTripPrices")
    private Flux<TripResponse> getTripPrices(@RequestBody TripRequest tripRequest) {
        return Flux
            // Convert the list of TripResponse objects into a Flux
            // stream.
            .fromIterable(mTrips)

            // Select only those TripResponse objects that equal the
            // tripRequest param.
            .filter(tripRequest::equals);
    }
}
