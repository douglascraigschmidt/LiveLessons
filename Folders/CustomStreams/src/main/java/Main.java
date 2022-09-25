import folder.Dirent;
import folder.Document;
import folder.Folder;
import utils.Options;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * This example shows the use of the Java streams framework to process
 * entries in a recursively-structured directory folder using custom
 * spliterators and collectors for Java sequential and parallel
 * streams.
 */
@SuppressWarnings("ALL")
public class Main {
    /**
     * The input "works" to process, which is a large recursive folder
     * containing thousands of subfolders and files.
     */
    private static final String sWORKS = "works";

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        // Parse the options.
        Options.getInstance().parseArgs(argv);

        // Warmup the thread pool.
        warmupThreadPool();

        // Run all the tests sequentially.
        runTests(false);

        // Run all the tests in parallel.
        runTests(true);

        // Print the results sorted by decreasing order of efficiency.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Warmup the thread pool.
     */
    private static void warmupThreadPool() {
        display("Warming up the thread pool");

        // Create a new folder.
        createFolder(sWORKS, true);
    }

    /**
     * Run all the tests, either sequentially or in parallel,
     * depending on the value of @a parallel.
     */
    private static void runTests(boolean parallel) {
        // Record whether we're running in parallel or sequentially.
        String mode = parallel ? "in parallel" : "sequentially";

        display("Starting the test " + mode);

        // The word to search for while the folder's being constructed.
        final String searchWord = "CompletableFuture";

        // Compute the time needed to create a new folder.
        Dirent rootFolder = 
            RunTimer.timeRun(() -> createFolder(sWORKS, parallel),
                             "createFolder() " + mode);

        // Compute the time taken to count the entries in the folder.
        RunTimer.timeRun(() -> countEntries(rootFolder, 
                                            parallel),
                         "countEntries() " + mode);

        // Compute the time taken to count the # of lines in the
        // folder.
        RunTimer.timeRun(() -> countLines(rootFolder,
                                          parallel),
                         "countLines() in " + mode);

        // Compute the time taken to synchronously search for a word
        // in all folders starting at the rootFolder.
        RunTimer.timeRun(() -> searchFolders(rootFolder,
                                             searchWord,
                                             parallel),
                         "searchFolders() in " + mode);

        display("Ending the test " + mode);
    }

    /**
     * @return A new folder containing all the works in the {@code works} folder
     */
    private static Dirent createFolder(String works,
                                       boolean parallel) {
        // Return the initialized folder.
        return Folder
            // Create a folder containing all the works in the root
            // directory.
            .fromDirectory(TestDataFactory.getRootFolderFile(works),
                           parallel);
    }

    /**
     * Count the # of entries in the {@code rootFolder}.
     */
    private static void countEntries(Dirent rootFolder,
                                     boolean parallel) {
        // Create a stream of dirents starting at the rootFolder,
        // which triggers the use of our *FolderSpliterator.
        Stream<Dirent> folderStream = rootFolder
            .stream();

        // Conditionally convert to a parallel stream.
        if (parallel)
            folderStream.parallel();

        // Get a count of the number of elements in the stream.
        long count = folderStream.count();

        display("number of entries in the folder = "
                + count);
    }

    /**
     * Synchronously find all occurrences of {@code searchWord} in {@code
     * rootFolder} using a stream.
     */
    private static void searchFolders(Dirent rootFolder,
                                      String searchWord,
                                      boolean parallel) {
        // Create a stream of dirents starting at the rootFolder,
        // which triggers the use of our *FolderSpliterator.
        Stream<Dirent> folderStream = rootFolder
            .stream();

        // Conditionally convert to a parallel stream.
        if (parallel)
            folderStream.parallel();

        // Compute the total number of matches of searchWord.
        long matches = folderStream
            // Only search documents.
            .filter(Main::isDocument)

            // Search the document synchronously.
            .mapToLong(document ->
                       // Count # of times searchWord appears in the document.
                       occurrencesCount(document.getContents(),
                                        searchWord,
                                        parallel))
            // Sum the results.
            .sum();

        // Print the results.
        display("total matches of \""
                + searchWord
                + "\" = "
                + matches);
    }

    /**
     * @return The # of times {@code searchWord} appears in {@code document}.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static Long occurrencesCount(CharSequence document,
                                         String searchWord,
                                         boolean parallel) {
        // Determine the # of times searchWord appears in a
        // document.
        Stream<String> wordStream =
            // Create a stream from the document around matches to
            // individual words.
            splitAsStream(document, "\\W+");

        // Conditionally convert to a parallel stream.
        if (parallel)
            wordStream.parallel();

        // Return # of times searchWord appears in the stream.
        return wordStream
            // Only consider words that match.
            .filter(searchWord::equals)

            // Count # of times searchWord appears in the stream.
            .count();
    }

    /**
     * Count # of lines in the recursively structured directory at @a
     * rootFolder.
     */
    private static void countLines(Dirent rootFolder, 
                                   boolean parallel) {
        // Create a stream of dirents starting at the rootFolder,
        // which triggers the use of our *FolderSpliterator.
        Stream<Dirent> folderStream = rootFolder
            .stream();

        // Conditionally convert to a parallel stream.
        if (parallel)
            folderStream.parallel();

        // Count # of lines in documents residing in the folder.
        long lineCount = folderStream
            // Only consider documents. 
            .filter(Main::isDocument)

            // Count # of lines in the document.
            .mapToLong(document -> 
                       // Create a stream from the document around
                       // matches to newlines.
                       splitAsStream(document.getContents(),
                                     "\\r?\\n")

                       // Count the number of newlines in the document.
                       .count())

            // Sum the results of all newlines in all the documents.
            .sum();

        display("total number of lines = "
                + lineCount);
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
     * @return True if {@code dirent} is a document, else false
     */
    private static boolean isDocument(Dirent dirent) {
        // Return true if dirent is a document, else false.
        return dirent instanceof Document;
    }

    /**
     * Display {@code string} if the program is run in verbose mode.
     */
    static void display(String string) {
        if (Options.getInstance().getVerbose())
            System.out.println("["
                    + Thread.currentThread().getId()
                    + "] "
                    + string);
    }
}
