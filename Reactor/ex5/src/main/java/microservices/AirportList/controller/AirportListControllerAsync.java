package microservices.AirportList.controller;

import datamodels.AirportInfo;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import utils.DataFactory;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle HTTP GET requests via asynchronous reactive programming.
 * These GET requests are mapped to methods that return information
 * about all the airports (e.g., the three-letter airport code and
 * airport name) asynchronously.
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
@RequestMapping("/microservices/AirportListAsync")
public class AirportListControllerAsync {
    /**
     * The list of AirportInfo objects.
     */
    private final List<AirportInfo> mAirportList;

    /**
     * Constructor initializes the field.
     */
    AirportListController() {
        mAirportList = DataFactory
            // Initialize the list of AirportInfo objects from the
            // AirportList.txt file.
            .getAirportInfoList("airport-list.txt");
    }

    /**
     * This method finds information about all the airports
     * asynchronously.
     *
     * WebFlux maps HTTP GET requests sent to the /_getAirportList
     * endpoint to this method.
     *
     * @return A Flux that emits all {@code AirportInfo} objects
     */
    @GetMapping("/_getAirportList")
    private Flux<AirportInfo> getAirportInfoAsync() {
        return Flux
            // Convert the list of AirportInfo objects into a Flux
            // stream.
            .fromIterable(mAirportList);
    }
}
