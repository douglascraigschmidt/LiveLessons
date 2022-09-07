import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * This program demonstrates how to use modern Java features (including
 * lambda expressions, method references, and parallel streams) to build
 * a cosine vector {@link Map} from a CSV file containing the cosine
 * values for movies.
 */
class ex40 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Create a Map containing the movie dataset.
        Map<String, List<Double>> map = vectorMap("dataset.csv");

        System.out.println("Size of the movie dataset = "
                           + map.size());

        // Print the contents of the movie dataset.
        map.forEach((title, cosineVector) -> {
                System.out.println("Title = \""
                                   + title
                                   + "\" cosine vector "
                                   + cosineVector);
            });
    }

    /**
     * Constructs a {@link Map} that loads the cosine vector map from
     * resources.
     *
     * @param dataset Dataset resource file name
     * @return A map containing all movie cosine vectors
     */
    public static Map<String, List<Double>> vectorMap(final String dataset) {
        return loadCSVFile(requireNonNull(ex40.class
                                          .getResource("/" + dataset))
                           .getFile());
    }

    /**
     * Factory method that builds a cosine vector {@link Map} from a
     * CSV file containing the cosine values for all the movies.
     *
     * @param path Resource file path
     * @return A {@link Map} that associates the movie title with the
     *         cosine vector for each movie
     */
    private static Map<String, List<Double>> loadCSVFile(String path) {
        // Read all lines from filename and convert into a Stream of
        // Strings.  The "try-with-resources" statement ensures the
        // Stream cleanup is done automatically!
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
            return lines
                // Convert the stream into a parallel stream.
                .parallel()

                // Consume the first line, which gives the format of
                // the CSV file.
                .skip(1)

                // Divide the title from the cosine vector.
                .map(line -> line.split(";"))

                // Put the title and the associated cosine vector in
                // the map.
                .map(strings -> new SimpleEntry<>
                     (strings[0], parseVector(strings[1])))

                // Trigger intermediate processing and collect the
                // results into a Map.
                .collect(toMap(SimpleEntry::getKey,
                               SimpleEntry::getValue,
                               (x, y) -> x));
        } catch (IOException e) {
            // There is no point in continuing if the underlying
            // database has loading issues.
            System.out.println("IO Exception Occurred: " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert the {@link String} representation of movie cosine
     * values into a {@link List}.
     *
     * @param movieValues The {@link String} vector of cosine values
     *                    representing a movie
     * @return A {@link List} containing the movie cosine values
     */
    private static List<Double> parseVector(String movieValues) {
        // Access the vector that's stored in String form.
        return Pattern
            // Compile splitter into a regular expression (regex) that
            // splits by the space separating the values.
            .compile(" ")

            // Use the regex to split the vector into a stream of
            // strings.
            .splitAsStream(movieValues
                           // Remove leading/trailing brackets and
                           // leading space.
                           .substring(3, movieValues.length() - 2))

            // Convert the stream to a parallel stream.
            .parallel()

            // Convert each cosine value from String to Double.
            .map(Double::valueOf)

            // Collect the Stream<Double> into an List<Double>.
            .collect(toList());
    }
}
