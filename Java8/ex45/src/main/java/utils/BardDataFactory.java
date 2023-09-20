package utils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * This utility class contains methods for obtaining test data.
 */
public class BardDataFactory {
    /**
     * A utility class should always define a private constructor.
     */
    private BardDataFactory() {
    }

    /**
     * Split the input data in the given {@code filename} using the
     * {@code splitter} regular expression and return a list of
     * strings.
     */
    public static List<String> getInput(String filename,
                                        String splitter) {
        try {
            // Convert the filename into a pathname.
            URI uri = ClassLoader
                .getSystemResource(filename).toURI();

            // Open the file and get all the bytes.
            var bytes =
                new String(Files.readAllBytes(Paths.get(uri)));

            return Pattern
                // Compile splitter into a regular expression (regex).
                .compile(splitter)

                // Use the regex to split the file into a stream of
                // strings.
                .splitAsStream(bytes)

                // Filter out any empty strings.
                .filter(string -> !string.isEmpty())
                
                // Collect the results into a List of String objects.
                .toList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
