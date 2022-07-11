package utils;

import datamodels.TripRequest;
import datamodels.Flight;
import reactor.core.publisher.Flux;
import utils.FlightFactory;

import java.time.LocalDateTime;

/**
 * This utility class contains methods that obtain test data.
 */
public class TestDataFactory {
    /**
     * A utility class should always define a private constructor.
     */
    private TestDataFactory() {
    }

    /**
     * Return a Flux that emits {@code TripResponse} objects that
     * match the given {@code tripRequest}.
     */
    public static Flux<Flight> findFlights(TripRequest tripRequest) {
        return Flux
            // Convert the List into a stream.
            .fromIterable(FlightFactory.flights())

            // Only keep TripResponse objects that match the
            // tripRequest.
            .filter(tripRequest::equals);
    }
}
