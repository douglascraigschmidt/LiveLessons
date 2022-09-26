package microservices.airports.controller;

import datamodels.AirportInfo;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import utils.DataFactory;

import java.util.List;

/**
 * This Spring controller demonstrates how WebFlux can be used to
 * handle RSocket requests via reactive programming.  These requests
 * are mapped to method(s) that convert between various currencies
 * asynchronously.
 *
 * In Spring's approach to building RSocket services, message
 * requests are handled by a controller that defines the
 * endpoints/routes for each supported operation, i.e.,
 * {@code @MessageMapping}.  These components are
 * identified by the @Controller annotation below.
 *
 * WebFlux uses the {@code @MessageMapping} annotation to map RSocket
 * requests onto methods in the {@code ExchangeRateControllerRSocket}.
 */
@Controller
public class AirportListControllerRSocket {
    /**
     * The list of AirportInfo objects.
     */
    private final List<AirportInfo> mAirportList;

    /**
     * Constructor initializes the field.
     */
    AirportListControllerRSocket() {
        mAirportList = DataFactory
            // Initialize the list of AirportInfo objects from the
            // AirportList.txt file.
            .getAirportInfoList("airport-list.txt");
    }

    /**
     * This method finds information about all the airports
     * asynchronously.
     *
     * WebFlux maps RSocket requests sent to the _getAirportList
     * endpoint to this method.
     *
     * @return A Flux that emits all {@code AirportInfo} objects
     */
    @MessageMapping("_getAirportList")
    private Flux<AirportInfo> getAirportInfo() {
        return Flux
            // Convert the list of AirportInfo objects into a Flux
            // stream.
            .fromIterable(mAirportList);
    }
}
