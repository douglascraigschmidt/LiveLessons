package utils;

import datamodels.Trip;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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
     * Return the Trip list in the {@code filename} as a list of
     * non-empty Trip objects.
     */
    public static List<Trip> getTripList(String filename) {
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
    private static Trip makeTrip(String line) {
        String[] result = line.split(",");

        return new Trip(LocalDateTime.parse(result[0]),
                        LocalDateTime.parse(result[1]),
                        LocalDateTime.parse(result[2]),
                        LocalDateTime.parse(result[3]),
                        result[4],
                        result[5],
                        Double.parseDouble(result[6]));
    }
}
