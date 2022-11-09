import folder.Dirent;
import tests.Tests;
import utils.Options;
import utils.RunTimer;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static tests.TestDriverAsync.runAsyncTests;
import static tests.TestDriverSync.runSyncTests;
import static utils.Options.display;
import static utils.Options.sVoid;
import static utils.StreamOfFuturesCollector.toFuture;

/**
 * This example combines the Java completable futures and sequential
 * streams frameworks to process entries in a recursively-structured
 * directory hierarchy concurrently, asynchronously, and
 * synchronously.
 */
public class Main {
    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        // Parse the options.
        Options.getInstance().parseArgs(argv);

        // Warmup the cache.
        runAsyncTests();

        // Run the tests synchronously.
        runSyncTests();

        // Run the tests asynchronously.
        runAsyncTests();

        // Print the results.
        System.out.println(RunTimer.getTimingResults());
    }

}
