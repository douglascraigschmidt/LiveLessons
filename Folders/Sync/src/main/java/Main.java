import folder.Dirent;
import folder.Document;
import folder.Folder;
import utils.Options;
import utils.StreamsUtils;
import utils.TestDataFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * This example shows the use of the Java 8 streams to process entries
 * in a recursively structured directory folder sequentially or in
 * parallel.
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
    static public void main(String[] argv)
            throws IOException, URISyntaxException {
        // Parse the options.
        Options.getInstance().parseArgs(argv);

        warmupThreadPool();
        runTests(false);
        runTests(true);
    }

    /**
     * Warmup the thread pool.
     */
    private static void warmupThreadPool() throws IOException, URISyntaxException {
        // Create a new folder.
        createFolder(true);
    }

    /**
     * Run all the tests, either sequentially or in parallel,
     * depending on the value of @a parallel.
     */
    private static void runTests(boolean parallel)
        throws IOException, URISyntaxException {
        // The word to search for while the folder's been constructed.
        final String searchedWord = "CompletableFuture";

        // Start the timer.
        final long startTime = System.nanoTime();

        // Create a new folder.
        Dirent rootFolder = createFolder(parallel);

        // Print the timing results.
        System.out.println((parallel ? "Parallel" : "Sequential")
                           + " folder creation ran in "
                           + (System.nanoTime() - startTime) / 1_000_000
                           + " milliseconds");

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
    private static Dirent createFolder(boolean parallel)
        throws IOException, URISyntaxException {
        // Create and return a folder containing all the works in the
        // sWORKs directory.
        return Folder
            .fromDirectory(TestDataFactory
                           .getRootFolderFile(sWORKs),
                           parallel);
    }

    /**
     * Count the number of entries in the folder.
     */
    private static void countEntries(Dirent folder,
                                     boolean parallel) {
        // Start the timer.
        long startTime = System.nanoTime();

        // Create a stream from the folder.
        Stream<Dirent> folderStream = folder
            .stream();

        // Convert to the parallel stream if desired.
        if (parallel)
            folderStream.parallel();

        // Get a count of the number of elements in the stream.
        long count = folderStream.count();

        System.out.println("number of entries in the folder = "
                           + count);

        // Print the timing results.
        System.out.println((parallel
                            ? "Parallel" : "Sequential")
                           + " entry counting ran in "
                           + (System.nanoTime() - startTime) / 1_000_000
                           + " milliseconds");
    }

    /**
     * Synchronously find all occurrences of @a searchedWord in @a
     * rootFolder using a stream.
     */
    private static void searchFolders(Dirent rootFolder,
                                      String searchedWord,
                                      boolean parallel) {
        // Start the timer.
        long startTime = System.nanoTime();

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
        System.out.println("total matches of \""
                           + searchedWord
                           + "\" = "
                           + matches);

        // Print the timing results.
        System.out.println((parallel ? "Parallel" : "Sequential")
                           + " searchFoldersSync ran in "
                           + (System.nanoTime() - startTime) / 1_000_000
                           + " milliseconds");
    }
    /**
     * Determine # of times @a searchedWord appears in @a document.
     */
    private static Long occurrencesCount(CharSequence document,
                                         String searchedWord,
                                         boolean parallel) {
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
        // Start the timer.
        long startTime = System.nanoTime();

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

        System.out.println("total number of lines = "
                           + lineCount);

        // Print the timing results.
        System.out.println((parallel ? "Parallel" : "Sequential")
                           + " line count ran in "
                           + (System.nanoTime() - startTime) / 1_000_000
                           + " milliseconds");
    }
}
