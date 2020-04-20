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

        // Warmup the stream pool.
        warmupThreadPool();

        // Run all the tests.
        runTests();

        // Print the results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Warmup the thread pool.
     */
    private static void warmupThreadPool() {
        display("Warming up the thread pool"); 

        // Create a new folder.
        createFolder().join();
    }

    /**
     * Run all the tests.
     */
    private static void runTests() {
        // The word to search for after the folder's been constructed.
        final String searchWord = "CompletableFuture";

        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        // Compute the time needed to create a new folder.
        CompletableFuture<Dirent> rootFolderF =
            RunTimer.timeRun(Main::createFolder,
                             "createFolder()");

        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        Stream
            // Call the of() factory method to create a stream of
            // CompletableFuture<Void> based on asynchronous calls to
            // the function lambdas below, which run concurrently.
            .of(runFunction(rootFolderF,
                            Main::countEntries,
                            "countEntries()"),
                runFunction(rootFolderF,
                            Main::countLines,
                            "countLines()"),
                runBiFunction(rootFolderF,
                              Main::searchFolders,
                              searchWord,
                              "searchFolders()"))

            // Trigger intermediate operation processing and return a
            // a single future that can be used to wait for all
            // futures in the list to complete.
            .collect(toFuture())

            // Wait for everything to finish and return from main().
            .join();
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
    private static CompletableFuture<Void> runFunction
        (CompletableFuture<Dirent> rootFolderF,
         Function<Dirent, Void> func,
         String funcName) {
        return rootFolderF
            // Completion stage method invoked when rootFolderF
            // completes.
            .thenApplyAsync(rootFolder ->
                            // Compute time needed to apply biFunc on
                            // rootFolder in fork-join pool.
                            RunTimer.timeRun(() -> func.apply(rootFolder),
                                             funcName));
    }

    /**
     * A factory method that runs {@code func} asynchronously in the
     * fork-join pool.
     *
     * @param rootFolderF A future to the asynchronously created folder
     * @param biFunc The bifunction to run in the fork-join pool
     * @param param The parameter to pass to {@code biFunc}
     * @param funcName The name of the function to run in the fork-join pool
     * @return A CompletableFuture<Void> that will complete after
     *         {@code biFunc} completes.
     */
    private static CompletableFuture<Void> runBiFunction
        (CompletableFuture<Dirent> rootFolderF,
         BiFunction<Dirent, String, Void> biFunc,
         String param,
         String funcName) {
        return rootFolderF
            // Completion stage method invoked when rootFolderF
            // completes.
            .thenApplyAsync(rootFolder ->
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
    private static CompletableFuture<Dirent> createFolder() {
        // Asynchronously create and return a folder containing all
        // the works in the sWORKS directory.
        return Folder
            .fromDirectory(TestDataFactory.getRootFolderFile(sWORKS));
    }

    /**
     * Count the number of entries in the folder.
     */
    private static Void countEntries(Dirent folder) {
        long folderCount = folder
            // Create a stream from the folder.
            .stream()

            // Count the number of elements in the stream.
            .count();

        display("number of entries in the folder = "
                + folderCount);
        return null;
    }

    /**
     * Find all occurrences of {@code searchWord} in {@code
     * rootFolder} using completable futures in cojunction with a
     * stream.
     */
    private static Void searchFolders(Dirent rootFolder,
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

        allDoneFuture
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
        return null;
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
    private static Void countLines(Dirent rootFolder) {
        // Count # of lines in documents residing in rootFolder.
        display("total number of lines = "
                + rootFolder
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
                .sum());
        return null;
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
}
