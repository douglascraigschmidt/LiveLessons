import folder.Dirent;
import folder.Document;
import folder.Folder;
import utils.Options;
import utils.RunTimer;
import utils.StreamsUtils;
import utils.TestDataFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * This example shows the use of the Java 8 streams to process entries
 * in a recursively structured directory folder concurrently using the
 * Java 8 completable futures framework.
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
    static void display(String string) {
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

        // Run the tests sequentially.
        runTests(false);

        // Run the tests in parallel.
        runTests(true);

        // Print the results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Warmup the thread pool.
     */
    private static void warmupThreadPool() {
        // Create a new folder.
        System.out.println("Warming up the thread pool");
        createFolder(true);
    }

    /**
     * Run all the tests, either sequentially or in parallel,
     * depending on the value of @a parallel.
     */
    private static void runTests(boolean parallel) {
        // The word to search for while the folder's been constructed.
        final String searchedWord = "CompletableFuture";

        // Create a new folder.
        Dirent rootFolder = createFolder(parallel);

        // Count the number of entries in the folder.
        countEntries(rootFolder, 
                     parallel);

        // Count the number of lines in the folder.
        countLines(rootFolder,
                   parallel);

        // Synchronously search for a word in all folders starting at
        // the rootFolder.
        searchFolders(rootFolder,
                      searchedWord,
                      parallel);
    }

    /**
     * Create a new folder.
     *
     * @return An open folder
     */
    private static Dirent createFolder(boolean parallel) {
        // Count the time needed to create and return a folder
        // containing all the works in the sWORKs directory.
        return RunTimer.timeRun(() ->
                                Folder
                                .fromDirectory(TestDataFactory
                                               .getRootFolderFile(sWORKs),
                                               parallel),
                                "createFolder() in "
                                + (parallel ? "Parallel" : "Sequential"));
    }

    /**
     * Count the number of entries in the folder.
     */
    private static void countEntries(Dirent folder,
                                     boolean parallel) {
        // Compute the time taken to count the entries.
        RunTimer.timeRun(() -> {
                // Create a stream from the folder.
                Stream<Dirent> folderStream = folder
                    .stream();

                // Convert to the parallel stream if desired.
                if (parallel)
                    folderStream.parallel();

                // Get a count of the number of elements in the stream.
                long count = folderStream.count();

                display("number of entries in the folder = "
                        + count);
            },
            "countEntries() in "
            + (parallel ? "Parallel" : "Sequential"));
    }

    /**
     * Synchronously find all occurrences of @a searchedWord in @a
     * rootFolder using a stream.
     */
    private static void searchFolders(Dirent rootFolder,
                                      String searchedWord,
                                      boolean parallel) {
        // Compute the time taken to search the folders.
        RunTimer.timeRun(() -> {
                // Create a stream for the folder.
                Stream<Dirent> folderStream = rootFolder
                    .stream();

                // Convert to the parallel stream if desired.
                if (parallel)
                    folderStream.parallel();

                // Compute the total number of matches of searchedWord.
                long matches = folderStream
                    // Only search documents.
                    .filter(dirent
                            -> dirent instanceof Document)

                    // Search the document synchronously.
                    .mapToLong(document
                               -> occurrencesCount(document.getContents(),
                                                   searchedWord,
                                                   parallel))
                    // Sum the results.
                    .sum();

                // Print the results.
                display("total matches of \""
                        + searchedWord
                        + "\" = "
                        + matches);
            },
            "searchFolders() in "
            + (parallel ? "Parallel" : "Sequential"));
    }

    /**
     * Determine # of times @a searchedWord appears in @a document.
     */
    private static Long occurrencesCount(CharSequence document,
                                         String searchedWord,
                                         boolean parallel) {
        // Determine the # of times searchedWord appears in a
        // document.
        Stream<String> wordStream = Pattern
            // Compile word splitter into a regular expression
            // (regex).
            .compile("\\W+")

            // Use the regex to split the file into a stream of
            // words.
            .splitAsStream(document);

        // Convert to the parallel stream if desired.
        if (parallel)
            wordStream.parallel();

        // Return # of times searchedWord appears in the stream.
        return wordStream
            // Only consider words that match.
            .filter(searchedWord::equals)

            // Count the results.
            .count();
    }

    /**
     * Count # of lines in the recursively structured directory at @a
     * rootFolder.
     */
    private static void countLines(Dirent rootFolder, 
                                   boolean parallel) {
        // Compute the time taken to count the # of lines in the
        // directory.
        RunTimer.timeRun(() -> {
                // Options.getInstance().setVerbose(true);

                // Create a stream for the folder.
                Stream<Dirent> folderStream = rootFolder
                    .stream();

                // Convert to the parallel stream if desired.
                if (parallel)
                    folderStream.parallel();

                // Count # of lines in documents residing in the folder.
                long lineCount = folderStream
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

                display("total number of lines = "
                       + lineCount);
            },
            "countLines() in "
            + (parallel ? "Parallel" : "Sequential"));
    }
}
