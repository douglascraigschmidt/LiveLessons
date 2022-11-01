import folder.Dirent;
import tests.Tests;
import utils.Options;
import utils.RunTimer;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static tests.Tests.*;
import static utils.Options.display;
import static utils.Options.sVoid;
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
        var rootFolderF =
            RunTimer.timeRun(() -> createFolder(sWORKS),
                             "async createFolder()");

        var asyncResults = Stream
            // The of() factory method creates a stream of futures
            // based on async calls to the (bi)function lambdas below,
            // which run concurrently in the common fork-join pool.
            .of(runFunctionAsync(rootFolderF,
                                 Tests::countEntries,
                                 "async countEntries()"),
                runBiFunctionAsync(rootFolderF,
                                   Tests::searchFolders,
                                   searchWord,
                                   "async searchFolders()"),
                runFunctionAsync(rootFolderF,
                                 Tests::countLines,
                                 "async countLines()"))

            // Trigger intermediate operation processing and return a
            // single future that is used to wait for all futures in
            // the stream to complete.
            .collect(toFuture());

        // Wait for async processing to finish and then return.
        RunTimer.timeRun(asyncResults::join,
                         "asyncResults::join");

        display("Ending runAsyncTests()");
    }
    
    /**
     * A factory method that runs {@code func} asynchronously in the
     * common fork-join pool.
     *
     * @param rootFolderF A future to the asynchronously created folder
     * @param func The function to run in the fork-join pool
     * @param funcName The name of the function to run in the fork-join pool
     * @return A future that completes after {@code func} completes
     */
    private static CompletableFuture<Void> runFunctionAsync
        (CompletableFuture<Dirent> rootFolderF,
         Function<Dirent, CompletableFuture<Void>> func,
         String funcName) {
        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        // Return a future that completes after func completes.
        return rootFolderF
            // Completion stage method is invoked when rootFolderF
            // completes and runs action in the common fork-join pool.
            .thenComposeAsync(rootFolder ->
                              // Compute time needed to apply func on
                              // rootFolder in fork-join pool.
                              RunTimer.timeRun(() ->
                                               func.apply(rootFolder),
                                               funcName));
    }

    /**
     * A factory method that runs {@code biFunc} asynchronously in the
     * common fork-join pool.
     *
     * @param rootFolderF A future to the asynchronously created folder
     * @param biFunc The {@link BiFunction} to run in the fork-join pool
     * @param param The parameter to pass to {@code biFunc}
     * @param funcName The name of the function to run in the fork-join pool
     * @return A future that completes after {@code biFunc} completes
     */
    private static CompletableFuture<Void> runBiFunctionAsync
        (CompletableFuture<Dirent> rootFolderF,
         BiFunction<Dirent, String, CompletableFuture<Void>> biFunc,
         String param,
         String funcName) {
        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        // Return a future that completes after func completes.
        return rootFolderF
            // Completion stage method invoked when rootFolderF
            // completes and runs action in the common fork-join pool.
            .thenComposeAsync(rootFolder ->
                              // Compute time needed to apply biFunc
                              // on rootFolder in fork-join pool.
                              RunTimer.timeRun(() ->
                                               biFunc.apply(rootFolder,
                                                            param),
                                               funcName));
    }

    /**
     * Warmup the thread pool and run the sync tests.
     */
    private static void runSyncTests() {
        display("Starting runSyncTests()");
        
        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        // Clever hack.. ;-)
        CompletableFuture<Dirent>[] cff = new CompletableFuture[1];

        // Create a new folder.
        RunTimer.timeRun(() -> (cff[0] = createFolder(sWORKS)).join(),
                         "sync createFolder()");

        // Run/time all the following tests synchronously to measure
        // how long they take to execute.
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
