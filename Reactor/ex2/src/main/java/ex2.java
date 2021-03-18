import datamodels.TripRequest;
import datamodels.TripResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;
import utils.TestDataFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class ex2 {
    /**
     * The trip flight leg used for the tests.
     */
    private static final TripRequest sTrip = TripRequest
        .valueOf(LocalDateTime.parse("2025-01-01T07:00:00"),
                 LocalDateTime.parse("2025-02-01T19:00:00"),
                 "LHR",
                 "JFK",
                 1);

     /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws InterruptedException {
        // Get all the flights.
        final Flux<TripResponse> flights = TestDataFactory.findFlights(sTrip);

        // Find the cheapest flights.
        Flux<TripResponse> lowestPrices = MathFlux
            // Find the cheapest flight.
            .min(flights, Comparator.comparing(TripResponse::getPrice))
            // Create a Flux that contains the cheapest flights.
            .flatMapMany(min -> flights
                        // Only allow flights that match the cheapest.
                        .filter(tr -> tr.getPrice().equals(min.getPrice())));

        // Print the cheapest flights.
        lowestPrices
           .subscribe(System.out::println);
    }
}
