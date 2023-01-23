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
 * recursively-structured directory folder sequentially and
 * concurrently.
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

        Options.getInstance().display("Starting the test " + mode);

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        // Get a single to a folder.
        Single<Dirent> rootFolderS = RunTimer
            // Compute the time needed to create a new folder
            // asynchronously.
            .timeRun(() -> createFolder(sWORKS, parallel),
                             "createFolder() " + mode);

        RunTimer
            // Compute the time taken to count the entries in the folder.
            .timeRun(() -> countEntries(rootFolderS,
                                        parallel),
                         "countEntries() " + mode);

        RunTimer
            // Compute the time taken to count the # of lines in the
            // folder.
            .timeRun(() -> countLines(rootFolderS,
                                      parallel),
                         "countLines() " + mode);

        RunTimer
            // Compute the time taken to synchronously search for a
            // word in all folders starting at the rootFolder.
            .timeRun(() -> searchFolders(rootFolderS,
                                         searchWord,
                                         parallel),
                         "searchFolders() " + mode);

        Options.getInstance().display("Ending the test " + mode);
    }

    /**
     * Asynchronously create an in-memory folder containing all the works.
     *
     * @param works Name of the directory in the file system containing the works.
     * @param parallel Flag indicating whether to run the tests in parallel or not
     * @return A single to a folder containing all works in {@code works}
     */
    private static Single<Dirent> createFolder(String works,
                                               boolean parallel) {
        // Return a single to the initialized folder.
        return Folder
            // Asynchronously create a folder with all
            // works in the root directory.
            .fromDirectory(TestDataFactory.getRootFolderFile(works),
                           parallel)
            
            // Cache the results so that they won't be re-emitted
            // repeatedly each time.
            .cache();
    }

    /**
     * Count the # of entries in the {@code rootFolder}.
     *
     * @param rootFolderS A single to an in-memory folder containing the works.
     * @param parallel Flag indicating whether to run the tests in parallel or not
     */
    private static void countEntries(Single<Dirent> rootFolderS,
                                     boolean parallel) {
        rootFolderS
            // This code is called after the rootFolder has completed
            // its initialization.
            .flatMap(rootFolder -> RxUtils
                     // Create a stream of dirents at rootFolder
                     // either concurrently or sequentially.
                     .fromIterableConcurrentIf(rootFolder, parallel)

                     // Count the # of elements in the stream.
                     .count())

            // Block until result is available and then display it.
            .blockingSubscribe(count -> Options.getInstance().
                               display("number of entries in the folder = "
                                       + count));
    }

    /**
     * Count # of lines in the recursively structured directory at
     * {@code rootFolder}.
     *
     * @param rootFolderS A single to an in-memory folder containing the works.
     * @param parallel Flag indicating whether to run the tests in parallel or not
     */
    private static void countLines(Single<Dirent> rootFolderS,
                                   boolean parallel) {
        rootFolderS
            // This code is called after the rootFolder has completed
            // its initialization.
            .flatMap(rootFolder -> Observable
                     // Create a stream of dirents from rootFolder.
                     .fromIterable(rootFolder)

                     // Use the RxJava flatMap() idiom to count the #
                     // of lines in the folder.
                     .flatMap(dirent -> RxUtils
                              // Emit direct concurrently or sequentially.
                              .justConcurrentIf(dirent, parallel)

                              // Only search documents.
                              .filter(Main::isDocument)

                              // Split the document into lines.
                              .flatMap(document -> splitAsObservable(document,
                                                                     "\\r?\\n|\\r"))

                              // Count the number of newlines in the document.
                              .count()

                              // Convert the single to an observable.
                              .toObservable())

                     // Sum all the counts.
                     .reduce(Long::sum)

                     // Return 0 if empty.
                     .defaultIfEmpty(0L))

            // Block until result is available and then display it.
            .blockingSubscribe(lineCount -> Options.getInstance().
                               display("total number of lines = "
                                       + lineCount));
    }

    /**
     * Find all occurrences of {@code searchWord} in {@code
     * rootFolder} using a stream.
     *
     * @param rootFolderS A single in-memory folder containing the works
     * @param searchWord Word to search for in the folder
     * @param parallel Flag indicating whether to run the tests in parallel or not
     */
    private static void searchFolders(Single<Dirent> rootFolderS,
                                      String searchWord,
                                      boolean parallel) {
        rootFolderS
            // This code is called after the rootFolder has completed
            // its initialization.
            .flatMap(rootFolder -> Observable
                     // Create a stream of dirents from rootFolder.
                     .fromIterable(rootFolder)

                     // Use the RxJava flatMap() idiom to count the #
                     // of times searchWord appears in the folder.
                     .flatMap(dirent -> RxUtils
                             // Emit direct concurrently or sequentially.
                             .justConcurrentIf(dirent, parallel)

                             // Conditionally convert to run
                              // concurrently.
                              .compose(RxUtils.concurrentObservableIf(parallel))

                              // Only search documents.
                              .filter(Main::isDocument)

                              // Search document looking for matches.
                              .flatMap(document ->
                                       // Count # of times searchWord
                                       // appears in the document.
                                       occurrencesCount(document,
                                                        searchWord)))

                     // Sum all the counts.
                     .reduce(Long::sum)

                     // Return 0 if empty.
                     .defaultIfEmpty(0L))

            // Block until result is available and then display it.
            .blockingSubscribe(wordMatches -> Options.getInstance().
                               display("total matches of \""
                                       + searchWord
                                       + "\" = "
                                       + wordMatches));
    }

    /**
     * Count the number of times {@code searchWord} appears in the
     * {@code document}.
     *
     * @param document In-memory document containing text
     * @param searchWord Word to search for in the document
     * @return The # of times {@code searchWord} appears in {@code document}.
     */
    private static Observable<Long> occurrencesCount(Dirent document,
                                                     String searchWord) {
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
        // Return an observable of strings.
        return Observable
            // Create an observable from an array of strings.
            .fromArray(document
                       // Create a string containing the document's contents.
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
