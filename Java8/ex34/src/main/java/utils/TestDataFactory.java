package utils;

import datamodels.TripRequest;
import datamodels.Flight;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * This utility class contains methods that obtain test data on
 * {@link Flight} objects.
 */
public class TestDataFactory {
    /**
     * A utility class should always define a private constructor.
     */
    private TestDataFactory() {
    }

    /**
     * An array of flight information used for testing.
     */
    private static final String[] sFlightInfo = {
            // Feel free to add more rows to this array to generate more test data!
            "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T19:00:00,2025-02-02T07:00:00,LHR,JFK,600.00,AA,USD",
            "2025-02-01T19:00:00,2025-01-02T10:00:00,2025-02-01T07:00:00,2025-02-01T10:00:00,JFK,LHR,555.00,AA,USD",
            "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T19:00:00,2025-02-02T07:00:00,LHR,JFK,100.00,AA,USD",
            "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T19:00:00,2025-02-02T07:00:00,JFK,LHR,100.00,AA,USD",
            "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T19:00:00,2025-02-02T07:00:00,LHR,JFK,888.00,SWA,USD",
            "2025-02-01T19:00:00,2025-01-02T10:00:00,2025-02-01T07:00:00,2025-02-01T10:00:00,JFK,LHR,666.00,SWA,USD",
            "2025-02-01T19:00:00,2025-01-02T10:00:00,2025-02-01T07:00:00,2025-02-01T10:00:00,JFK,LHR,555.00,SWA,USD",
            "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T19:00:00,2025-02-02T07:00:00,LHR,JFK,100.00,SWA,USD",
            "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T19:00:00,2025-02-02T07:00:00,LHR,JFK,444.00,LH,EUR",
            "2025-02-01T19:00:00,2025-01-02T10:00:00,2025-02-01T07:00:00,2025-02-01T10:00:00,JFK,LHR,555.00,LH,EUR",
            "2025-02-01T19:00:00,2025-01-02T10:00:00,2025-02-01T07:00:00,2025-02-01T10:00:00,JFK,LHR,333.00,LH,EUR",
            "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T19:00:00,2025-02-02T07:00:00,LHR,JFK,777.00,LH,EUR",
            "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T19:00:00,2025-02-02T07:00:00,LHR,JFK,888.00,BAW,GBP",
            "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T19:00:00,2025-02-02T07:00:00,JFK,LHR,444.00,BAW,GBP",
            "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T19:00:00,2025-02-02T07:00:00,LHR,JFK,888.00,BAW,GBP",
            "2025-02-01T19:00:00,2025-01-02T10:00:00,2025-02-01T07:00:00,2025-02-01T10:00:00,JFK,LHR,666.00,BAW,GBP"
    };

    /**
     * Return a Stream containing {@link Flight} objects that
     * match the given {@link TripRequest}.
     */
    public static Stream<Flight> findFlights(TripRequest tripRequest) {
        return Stream
            // Convert the array into a stream.
            .of(sFlightInfo)

            // Convert the strings into Flight objects.
            .map(TestDataFactory::makeFlight)

            // Only keep TripResponse objects that match the
            // tripRequest.
            .filter(tripRequest::equals);
    }

    /**
     * Factory method that converts a string of comma-separated values
     * indicating the date/time of the initial departure/arrival, the
     * date/time of the return departure/arrival, the airport code for
     * the departing/arriving airports, the price, and the airline
     * code into the corresponding {@link Flight}.
     *
     * @param tripString A string containing comma-separated values
     * indicating information about the trip
     * @return The corresponding {@link Flight}
     */
    private static Flight makeFlight(String tripString) {
        String[] result = tripString.split(",");

        // Create and return a TripResponse via a factory method.
        return Flight
            .valueOf(// Date/time of the initial departure.
                     LocalDateTime.parse(result[0]),

                     // Date/time of the initial arrival.
                     LocalDateTime.parse(result[1]),

                     // Date/time of the return departure.
                     LocalDateTime.parse(result[2]),

                     // Date/time of the return arrival.
                     LocalDateTime.parse(result[3]),

                     // Code for the departure airport.
                     result[4],

                     // Code for the arrival airport.
                     result[5],

                     // Price of the flight.
                     Double.parseDouble(result[6]),

                     // Code for the airline.
                     result[7],

                     // Currency.
                     result[8]);
    }
}
