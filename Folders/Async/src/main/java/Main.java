import folder.Dirent;
import folder.Document;
import folder.Folder;
import utils.Options;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static utils.StreamOfFuturesCollector.toFuture;

/**
 * This example combines the Java completable futures framework with
 * the Java sequential streams framework to process entries in a
 * recursively-structured folder hierarchy concurrently.
 */
public class Main {
    /**
     * The input "works", which is a large recursive folder containing
     * thousands of subfolders and files.
     */
    private static final String sWORKS = "works";

    /**
     * A completed completable future to nothing.
     */
    private static CompletableFuture<Void> sVoid
            = CompletableFuture.completedFuture(null);

    /**
     * Display {@code string} if the program is run in verbose mode.
     */
    private static void display(String string) {
        if (Options.getInstance().getVerbose())
            System.out.println(string);
    }

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        // Parse the options.
        Options.getInstance().parseArgs(argv);

        // Warmup the thread pool and run the sync tests.
        runSyncTests();

        // Run the async tests.
        runAsyncTests();

        // Print the results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Run the async tests.
     */
    private static void runAsyncTests() {
        display("Starting runAsyncTests()");

        // The word to search for after the folder's been constructed.
        final String searchWord = "CompletableFuture";

        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        // Compute the time needed to initiate the creation of a new
        // folder, which of course will be very low since the caller
        // doesn't block waiting for the future to complete..
        CompletableFuture<Dirent> rootFolderF =
            RunTimer.timeRun(() -> Main.createFolder(sWORKS),
                             "async createFolder()");

        final CompletableFuture<Stream<Void>> collect = Stream
                // The of() factory method creates a stream of futures
                // based on async calls to the (bi)function lambdas below,
                // which run concurrently in the common fork-join pool.
                .of(runFunctionAsync(rootFolderF,
                        Main::countEntries,
                        "async countEntries()"),
                        runBiFunctionAsync(rootFolderF,
                                Main::searchFolders,
                                searchWord,
                                "async searchFolders()"),
                        runFunctionAsync(rootFolderF,
                                Main::countLines,
                                "async countLines()"))

                // Trigger intermediate operation processing and return a
                // a single future that is used to wait for all futures in
                // the stream to complete.
                .collect(toFuture());

        // Wait for async processing to finish and then return.
        RunTimer.timeRun(collect::join,
                         "collect::join");

        display("Ending runAsyncTests()");
    }
    
    /**
     * A factory method that runs {@code func} asynchronously in the
     * common fork-join pool.
     *
     * @param rootFolderF A future to the asynchronously created folder
     * @param func The function to run in the fork-join pool
     * @param funcName The name of the function to run in the fork-join pool
     * @return A CompletableFuture<Void> that will complete after {@code func} completes.
     */
    private static CompletableFuture<Void> runFunctionAsync
        (CompletableFuture<Dirent> rootFolderF,
         Function<Dirent, CompletableFuture<Void>> func,
         String funcName) {
        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        return rootFolderF
            // Completion stage method is invoked when rootFolderF
            // completes and runs action in the common fork-join pool.
            .thenComposeAsync(rootFolder ->
                            // Compute time needed to apply biFunc on
                            // rootFolder in fork-join pool.
                            RunTimer.timeRun(() -> func.apply(rootFolder),
                                             funcName));
    }

    /**
     * A factory method that runs {@code func} asynchronously in the
     * common fork-join pool.
     *
     * @param rootFolderF A future to the asynchronously created folder
     * @param biFunc The bifunction to run in the fork-join pool
     * @param param The parameter to pass to {@code biFunc}
     * @param funcName The name of the function to run in the fork-join pool
     * @return A CompletableFuture<Void> that will complete after
     *         {@code biFunc} completes.
     */
    private static CompletableFuture<Void> runBiFunctionAsync
        (CompletableFuture<Dirent> rootFolderF,
         BiFunction<Dirent, String, CompletableFuture<Void>> biFunc,
         String param,
         String funcName) {
        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        return rootFolderF
            // Completion stage method invoked when rootFolderF
            // completes and runs action in the common fork-join pool.
            .thenComposeAsync(rootFolder ->
                            // Compute time needed to apply biFunc on
                            // rootFolder in fork-join pool.
                            RunTimer.timeRun(() -> biFunc.apply(rootFolder,
                                                                param),
                                             funcName));
    }

    /**
     * Create a new folder asynchronously.
     *
     * @return A future to the folder that completes when the folder
     *         is created
     */
    private static CompletableFuture<Dirent> createFolder(String root) {
        // Asynchronously create and return a folder containing all
        // the works in the root directory.
        return Folder
            .fromDirectory(TestDataFactory.getRootFolderFile(root));
    }

    /**
     * Count the number of entries in the folder.
     */
    private static CompletableFuture<Void> countEntries(Dirent folder) {
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
     * Find all occurrences of {@code searchWord} in {@code
     * rootFolder} using completable futures in cojunction with a
     * stream.
     */
    private static CompletableFuture<Void>
        searchFolders(Dirent rootFolder,
                      String searchWord) {
        // Create a single future that triggers when all the
        // futures in the stream complete.
        CompletableFuture<Stream<Long>> allDoneFuture = rootFolder
            // Create a stream for the folder.
            .stream()

            // Only search documents.
            .filter(Main::isDocument)

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
     * Determine # of times {@code searchWord} appears in {@code
     * document}.
     */
    private static Long occurrencesCount(CharSequence document,
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
     * Count # of lines in the recursively structured directory at
     * {@code rootFolder}.
     */
    private static CompletableFuture<Void> countLines(Dirent rootFolder) {
        // Count # of lines in documents residing in rootFolder.
        long lineCount = rootFolder
                // Create a stream from the folder.
                .stream()

                // Only consider documents.
                .filter(Main::isDocument)

                // Count # of lines in the document.
                .mapToLong(document ->
                        // Create a stream from the document around
                        // matches to newlines.
                        splitAsStream(document.getContents(),
                                "[\n\r]")

                                // Count the number of newlines in the document.
                                .count())

                // Sum the results;
                .sum();

        display("total number of lines = " + lineCount);

        return sVoid;
    }

    /**
     * Creates a stream from the {@code document} around matches of
     * this {@code regex} pattern.
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
     * @return True of {@code dirent} is a document, else false
     */
    private static boolean isDocument(Dirent dirent) {
        return dirent instanceof Document;
    }

    /**
     * Warmup the thread pool and run the sync tests.
     */
    private static void runSyncTests() {
        display("Starting runSyncTests()");
        
        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        CompletableFuture<Dirent>[] cff = new CompletableFuture[1];

        // Create a new folder.
        RunTimer.timeRun(() -> (cff[0] = createFolder(sWORKS)).join(),
                         "sync createFolder()");

        // Run/time all the following tests synchronously.
        runFunctionAsync(cff[0],
                         folder -> {
                             countEntries(folder).join();
                             return sVoid;
                         },
                         "sync countEntries()");

        runFunctionAsync(cff[0],
                         folder -> { countLines(folder).join();
                             return sVoid;
                         },
                         "sync countLines()");

        runBiFunctionAsync(cff[0],
                           (rootFolder, searchWord) -> {
                               searchFolders(rootFolder, searchWord).join();
                               return sVoid;
                           },
                           "CompletableFuture",
                           "sync searchFolders()");

        display("Ending runSyncTests()");
    }
}
