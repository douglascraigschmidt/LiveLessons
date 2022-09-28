package folders.common;

import folders.datamodel.Dirent;
import folders.datamodel.Document;
import folders.datamodel.Folder;
import folders.utils.ExceptionUtils;
import folders.utils.TestDataFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This Java utility class contains methods that can be used on both
 * the client and the server.
 */
public final class FolderOps {
    /**
     * A Java utility class should have a private constructor.
     */ 
    private FolderOps() {}

    /**
     * This factory method creates a folder from the given {@code
     * rootFile}.
     *
     * @param rootFile The root file in the file system
     * @param parallel A flag that indicates whether to create the
     *                 folder sequentially or in parallel
     *
     * @return An open folder containing all contents in the {@code rootFile}
     */
    public static Dirent fromDirectory(File rootFile,
                                       boolean parallel) {
        return StreamSupport
            // Create a parallel stream.
            .stream(Arrays
                    // Convert the array of File objects
                    // into a List.
                    .asList(Objects
                            .requireNonNull(rootFile
                                            .listFiles()))

                    // Convert the List into a parallel stream.
                    .spliterator(), parallel)

            // Eliminate rootPath to avoid infinite recursion.
            .filter(path -> !path.equals(rootFile))

            // Create a stream of Dirent objects.
            .map(path -> FolderOps
                 // Create and return a Dirent containing all the
                 // contents at the given path.
                 .createEntry(path, parallel))

            // Collect the results into a Folder containing all the
            // entries in stream.
            .collect(Collector
                     // Create a custom collector.
                     .of(() -> new Folder(rootFile),
                         Folder::addEntry,
                         Folder::merge));
    }

    /**
     * Create a new {@code entry} and return it.
     */
    static Dirent createEntry(File entry,
                              boolean parallel) {
        // Add entry to the appropriate list.
        if (entry.isDirectory())
            // Recursively create a folder from the entry.
            return FolderOps.fromDirectory(entry, parallel);
        else
            // Create a document from the entry and return it.
            return FolderOps.fromPath(entry);
    }

    /**
     * @param entry Either a file or directory
     * @return A new {@link Dirent} that encapsulates the {@code
     *         entry}
     */
    static Dirent createEntryParallel(File entry) {
        // Add entry to the appropriate list.
        if (entry.isDirectory())
            // Recursively create a folder from the entry in parallel.
            return FolderOps.fromDirectory(entry, true);
        else
            // Create a document from the entry and return it.
            return FolderOps.fromPath(entry);
    }

    /**
     * This factory method creates a {@link Dirent} document from the
     * file at the given {@code path}.
     *
     * @param path The path of the document in the file system
     * @return A {@link Dirent} containing the document's contents
     */
    public static Dirent fromPath(File path) {
        // Create an exception adapter.
        Function<Path, byte[]> getBytes = ExceptionUtils
            // mMake it easier to use a checked exception.
            .rethrowFunction(Files::readAllBytes);

        // Create a new document containing all the bytes of the
        // file at the given path.
        return new Document(new String(getBytes.apply(path.toPath())),
                            path);
    }

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
        return StreamSupport
            // Create a parallel or sequential stream from
            // a Dirent.
            .stream(rootFolder.spliterator(), parallel)

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
                     .splitAsStream(document, "\\r?\\n"))

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
        return FolderOps
            // Create a folder with all works in the root directory.
            .fromDirectory(TestDataFactory.getRootFolderFile(rootDir),
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
