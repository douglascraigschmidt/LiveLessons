package utils;

import datamodels.TripRequest;
import datamodels.Flight;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This utility class contains methods that obtain test data.
 */
public class TestDataFactory {
    /**
     * Number of concatenations.
     */
    private static int sMAX_CONCATENATIONS = 100_000;

    /**
     * A utility class should always define a private constructor.
     */
    private TestDataFactory() {
    }

    private static final String[] sFlightInfo1 = {
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,60,AA,USD",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,60,SWA,USD",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,60,LH,EUR",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,60,BAW,GBP",
    };

    private static final String[] sFlightInfo2 = {
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,888,BAW,GBP",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,444,BAW,GBP",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,888,BAW,GBP",
        "2025-02-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,666,BAW,GBP",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,600,AA,USD",
        "2025-02-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,555,AA,USD",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,100,AA,USD",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,100,AA,USD",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,888,SWA,USD",
        "2025-02-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,666,SWA,USD",
        "2025-02-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,555,SWA,USD",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,100,SWA,USD",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,444,LH,EUR",
        "2025-02-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,555,LH,EUR",
        "2025-02-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,333,LH,EUR",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,777,LH,EUR",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,888,BAW,GBP",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,444,BAW,GBP",
        "2025-01-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,888,BAW,GBP",
        "2025-02-01T07:00:00,2025-01-01T10:00:00,2025-02-01T07:00:00,2025-02-02T07:00:00,LHR,JFK,666,BAW,GBP"
    };

    /**
     * Use the Java Stream concat() method to concatenate the contents
     * of {@link List} together {@code n} times.
     */
    static List<String> concatStream(List<String> list, int n) {
        return IntStream
            // Create a stream of integers of size n.
            .rangeClosed(1, n)

            // Repeatedly emit the list 'n' times.
            .mapToObj(__ -> list)

            // Flatmap the stream of lists of strings into a stream of
            // strings.
            .flatMap(List::stream)

            // Collect the results into a list of strings.
            .collect(toList());
    }

    /**
     * Return a {@link List} that contains {@code TripResponse} objects
     * that match the given {@code tripRequest}.
     */
    public static List<Flight> findFlights(TripRequest tripRequest) {
        // Get the List of flights.
        return Stream
            // Create a stream containing lists of strings.
            .of(List.of(sFlightInfo1),
                concatStream(List.of(sFlightInfo2),
                        sMAX_CONCATENATIONS))

            // Flatten the stream of lists of strings into a
            // string of strings.
            .flatMap(List::stream)

            // Convert the stream of strings into a stream of
            // Flight objects.
            .map(TestDataFactory::makeTrip)

            // Only keep TripResponse objects that match the
            // tripRequest.
            .filter(tripRequest::equals)

            // Collect into a List.
            .collect(toList());
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
    private static Flight makeTrip(String tripString) {
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
                        Integer.parseInt(result[6]),

                        // Code for the airline.
                        result[7],

                        // Currency.
                        result[8]);
    }
}
