package tests;

import folder.Dirent;
import utils.RunTimer;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static utils.Options.display;
import static utils.Options.sWORKS;
import static utils.StreamOfFuturesCollector.toFuture;

public class TestDriverAsync {
    /**
     * Run the tests asynchronously.
     */
    public static void runAsyncTests() {
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
}
