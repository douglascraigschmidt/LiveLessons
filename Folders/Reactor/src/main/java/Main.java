import folder.Dirent;
import folder.Document;
import folder.Folder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import utils.Options;
import utils.ReactorUtils;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This example shows the use of the Reactor framework to process
 * entries in a recursively-structured directory folder sequentially
 * and in parallel.
 */
public class Main {
    /**
     * The input "works" to process, which is a large recursive folder
     * containing thousands of subfolders and files.
     */
    private static final String sWORKS = "works";

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) throws InterruptedException {
        System.out.println("Starting ReactorFolders test");

        // Parse the options.
        Options.getInstance().parseArgs(argv);

        // Run all the tests sequentially.
        runTests(false);

        // Run all the tests concurrently.
        runTests(true);

        // Print the results sorted by decreasing order of efficiency.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending ReactorFolders test");
    }

    /**
     * Run all the tests, either sequentially or concurrently,
     * depending on the value of {@code concurrent}.
     */
    private static void runTests(boolean concurrent) throws InterruptedException {
        // Record whether we're running concurrently or sequentially.
        String mode = concurrent ? "concurrently" : "sequentially";

        Options.getInstance().display("Starting the test " + mode);

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        // Get a mono to a Folder.
        Mono<Dirent> rootFolderM = RunTimer
            // Compute the time needed to create a new folder
            // asynchronously.
            .timeRun(() -> createFolder(sWORKS, concurrent),
                     "createFolder() " + mode);

        RunTimer
            // Compute the time taken to synchronously search for a
            // word in all folders starting at the rootFolder.
            .timeRun(() -> searchFolders(rootFolderM,
                                         searchWord,
                                         concurrent),
                     "searchFolders() " + mode);

        RunTimer
            // Compute the time taken to count the entries in the
            // folder.
            .timeRun(() -> countEntries(rootFolderM,
                                        concurrent),
                     "countEntries() " + mode);

        RunTimer
            // Compute the time taken to count the # of lines in the
            // folder.
            .timeRun(() -> countLines(rootFolderM,
                                      concurrent),
                     "countLines() " + mode);

        Options.getInstance().display("Ending the test " + mode);
    }

    /**
     * Asynchronously create an in-memory folder containing all the
     * works.
     *
     * @param works Name of the directory in the file system containing the works.
     * @param concurrently Flag indicating whether to run the tests concurrently or not
     * @return A mono to a folder containing all works in {@code works}
     */
    private static Mono<Dirent> createFolder(String works,
                                             boolean concurrently) {
        // Return a mono to the initialized folder.
        return Folder
            // Asynchronously create a folder with all works in the
            // root directory.
            .fromDirectory(TestDataFactory.getRootFolderFile(works),
                           concurrently)

            // Cache the results so that they won't be re-emitted
            // repeatedly each time.
            .cache();
    }

    /**
     * Count the # of entries in the {@code rootFolder}.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works.
     * @param concurrently Flag indicating whether to run the tests concurrently or not
     */
    private static void countEntries(Mono<Dirent> rootFolderM,
                                     boolean concurrently) {
        long count = rootFolderM
            // This code is called after the rootFolder has completed
            // its initialization.
            .flatMap(rootFolder -> ReactorUtils
                     // Create a stream of dirents that run either
                     // concurrently or sequentially.
                     .fromIterableConcurrentIf(rootFolder, concurrently)

                     // Count the number of dirents in the stream.
                     .count())

            // Block until result is available.
            .block();

        // Display the result.
        Options.getInstance().display("number of entries in the folder = "
                                      + count);
    }

    /**
     * Count # of lines in the recursively structured directory at
     * {@code rootFolder}.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works.
     * @param concurrently Flag indicating whether to run the tests concurrently or not
     */
    private static void countLines(Mono<Dirent> rootFolderM,
                                   boolean concurrently) {
        long lineCount = rootFolderM
            // This code is called after the rootFolder has completed
            // its initialization.
            .flatMap(rootFolder -> Flux
                     // Create a stream of dirents from rootFolder.
                     .fromIterable(rootFolder)

                     // Use the Reactor flatMap() idiom to count the #
                     // of lines in the folder.
                     .flatMap(dirent -> ReactorUtils
                              // Emit direct concurrently or sequentially.
                              .justConcurrentIf(dirent, concurrently)

                              // Only search documents.
                              .filter(Main::isDocument)

                              // Split the document into lines.
                              .flatMap(document -> splitAsFlux(document,
                                                               "\\r?\\n"))

                              // Count the number of newlines in the document.
                              .count())

                     // Sum all the counts.
                     .reduce(Long::sum)

                     // Return 0 if empty.
                     .defaultIfEmpty(0L))

            // Block until result is available
            .block();

        // Display the result.
        Options.getInstance().display("total number of lines = " + lineCount);
    }

    /**
     * Find all occurrences of {@code searchWord} in {@code
     * rootFolder} using a stream.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works
     * @param searchWord Word to search for in the folder
     * @param concurrently Flag indicating whether to run the tests concurrently or not
     */
    private static void searchFolders(Mono<Dirent> rootFolderM,
                                      String searchWord,
                                      boolean concurrently) {
        long wordMatches = rootFolderM
            // This code is called after the rootFolder has completed
            // its initialization.
            .flatMap(rootFolder ->
                     Flux
                     // Create a stream of dirents from rootFolder.
                     .fromIterable(rootFolder)

                     // Use the Reactor flatMap() idiom to count the #
                     // of times searchWord appears in the folder.
                     .flatMap(dirent -> ReactorUtils
                              // Emit direct concurrently or sequentially.
                              .justConcurrentIf(dirent, concurrently)

                              // Only search documents.
                              .filter(Main::isDocument)

                              // Search document looking for matches.
                              .flatMap(document ->
                                       // Count # of times searchWord
                                       // appears in the document.
                                       occurrencesCount(document,
                                                        searchWord,
                                                        concurrently)))

                     // Sum all the counts.
                     .reduce(Long::sum)

                     // Return 0 if empty.
                     .defaultIfEmpty(0L))

            // Block until result is available.
            .block();

        // Display the result.
        Options.getInstance().display("total matches of \""
                                      + searchWord
                                      + "\" = "
                                      + wordMatches);
    }

    /**
     * Count the number of times {@code searchWord} appears in the
     * {@code document}.
     *
     * @param document In-memory document containing text
     * @param searchWord Word to search for in the document
     * @param concurrently Flag indicating whether to run the tests concurrently or not
     * @return The # of times {@code searchWord} appears in {@code document}.
     */
    private static Mono<Long> occurrencesCount(Dirent document,
                                               String searchWord,
                                               boolean concurrently) {
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
    private static Flux<String> splitAsFlux(Dirent document,
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
    private static boolean isDocument(Dirent dirent) {
        // Return true if dirent is a document, else false.
        return dirent instanceof Document;
    }
}
