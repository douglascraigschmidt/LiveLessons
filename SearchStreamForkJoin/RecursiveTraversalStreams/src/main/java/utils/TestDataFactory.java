package utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
     * Return a folder object that's used to search a recursive
     * directory containing the complete works of William Shakespeare.
     */
    public static Folder getRootFolder(String rootFolderName,
                                       boolean parallel)
        throws URISyntaxException, IOException {
        return Folder
            .fromDirectory(new File(ClassLoader
                                    .getSystemResource(rootFolderName)
                                    .toURI()),
                           parallel);
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
