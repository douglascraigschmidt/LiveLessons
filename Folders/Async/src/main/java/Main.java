import folder.Dirent;
import folder.Document;
import folder.Folder;
import utils.Options;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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
     * Run all the tests.
     */
    private static void runTests() {
        // The word to search for while the folder's been constructed.
        final String searchedWord = "CompletableFuture";

        // Compute the time needed to create a new folder.
        CompletableFuture<Dirent> rootFolderFuture = 
            RunTimer.timeRun(() -> createFolder(),
                             "createFolder()");
        
        Stream
            // Call the of() factory method to create a stream of
            // CompletableFuture<Void>.  All the thenAccept() calls
            // below can run concurrently.
            .of(rootFolderFuture
                // This method is invoked when asynchronous folder
                // creation has completed.
                .thenCompose((Dirent rootFolder) -> CompletableFuture
                             // Run asynchronously.
                             .runAsync(() ->
                                       // Compute the time needed to count the
                                       // number of entries in the folder.
                                       RunTimer
                                       .timeRun(() -> countEntries(rootFolder),
                                                "countEntries()"))),
                rootFolderFuture
                // This method is invoked when asynchronous folder
                // creation has completed.
                .thenCompose((Dirent rootFolder) -> CompletableFuture
                             // Run asynchronously.
                             .runAsync(() ->
                                       // Compute the time needed to
                                       // count the number of lines in
                                       // the folder.
                                       RunTimer
                                       .timeRun(() -> countLines(rootFolder),
                                                "countLines()"))),

                rootFolderFuture
                // This method is invoked when asynchronous folder
                // creation has completed.
                .thenCompose((Dirent rootFolder) -> CompletableFuture
                             // Run asynchronously.
                             .runAsync(() ->
                                       // Compute time needed to
                                       // search for a word in all
                                       // folders starting at the
                                       // rootFolder.
                                       RunTimer
                                       .timeRun(() -> searchFolders(rootFolder,
                                                                    searchedWord),
                                                "searchFolders()"))))

            // Trigger intermediate operation processing and return a
            // a single future that can be used to wait for all the
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
                           .getRootFolderFile(sWORKs),
                           false);
    }

    /**
     * Count the number of entries in the folder.
     */
    private static void countEntries(Dirent folder) {
        long count = folder
            // Create a stream from the folder.
            .stream()

            // Count the number of elements in the stream.
            .count();

        display("number of entries in the folder = "
                + count);
    }

    /**
     * Asynchronously find all occurrences of @a searchedWord in @a
     * rootFolder using a stream.
     */
    private static void searchFolders(Dirent rootFolder,
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

        // Compute the total number of matches of searchedWord.
        long matches = allDoneFuture
            .join()
            .stream()
            .mapToLong(Long::longValue)
            .sum();

        // Print the results.
        System.out.println("total matches of \""
                           + searchedWord
                           + "\" = "
                           + matches);
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
    private static void countLines(Dirent rootFolder) {
        // Options.getInstance().setVerbose(true);

        // Count # of lines in documents residing in the folder.
        long lineCount = rootFolder
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
            .sum();

        System.out.println("total number of lines = "
                           + lineCount);
    }
}
