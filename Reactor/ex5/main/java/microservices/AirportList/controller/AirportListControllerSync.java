package microservices.AirportList.controller;

import datamodels.AirportInfo;
import org.springframework.web.bind.annotation.*;
import utils.DataFactory;

import java.util.List;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET requests via object-oriented programming.  These
 * GET requests are mapped to methods that return information about
 * all the airports (e.g., the three-letter airport code and airport
 * name) synchronously.
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
 * requests onto methods in the {@code FlightPriceControllerSync}.
 * GET requests invoked from any HTTP web client (e.g., a web browser)
 * or command-line utility (e.g., Curl or Postman).
 */
@RestController
@RequestMapping("/microservices/AirportListSync")
public class AirportListControllerSync {
    /**
     * The list of AirportInfo objects.
     */
    private final List<AirportInfo> mAirportList;

    /**
     * Constructor initializes the field.
     */
    AirportListControllerSync() {
        mAirportList = DataFactory
            // Initialize the list of AirportInfo objects from the
            // AirportList.txt file.
            .getAirportInfoList("airport-list.txt");
    }

    /**
     * This method finds information about all the airports
     * synchronously.
     *
     * WebFlux maps HTTP GET requests sent to the /_getAirportList
     * endpoint to this method.
     *
     * @return A List that contains all the trips for the given departure
     *         date and flight leg
     */
    @GetMapping("/_getAirportList")
    private List<AirportInfo> getAirportInfo() {
        // Return the airport list.
        return mAirportList;
    }
}
