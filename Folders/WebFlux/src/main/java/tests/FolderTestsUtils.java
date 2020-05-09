package tests;

import folder.Dirent;
import folder.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This Java utility class contains various helper methods shared by
 * {@code FolderTests} and {@code FolderTestsParallel}.
 */
public final class FolderTestsUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private FolderTestsUtils() {}

    /**
     * Count the number of times {@code searchWord} appears in the
     * {@code document}.
     *
     * @param document In-memory document containing text
     * @param searchWord Word to search for in the document
     * @return The # of times {@code searchWord} appears in {@code document}.
     */
    public static Mono<Long> occurrencesCount(Dirent document,
                                              String searchWord) {
        // Return a mono that counts the # of times searchWord appears
        // in the document.
        return 
            // Split the document into a stream of words.
            splitAsFlux(document,
                        "\\W+")

            // Only consider words that match.
            .filter(searchWord::equals)

            // Count # of times searchWord appears in the stream.
            .count();
    }

    /**
     * Creates a stream from the {@code document} around matches of
     * this {@code regex} pattern.
     *
     * @param document The document to be split
     * @param regex The regular expression to compile
     * @return The stream of strings computed by splitting the {@code
     *         document} around matches of this {@code regex} pattern
     */
    public static Flux<String> splitAsFlux(Dirent document,
                                           String regex) {
        // Return an stream of strings.
        return Flux
            // Create an stream from an array of strings.
            .fromArray(document
                       // Create a string containing the document's
                       // contents.
                       .getContents().toString()

                       // Split the string into an array of strings
                       // based on the regular expression.
                       .split(regex));
    }

    /**
     * @return True if {@code dirent} is a document, else false
     */
    public static boolean isDocument(Dirent dirent) {
        // Return true if dirent is a document, else false.
        return dirent instanceof Document;
    }
}
