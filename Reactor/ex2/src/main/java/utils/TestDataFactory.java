package utils;

import datamodels.Flight;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * This utility class contains methods that obtain test data.
 */
public class TestDataFactory {
    /**
     * Preferred currency.
     */
    public static String CURRENCY = "USD";

    /**
     * A utility class should always define a private constructor.
     */
    private TestDataFactory() {
    }

    /**
     * Return a Flux that emits {@code TripResponse} objects that
     * match the given {@code flightRequest}.
     */
    public static Flux<Flight> randomFlights(List<String> currencies) {
        // Convert the List into a stream.
        return Flux.fromIterable(
                FlightFactory.buildExpectedFlights(CURRENCY, currencies));
    }
}
