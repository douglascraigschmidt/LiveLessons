import datamodels.TripRequest;
import datamodels.TripResponse;
import utils.TestDataFactory;

import java.time.LocalDateTime;
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
        final List<TripResponse> flights = TestDataFactory.findFlights(sTrip);

        flights.forEach(System.out::println);
    }
}
