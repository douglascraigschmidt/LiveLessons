package utils;

import datamodels.TripRequest;
import datamodels.TripResponse;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
     * Return a list of {@code TripResponse} objects that match the
     * given {@code tripRequest}.
     */
    @SneakyThrows
    public static List<TripResponse> findFlights(TripRequest tripRequest) {
        return Files
            // Create a stream of the paths for all airline databases.
            .list(Paths.get(ClassLoader
                            .getSystemResource("airlineDBs")
                            .toURI()))

            // Flatten the contents of all the airline databases into
            // a stream of strings containing comma-separated values.
            .flatMap(ExceptionUtils.rethrowFunction(Files::lines))

            // Filter out any empty strings.
            .filter(((Predicate<String>) String::isEmpty).negate())

            // Convert the strings into TripResponse objects.
            .map(TestDataFactory::makeTrip)

            // Only keep TripResponse objects that match the
            // tripRequest.
            .filter(tripRequest::equals)

            // Collect the results into a list.
            .collect(toList());
    }

    /**
     * Return the Trip list in the {@code filename} as a list of
     * non-empty Trip objects.
     */
    public static List<TripResponse> getTripList(String filename) {
        try {
            return Files
                // Read all lines from filename and convert into a
                // stream of strings.
                .lines(Paths.get(ClassLoader.getSystemResource
                                 (filename).toURI()))

                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Convert the strings into TripResponse objects.
                .map(TestDataFactory::makeTrip)

                // Trigger intermediate operations and collect the
                // results into a list of Trip objects.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Factory method that converts a string of comma-separated values
     * indicating the date/time of the initial departure/arrival, the
     * date/time of the return departure/arrival, the airport code for
     * the departing/arriving airports, the price, and the airline
     * code into the corresponding {@code TripResponse}.
     *
     * @param tripString A string containing comma-separated values
     * indicating information about the trip
     * @return The corresponding {@code TripResponse}
     */
    private static TripResponse makeTrip(String tripString) {
        String[] result = tripString.split(",");

        // Create and return a TripResponse via a factory method.
        return TripResponse
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
                     result[7]);
    }
}
