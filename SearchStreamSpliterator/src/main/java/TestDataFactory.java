import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
     * Return the input data in the given @a filename as a string.
     */
    public static String getInput(String filename) {
        try {
            // Convert the filename into a pathname.
            URI uri = ClassLoader.getSystemResource(filename).toURI();

            // Open the file and get all the bytes.
            return new String(Files.readAllBytes(Paths.get(uri)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the phrase list in the @a filename as a list of Strings.
     */
    public static List<String> getPhraseList(String filename) {
        try {
            // Read all lines from filename and return a list of
            // Strings.
            return Files.readAllLines(Paths.get(ClassLoader.getSystemResource
                                                (filename).toURI()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
