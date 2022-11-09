package tests;

import folder.Dirent;
import folder.Document;
import folder.Folder;
import utils.TestDataFactory;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static utils.Options.display;
import static utils.Options.sVoid;
import static utils.StreamOfFuturesCollector.toFuture;

/**
 * This Java utility class defines various methods that perform async
 * operations on a recursively-defined folder structure.
 */
public final class Tests {
    /**
     * Create a new folder asynchronously.
     *
     * @return A future to the folder that completes when the folder
     *         is created
     */
    public static CompletableFuture<Dirent> createFolder(String root) {
        // Return a future that completes when folder's initialized.
        return Folder
            // Asynchronously create a folder containing all the works
            // in the root directory.
            .fromDirectory(TestDataFactory.getRootFolderFile(root));
    }

    /**
     * Synchronously count the number of entries in the folder.
     */
    public static CompletableFuture<Void> countEntries(Dirent folder) {
        long folderCount = folder
            // Create a stream from the folder.
            .stream()

            // Count the number of elements in the stream.
            .count();

        display("number of entries in the folder = "
                + folderCount);

        return sVoid;
    }

    /**
     * Asynchronously count all occurrences of {@code searchWord} in
     * {@code rootFolder} using completable futures in conjunction
     * with a stream.
     */
    public static CompletableFuture<Void>
        searchFolders(Dirent rootFolder,
                      String searchWord) {
        // Create a single future that triggers when all the
        // futures in the stream complete.
        CompletableFuture<Stream<Long>> allDoneFuture = rootFolder
            // Create a stream for the folder.
            .stream()

            // Only search documents.
            .filter(Tests::isDocument)

            // Search the document asynchronously.
            .map(document -> CompletableFuture
                 .supplyAsync(() -> 
                              occurrencesCount(document.getContents(),
                                               searchWord)))

            // Trigger intermediate operation processing and return a
            // a single future that can be used to wait for all the
            // futures in the list to complete.
            .collect(toFuture());

        return allDoneFuture
            // Print matches when all async computations complete.
            .thenAccept(stream -> {
                    long matches = stream
                        // Map contents to Long.
                        .mapToLong(Long::longValue)

                        // Sum # of searchWord matches.
                        .sum();

                    // Print the results.
                    display("total matches of \""
                            + searchWord
                            + "\" = "
                            + matches);
                });
    }

    /**
     * Synchronously determine the # of times {@code searchWord}
     * appears in {@code document}.
     */
    public static Long occurrencesCount(CharSequence document,
                                        String searchWord) {
        // Return # of times searchWord appears in the stream.
        return 
            // Create a stream from the document around matches to
            // individual words.
            splitAsStream(document, "\\W+")

            // Only consider words that match.
            .filter(searchWord::equals)

            // Count the results.
            .count();
    }

    /**
     * Synchronously count the # of lines in the recursively
     * structured directory at {@code rootFolder}.
     */
    public static CompletableFuture<Void> countLines(Dirent rootFolder) {
        // Count # of lines in documents residing in rootFolder.
        long lineCount = rootFolder
            // Create a stream from the folder.
            .stream()

            // Only consider documents.
            .filter(Tests::isDocument)

            // Count # of lines in the document.
            .mapToLong(document ->
                       // Create a stream from the document around
                       // matches to newlines.
                       splitAsStream(document.getContents(),
                                     "\\r?\\n|\\r")

                       // Count the number of newlines in the document.
                       .count())

            // Sum the results;
            .sum();

        display("total number of lines = " + lineCount);

        return sVoid;
    }

    /**
     * Synchronously create a stream from the {@code document} around
     * matches of this {@code regex} pattern.
     *
     * @param document The document to be split
     * @param regex The regular expression to compile
     * @return The stream of strings computed by splitting the {@code
     *         document} around matches of this {@code regex} pattern
     */
    private static Stream<String> splitAsStream(CharSequence document,
                                                String regex) {
        // Return a stream from the document around matches of the
        // regex pattern.
        return Pattern
            // Compile the regex into a pattern.
            .compile(regex)

            // Split the document into a stream around matches of the
            // regex pattern.
            .splitAsStream(document);
    }

    /**
     * @return True if {@code dirent} is a document, else false
     */
    private static boolean isDocument(Dirent dirent) {
        // Return true if dirent is a document, else false.
        return dirent instanceof Document;
    }
}
