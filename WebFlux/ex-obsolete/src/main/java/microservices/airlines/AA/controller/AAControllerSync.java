package microservices.airlines.AA.controller;

import datamodels.TripRequest;
import datamodels.TripResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.DataFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP POST requests via object-oriented programming.  These
 * POST requests are mapped to methods that return the flights
 * available on American airlines for certain dates and certain cities
 * synchronously.
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
 * POST requests invoked from any HTTP web client (e.g., a web
 * browser) or command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/airlines/AASync")
public class AAControllerSync {
    /**
     * The list of TripResponse objects and their associated prices.
     */
    private final List<TripResponse> mTrips;

    /**
     * Constructor initializes the field.
     */
    AAControllerSync() {
        mTrips = DataFactory
            // Initialize the list of TripResponse objects and their
            // associated prices from the AA.txt file.
            .getTripList("airlineDBs/AA.txt");
    }

    /**
     * This method finds all the trips on given departure date and the
     * flight leg for American Airlines synchronously.
     *
     * WebFlux maps HTTP POST requests sent to the /_getTripPrices
     * endpoint to this method.
     *
     * @param tripRequest Information about the trip, i.e., date and
     *        flight leg
     * @return A List that contains all the trips for the given departure
     *         date and flight leg
     */
    @PostMapping("/_getTripPrices")
    // @@ Monte, this is the method that I'd like to be connected to 
    // the persistent JPA objects.  Right now, it reads from an
    // in-memory list, but it should read from a database!
    private List<TripResponse> getTripPrices(@RequestBody TripRequest tripRequest) {
        return mTrips
            // Convert the list of TripResponse objects into a stream.
            .stream()

            // Select only those TripResponse objects that equal the
            // tripRequest param.
            .filter(tripRequest::equals)

            // Collect into a list.
            .collect(toList());
    }
}
