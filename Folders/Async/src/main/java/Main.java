import folder.Dirent;
import tests.Tests;
import utils.Options;
import utils.RunTimer;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static utils.Options.display;
import static utils.Options.sVoid;
import static utils.StreamOfFuturesCollector.toFuture;

/**
 * This example combines the Java completable futures and sequential
 * streams framework to process entries in a recursively-structured
 * folder hierarchy concurrently and asynchronously.
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

        // Run the tests asynchronously.
        runAsyncTests();

        // Run the tests synchronously.
        runSyncTests();

        // Print the results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Run the tests asynchronously.
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
            RunTimer.timeRun(() -> Tests.createFolder(sWORKS),
                             "async createFolder()");

        var asyncResults = Stream
            // The of() factory method creates a stream of
            // CompletableFuture objects based on async calls to the
            // (Bi)Function lambdas below, which all run concurrently
            // in the common fork-join pool.
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
     * A factory method that runs a {@link Function} asynchronously in
     * the common fork-join pool.
     *
     * @param rootFolderF A {@link CompletableFuture} to the
     *                    asynchronously created folder
     * @param func The {@link Function} to run in the common fork-join
     *             pool
     * @param funcName The name of the {@link Function} to run in the
     *                 common fork-join pool
     * @return A {@link CompletableFuture} that is triggered after the
     *         {@link Function} completes
     */
    private static CompletableFuture<Void> runFunctionAsync
        (CompletableFuture<Dirent> rootFolderF,
         Function<Dirent, CompletableFuture<Void>> func,
         String funcName) {
        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        // Return a CompletableFuture that is triggered after the
        // Function completes.
        return rootFolderF
            // This completion stage method is invoked when
            // rootFolderF completes and runs the (timed) action in
            // the common fork-join pool.
            .thenComposeAsync(rootFolder -> RunTimer
                              // Compute time needed to apply func on
                              // rootFolder in common fork-join pool.
                              .timeRun(() ->
                                       func.apply(rootFolder),
                                       funcName));
    }

    /**
     * A factory method that runs {@link BiFunction} asynchronously in
     * the common fork-join pool.
     *
     * @param rootFolderF A {@link CompletableFuture} to the
     *                    asynchronously created folder
     * @param biFunc The {@link BiFunction} to run in the common
     *               fork-join pool
     * @param param The parameter to pass to {@code BiFunction}
     * @param funcName The name of the {@link BiFunction} to run in
     *                 the common fork-join pool
     * @return A {@link CompletableFuture} that completes after the
     *         {@code BiFunction} completes
     */
    private static CompletableFuture<Void> runBiFunctionAsync
        (CompletableFuture<Dirent> rootFolderF,
         BiFunction<Dirent, String, CompletableFuture<Void>> biFunc,
         String param,
         String funcName) {
        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        // Return a CompletableFuture that completes after the
        // BiFunction completes.
        return rootFolderF
            // This Completion stage method is invoked when
            // rootFolderF completes and runs the action in the common
            // fork-join pool.
            .thenComposeAsync(rootFolder -> RunTimer
                              // Compute time needed to apply biFunc
                              // on rootFolder in fork-join pool.
                              .timeRun(() ->
                                       biFunc.apply(rootFolder,
                                                    param),
                                       funcName));
    }

    /**
     * Run the tests synchronously.
     */
    private static void runSyncTests() {
        display("Starting runSyncTests()");

        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        // Clever hack.. ;-)
        CompletableFuture<Dirent>[] cff = new CompletableFuture[1];

        RunTimer
            // Record the time needed to create a new folder.
            .timeRun(() -> (cff[0] = Tests.createFolder(sWORKS))
                                          .join(),
                     "sync createFolder()");

        // Run the following tests synchronously (by calling join())
        // and measure how long they take to execute.
        runFunctionAsync(cff[0],
                         folder -> {
                             Tests.countEntries(folder)
                                  .join();
                             return sVoid;
                         },
                         "sync countEntries()");

        runFunctionAsync(cff[0],
                         folder -> {
                             Tests.countLines(folder)
                                  .join();
                             return sVoid;
                         },
                         "sync countLines()");

        runBiFunctionAsync(cff[0],
                           (rootFolder, searchWord) -> {
                               Tests.searchFolders(rootFolder,
                                                   searchWord)
                                    .join();
                               return sVoid;
                           },
                           "CompletableFuture",
                           "sync searchFolders()");

        display("Ending runSyncTests()");
    }
}
