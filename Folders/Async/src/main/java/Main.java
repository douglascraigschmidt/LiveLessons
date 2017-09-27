import folder.Dirent;
import folder.Document;
import folder.Folder;
import utils.Options;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static utils.FuturesCollector.toFuture;

/**
 * This example shows the use of the Java 8 completable futures
 * framework in conjunction with the Java 8 sequential streams
 * framework to process entries in a recursively structured directory
 * folder concurrently.
 */
public class Main {
    /**
     * The input "works".
     */
    private static final String sWORKs =
        "works";

    /**
     * Display @a string if the program is run in verbose mode.
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
     * A factory method that runs {@code func} asynchronously in the
     * fork-join pool.
     *
     * @param rootFolderFuture A future to the asynchronously created folder
     * @param func The function to run in the fork-join pool
     * @param funcName The name of the function to run in the fork-join pool
     * @return A CompletableFuture<Void> that will complete after {@code func} completes.
     */
    private static CompletableFuture<Void> runFunction(CompletableFuture<Dirent> rootFolderFuture,
                                                       Function<Dirent, Void> func,
                                                       String funcName) {
        return rootFolderFuture
            // This method is invoked when rootFolderFuture completes.
            .thenCompose((Dirent rootFolder) -> CompletableFuture
                         // Run asynchronously in the fork-join pool.
                         .runAsync(() ->
                                   // Compute time needed to apply
                                   // func on the rootFolder.
                                   RunTimer
                                   .timeRun(() -> func.apply(rootFolder),
                                            funcName)));
    }

    /**
     * A factory method that runs {@code func} asynchronously in the
     * fork-join pool.
     *
     * @param rootFolderFuture A future to the asynchronously created folder
     * @param biFunc The bifunction to run in the fork-join pool
     * @param param The parameter to pass to {@code biFunc}
     * @param funcName The name of the function to run in the fork-join pool
     * @return A CompletableFuture<Void> that will complete after {@code func} completes.
     */
    private static CompletableFuture<Void> runBiFunction(CompletableFuture<Dirent> rootFolderFuture,
                                                         BiFunction<Dirent, String, Void> biFunc,
                                                         String param,
                                                         String funcName) {
        return rootFolderFuture
            // This method is invoked when rootFolderFuture completes.
            .thenCompose((Dirent rootFolder) -> CompletableFuture
                         // Run asynchronously in the fork-join pool.
                         .runAsync(() ->
                                   // Compute time needed to apply
                                   // biFunc on the rootFolder.
                                   RunTimer
                                   .timeRun(() -> biFunc.apply(rootFolder,
                                                               param),
                                            funcName)));
    }

    /**
     * Run all the tests.
     */
    private static void runTests() {
        // The word to search for while the folder's been constructed.
        final String searchedWord = "CompletableFuture";

        // Compute the time needed to create a new folder.
        CompletableFuture<Dirent> rootFolderFuture = 
            RunTimer.timeRun(Main::createFolder,
                             "createFolder()");
        

        Stream
            // Call the of() factory method to create a stream of
            // CompletableFuture<Void> based on asynchronous calls to
            // the function lambdas below, which run concurrently.
            .of(runFunction(rootFolderFuture,
                            Main::countEntries,
                            "countEntries()"),
                runFunction(rootFolderFuture,
                            Main::countLines,
                            "countLines()"),
                runBiFunction(rootFolderFuture,
                              Main::searchFolders,
                              searchedWord,
                              "searchFolders()"))

            // Trigger intermediate operation processing and return a
            // a single future that can be used to wait for all
            // futures in the list to complete.
            .collect(toFuture())
        
            // Wait for everything to finish and return from main().
            .join();
    }

    /**
     * Create a new folder asynchronously.
     *
     * @return A future to the folder that completes when the folder
     *         is created
     */
    private static CompletableFuture<Dirent> createFolder() {
        // Asynchronously create and return a folder containing all
        // the works in the sWORKs directory.
        return Folder
            .fromDirectory(TestDataFactory
                           .getRootFolderFile(sWORKs));
    }

    /**
     * Count the number of entries in the folder.
     */
    private static Void countEntries(Dirent folder) {
        display("number of entries in the folder = "
                + folder
                // Create a stream from the folder.
                .stream()

                // Count the number of elements in the stream.
                .count());
        return null;
    }

    /**
     * Find all occurrences of @a searchedWord in @a rootFolder using
     * completable futures in cojunction with a stream.
     */
    private static Void searchFolders(Dirent rootFolder,
                                      String searchedWord) {
        // Create a single future that can be used to wait for all the
        // futures in the list to complete.
        CompletableFuture<List<Long>> allDoneFuture = rootFolder
            // Create a stream for the folder.
            .stream()

            // Only search documents.
            .filter(dirent
                    -> dirent instanceof Document)

            // Search the document asynchronously.
            .map(document -> CompletableFuture
                 .supplyAsync(() -> 
                              occurrencesCount(document.getContents(),
                                               searchedWord)))

            // Trigger intermediate operation processing and return a
            // a single future that can be used to wait for all the
            // futures in the list to complete.
            .collect(toFuture());

        // Print the results.
        display("total matches of \""
                + searchedWord
                + "\" = "
                + allDoneFuture
                // Wait for all computations to
                // complete.
                .join()

                // Create a stream of long results.
                .stream()

                // Map contents to Long.
                .mapToLong(Long::longValue)

                // Sum # of searchedWord matches.
                .sum());
        return null;
    }

    /**
     * Determine # of times @a searchedWord appears in @a document.
     */
    private static Long occurrencesCount(CharSequence document,
                                         String searchedWord) {
        // Return # of times searchedWord appears in the stream.
        return Pattern
            // Compile word splitter into a regular expression
            // (regex).
            .compile("\\W+")

            // Use the regex to split the file into a stream of
            // words.
            .splitAsStream(document)

            // Only consider words that match.
            .filter(searchedWord::equals)

            // Count the results.
            .count();
    }

    /**
     * Count # of lines in the recursively structured directory at @a
     * rootFolder.
     */
    private static Void countLines(Dirent rootFolder) {
        // Options.getInstance().setVerbose(true);

        // Count # of lines in documents residing in the folder.
        display("total number of lines = "
                + rootFolder
                // Create a stream from the folder.
                .stream()

                // Only consider documents. 
                .filter(dirent
                        -> dirent instanceof Document)

                // Count # of lines in the document.
                .mapToInt(document
                          -> document
                          // Get contents of document
                          .getContents().toString()

                          // Split document by newline.
                          .split("[\n\r]")
                      
                          // Return length of the result.
                          .length)

                // Sum the results;
                .sum());
        return null;
    }
}
