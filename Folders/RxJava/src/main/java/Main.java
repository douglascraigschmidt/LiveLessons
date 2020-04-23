import folder.Dirent;
import folder.Document;
import folder.Folder;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import utils.Options;
import utils.RunTimer;
import utils.RxUtils;
import utils.TestDataFactory;

/**
 * This example shows the use of RxJava to process entries in a
 * recursively-structured directory folder sequentially and in
 * parallel.
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
    static public void main(String[] argv) {
        System.out.println("Starting RxJavaFolders test");

        // Parse the options.
        Options.getInstance().parseArgs(argv);

        // Run all the tests sequentially.
        runTests(false);

        // Run all the tests in parallel.
        runTests(true);

        // Print the results sorted by decreasing order of efficiency.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending RxJavaFolders test");
    }

    /**
     * Run all the tests, either sequentially or in parallel,
     * depending on the value of {@code parallel}.
     */
    private static void runTests(boolean parallel) {
        // Record whether we're running in parallel or sequentially.
        String mode = parallel ? "in parallel" : "sequentially";

        display("Starting the test " + mode);

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        // Compute the time needed to create a new folder.
        Dirent rootFolder =
            RunTimer.timeRun(() -> createFolder(sWORKS, parallel).blockingGet(),
                             "createFolder() " + mode);

        // Compute the time taken to count the entries in the folder.
        RunTimer.timeRun(() -> countEntries(rootFolder,
                                            parallel),
                         "countEntries() " + mode);

        // Compute the time taken to count the # of lines in the
        // folder.
        RunTimer.timeRun(() -> countLines(rootFolder,
                                          parallel),
                         "countLines() " + mode);

        // Compute the time taken to synchronously search for a word
        // in all folders starting at the rootFolder.
        RunTimer.timeRun(() -> searchFolders(rootFolder,
                                             searchWord,
                                             parallel),
                         "searchFolders() " + mode);

        display("Ending the test " + mode);
    }

    /**
     * Asynchronously create an in-memory folder containing all the works.
     *
     * @param works Name of the directory in the file system containing the works.
     * @param parallel Flag indicating whether to run the tests in parallel or not
     * @return A folder containing all works in {@code works}
     */
    private static Single<Dirent> createFolder(String works,
                                               boolean parallel) {
        // Return a single to the initialized folder.
        return Folder
            // Asynchronously create a folder containing all the works
            // in the root directory.
            .fromDirectory(TestDataFactory.getRootFolderFile(works),
                           parallel);
    }

    /**
     * Count the # of entries in the {@code rootFolder}.
     *
     * @param rootFolder In-memory folder containing the works.
     * @param parallel Flag indicating whether to run the tests in parallel or not
     */
    private static void countEntries(Dirent rootFolder,
                                     boolean parallel) {
        // Count the number of entries in the rootFolder.
        long count = Observable
            // Create a stream of dirents starting at the rootFolder.
            .fromIterable(rootFolder)

            // Conditionally convert to run concurrently.
            .compose(RxUtils.concurrentObservableIf(parallel))

            // Count the # of elements in the stream.
            .count()

            // Block until the processing is done.
            .blockingGet();

        // Print the results.
        display("number of entries in the folder = "
                + count);
    }

    /**
     * Count # of lines in the recursively structured directory at
     * {@code rootFolder}.
     *
     * @param rootFolder In-memory folder containing the works.
     * @param parallel Flag indicating whether to run the tests in parallel or not
     */
    private static void countLines(Dirent rootFolder,
                                   boolean parallel) {
        long lineCount = Observable
            // Create a stream of dirents starting at the rootFolder,
            .fromIterable(rootFolder)

            // Use the RxJava flatMap() idiom to count the # of lines
            // in the folder.
            .flatMap(dirent -> Observable
                     // Just omit this one dirent.
                     .just(dirent)

                     // Conditionally convert to run concurrently.
                     .compose(RxUtils.concurrentObservableIf(parallel))

                     // Only search documents.
                     .filter(Main::isDocument)

                     // Split the document into lines.
                     .flatMap(document -> splitAsObservable(document,
                                                            "\\r?\\n"))

                     // Count the number of newlines in the document.
                     .count()

                     // Convert the single to an observable.
                     .toObservable())

            // Sum all the counts.
            .reduce(Long::sum)

            // Return 0 if empty.
            .defaultIfEmpty(0L)

            // Block until complete.
            .blockingGet();

        // Print the results.
        display("total number of lines = "
                + lineCount);
    }

    /**
     * Find all occurrences of {@code searchWord} in {@code
     * rootFolder} using a stream.
     *
     * @param rootFolder In-memory folder containing the works
     * @param searchWord Word to search for in the folder
     * @param parallel Flag indicating whether to run the tests in parallel or not
     */
    private static void searchFolders(Dirent rootFolder,
                                      String searchWord,
                                      boolean parallel) {
        // Count the # of times searchWord appears in the folder.
        long matches = Observable
            // Create a stream of dirents starting at the rootFolder.
            .fromIterable(rootFolder)

            // Use the RxJava flatMap() idiom to count the # of times
            // searchWord appears in the folder.
            .flatMap(dirent -> Observable
                     // Just omit this one dirent.
                     .just(dirent)

                     // Conditionally convert to run concurrently.
                     .compose(RxUtils.concurrentObservableIf(parallel))

                     // Only search documents.
                     .filter(Main::isDocument)

                     // Search the document looking for matches.
                     .flatMap(document ->
                              // Count # of times searchWord appears
                              // in the document.
                              occurrencesCount(document,
                                               searchWord,
                                               parallel)))

            // Sum all the counts.
            .reduce(Long::sum)

            // Return 0 if empty.
            .defaultIfEmpty(0L)

            // Block until the result is available.
            .blockingGet();

        // Print the results.
        display("total matches of \""
                + searchWord
                + "\" = "
                + matches);
    }

    /**
     * Count the number of times {@code searchWord} appears in the
     * {@code document}.
     *
     * @param document In-memory document containing text
     * @param searchWord Word to search for in the document
     * @param parallel Flag indicating whether to run the tests in parallel or not
     * @return The # of times {@code searchWord} appears in {@code document}.
     */
    private static Observable<Long> occurrencesCount(Dirent document,
                                                     String searchWord,
                                                     boolean parallel) {
        // Return an observable that counts the # of times searchWord
        // appears in the document.
        return 
            // Split the document into a stream of observable words.
            splitAsObservable(document,
                                 "\\W+")

            // Only consider words that match.
            .filter(searchWord::equals)

            // Count # of times searchWord appears in the stream.
            .count()

            // Convert the single to an observable.
            .toObservable();
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
    private static Observable<String> splitAsObservable(Dirent document,
                                                        String regex) {
        return Observable
            .fromArray(document.getContents().toString()
                       .split(regex));
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
