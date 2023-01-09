package folders.common;

import folders.datamodel.Dirent;
import folders.datamodel.Document;
import folders.datamodel.Folder;
import folders.utils.TestDataFactory;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This Java utility class contains methods used on both the
 * client and the server.
 */
public final class FolderOps {
    /**
     * A Java utility class should have a private constructor.
     */ 
    private FolderOps() {}

    /**
     * @return True if {@code dirent} is a document, else false
     */
    public static boolean isDocument(Dirent dirent) {
        // Return true if dirent is a document, else false.
        // return dirent instanceof Document;
        return dirent instanceof Document;
    }

    /**
     * Count the number of entries in the folder emitted by {@code
     * rootFolder}.
     *
     * @param rootFolder A {@link Dirent} to an in-memory folder
     *                    containing the works
     * @param parallel Flag indicating whether to run the tests
     *                   concurrently or not
     * @return A {@link Long} that counts the number of entries
     *         in the folder emitted by {@code rootFolder}
     */
    public static Long countEntries(Dirent rootFolder,
                                    boolean parallel) {
        // Return a count of the # of entries starting at rootDir.
        return rootFolder
            // Create a parallel or sequential stream from
            // a Dirent.
            .stream(parallel)

            // Count the number of entries in the stream.
            .count();
    }

    /**
     * Count the number of lines in the recursively-structured folder
     * starting at {@code rootFolder}.
     *
     * @param rootFolder A {@link Long} that contains an in-memory
     *                    folder containing the works
     * @param parallel Flag indicating whether to run the tests
     *                   concurrently or not
     * @return A {@link Long} that counts of the number of
     *         lines in the folder starting at {@code rootFolderM}
     */
    public static Long countLines(Dirent rootFolder,
                                  boolean parallel) {
        // This function counts the # of lines in a Dirent.
        return rootFolder
            // Create a parallel or sequential stream from
            // a Dirent.
            .stream(parallel)

            // Only search documents.
            .filter(FolderOps::isDocument)

            // Process each document.
            .flatMap(document -> FolderOps
                     // Split the document into lines.
                     .splitAsStream(document,
                                    "\\r?\\n|\\r")

            // Count the total number of strings.
            .count();
    }

    /**
     * Find all occurrences of {@code word} in {@code rootFolderM}
     * return the number of matches.
     *
     * @param rootFolder A {@link Dirent} to an in-memory folder that
     *                    emits the contents
     * @param word Word to search for in the folder
     * @param parallel Flag indicating whether to run the tests
     *                     concurrently or not
     * @return A {@link Long} containing the number of times {@code
     *         word} appears the folder emitted by {@code rootFolder}
     */
    public static Long countWordMatches(Dirent rootFolder,
                                        String word,
                                        boolean parallel) {
        // This function counts # of 'word' matches in a Dirent.
        return rootFolder
            // Create a parallel or sequential stream from
            // a Dirent.
            .stream(parallel)

            // Only search documents.
            .filter(FolderOps::isDocument)

            // Search document looking for matches.
            .mapToLong(document -> FolderOps
                       // Count # of times word matches in document.
                       .occurrencesCount(document,
                                         word))

            // Sum the results.
            .sum();
    }

    /**
     * This method returns all the documents where a {@code
     * searchWord} appears in the folder emitted by {@code
     * rootFolder}.
     *
     * @param rootFolder A {@link Dirent} to an in-memory folder
     *                    containing the works
     * @param searchWord Word to search for in the folder
     * @param parallel Flag indicating whether to run the tests
     *                   concurrently or not
     * @return A {@link List} that emits all the documents where
     *         {@code searchWord} appears in the folder emitted by
     *         {@code rootFolder}
     */
    public static List<Dirent> getDocuments(Dirent rootFolder,
                                            String searchWord,
                                            boolean parallel) {
        // Return a List containing all documents where searchWord
        // appears in the folder starting at the root directory.
        return rootFolder
            // Create a parallel or sequential stream from
            // a Dirent.
            .stream(parallel)

            // Only consider documents.
            .filter(FolderOps::isDocument)

            // Only consider documents containing the search
            // searchWord.
            .filter(document -> FolderOps
                    .wordIsInDocument(document,
                                      searchWord))

            // Trigger intermediate operations and collect into a
            // List.
            .toList();
    }

    /**
     * This method returns a {@link Dirent} that contains all the
     * entries in the folder, starting at {@code rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param parallel True if the folder should be created
     *                   concurrently or not
     * @return A {@link Dirent} that contains all the entries in the
     *         folder starting at {@code rootDir}
     */
    public static Dirent createFolder(String rootDir,
                                      Boolean parallel) {
        // Return a Dirent containing the initialized folder.
        return Folder
            // Create a folder with all works in the root directory.
            .fromFolder(TestDataFactory.getRootFolderFile(rootDir),
                        parallel);
    }

    /**
     * Creates a stream from the {@code document} around matches of
     * this {@code regex} pattern.
     *
     * @param document The document to be split
     * @param regex The regular expression to compile
     * @return A {@link Stream} of strings computed by splitting the
     *         {@code document} around matches of this {@code regex}
     *         pattern
     */
    public static Stream<String> splitAsStream(Dirent document,
                                               String regex) {
        // Return a Stream of String objects.
        return Pattern
            // Compile the regular expression (regex).
            .compile(regex)

            // Split the string into a stream of strings based on the
            // regular expression.
            .splitAsStream(document
                           // Create a string containing the
                           // document's contents.
                           .getContents().toString());
    }

    /**
     * Check if the {@code searchWord} appears in the {@code document}.
     *
     * @param document In-memory document containing text
     * @param searchWord Word to search for in the document
     * @return A {@code boolean} containing true if {@code searchWord}
     *         appears in {@code document}
     */
    public static boolean wordIsInDocument(Dirent document,
                                           String searchWord) {
        // Return true if 'searchWord' appears in the document.

        return FolderOps
            // Split the document into a stream of words.
            . splitAsStream(document,
                            "\\W+")

            // Return true if any values of this stream match the
            // predicate.
            .anyMatch(searchWord::equals);
    }

    /**
     * Count the number of times {@code searchWord} appears in the
     * {@code document}.
     *
     * @param document In-memory document containing text
     * @param searchWord Word to search for in the document
     * @return The # of times {@code searchWord} appears in {@code document}.
     */
    public static Long occurrencesCount(Dirent document,
                                        String searchWord) {
        // Return a Long that counts the # of times searchWord appears
        // in the document.
        return FolderOps
            // Split the document into a stream of words.
            .splitAsStream(document,
                           "\\W+")

            // Only consider words that match.
            .filter(searchWord::equals)

            // Count # of times searchWord appears in the stream.
            .count();
    }
}
