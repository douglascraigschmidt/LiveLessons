package livelessons.utils;

import livelessons.utils.SharedString;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * This utility class contains methods for obtaining test data.
 */
public class TestDataFactory {
    /**
     * A utility class should always define a private constructor.
     */
    private TestDataFactory() {
    }

    /**
     * Return the input data in the given @a filename as a list of
     * Strings.
     */
    public static List<CharSequence> getInput(String filename,
                                              String splitter) {
        try {
            // Convert the filename into a pathname.
            URI uri = ClassLoader.getSystemResource(filename).toURI();

            // Open the file and get all the bytes.
            CharSequence bytes =
                new String(Files.readAllBytes(Paths.get(uri)));

            return Pattern
                // Compile splitter into a regular expression (regex).
                .compile(splitter)

                // Use the regex to split the file into a stream of
                // strings.
                .splitAsStream(bytes)

                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())
                
                // Collect the results into a string.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the input data in the given @a filename as a list of
     * SharedStrings.
     */
    public static List<CharSequence> getSharedInput(String filename,
                                                    String splitter) {
        try {
            // Convert the filename into a pathname.
            URI uri = ClassLoader.getSystemResource(filename).toURI();

            // Open the file and get all the bytes.
            CharSequence bytes =
                new String(Files.readAllBytes(Paths.get(uri)));

            return
                // Compile a regular expression that's used to split
                // the file into a list of Strings.
                Pattern.compile(splitter)

                // Creates a stream from the given input
                // sequence around matches of this pattern.
                .splitAsStream(bytes)

                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Map each String to a SharedString to
                // eliminate copying overhead.
                .map(string -> 
                     new SharedString(string.toCharArray()))

                // Collect the results into a string.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the phrase list in the @a filename as a list of
     * non-empty strings.
     */
    public static List<String> getPhraseList(String filename) {
        try {
            return Files
                // Read all lines from filename.
                .lines(Paths.get(ClassLoader.getSystemResource
                                        (filename).toURI()))
                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Collect the results into a string.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
