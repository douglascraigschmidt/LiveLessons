package utils;

import datamodels.AirportInfo;
import datamodels.TripRequest;
import datamodels.TripResponse;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * This utility class contains methods that obtain test data.
 */
public class DataFactory {
    /**
     * A utility class should always define a private constructor.
     */
    private DataFactory() {
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
            .map(DataFactory::makeTrip)

            // Only keep TripResponse objects that match the
            // tripRequest.
            .filter(tripRequest::equals)

            // Collect the results into a list.
            .collect(toList());
    }

    /**
     * @return The contents in the {@code filename} as a list of
     * non-empty {@code TripResponse} objects.
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
                .map(DataFactory::makeTrip)

                // Trigger intermediate operations and collect the
                // results into a list of TripResponse objects.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return The contents in the {@code filename} as a list of
     * non-empty {@code AirportInfo} objects.
     */
    public static List<AirportInfo> getAirportInfoList(String filename) {
        try {
            return Files
                // Read all lines from filename and convert into a
                // stream of strings.
                .lines(Paths.get(ClassLoader.getSystemResource
                                 (filename).toURI()))

                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Convert the strings into AirportInfo objects.
                .map(DataFactory::makeAirportInfo)

                // Trigger intermediate operations and collect the
                // results into a list of AirportInfo objects.
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
     * @param tripCSV A string containing comma-separated values
     * indicating information about the trip
     * @return The corresponding {@code TripResponse} object
     */
    private static TripResponse makeTrip(String tripCSV) {
        // Split the tripCSV string via the hyphen (',') character.
        String[] result = tripCSV.split(",");

        // Create and return a TripResponse via a factory method.
        return TripResponse
            .valueOf(// Date/time of the initial departure.
                     DateUtils.parse(result[0]),
                     // Date/time of the initial arrival.
                     DateUtils.parse(result[1]),

                     // Date/time of the return departure.
                     DateUtils.parse(result[2]),

                     // Date/time of the return arrival.
                     DateUtils.parse(result[3]),

                     // Code for the departure airport.
                     result[4],

                     // Code for the arrival airport.
                     result[5],

                     // Price of the flight.
                     Double.parseDouble(result[6]),

                     // Code for the airline.
                     result[7]);
    }

    /**
     * Factory method that converts a string of hyphen-separated
     * values into the corresponding {@code AirportInfo}.
     *
     * @param airportInfo A string containing comma-separated values
     * indicating information about the trip
     * @return The corresponding {@code AirportInfo} object
     */
    private static AirportInfo makeAirportInfo(String airportInfo) {
        // Split the airportInfo string via the hyphen ('-')
        // character.
        String[] result = airportInfo.split("-");

        // Create and return an AirportInfo via a factory method.
        return AirportInfo
            .valueOf(result[0],
                     result[1]);
    }
}
