package utils;

import datamodels.TripRequest;
import datamodels.TripResponse;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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
     * Return a list of {@code TripResponse} objects that match the given {@code tripRequest}.
     */
    public static List<TripResponse> findFlights(TripRequest tripRequest) {
        return Stream
            // Create a stream of all the airline databases.
            .of("AA.txt", "SWA.txt")

            // Get a list of TripResponse objects from each airline database.
            .map(TestDataFactory::getTripList)

            // Eliminate any null entries.
            .filter(Objects::nonNull)

            // Flatten the list of TripResponse objects into a stream of TripResponse objects.
            .flatMap(List::stream)

            // Only keep TripResponse objects that match the tripRequest.
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
     *
     * @param line
     * @return
     */
    private static TripResponse makeTrip(String line) {
        String[] result = line.split(",");

        // Create and return a TripResponse via a factory method.
        return TripResponse
            .valueOf(LocalDateTime.parse(result[0]),
                     LocalDateTime.parse(result[1]),
                     LocalDateTime.parse(result[2]),
                     LocalDateTime.parse(result[3]),
                     result[4],
                     result[5],
                     Double.parseDouble(result[6]),
                     result[7]);
    }
}
