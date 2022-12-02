import common.Options;
import tests.CompletableFuturesTest;
import tests.HybridStructuredConcurrencyTest;
import tests.ParallelStreamsTest;
import tests.StructuredConcurrencyTest;
import utils.*;

import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * This example compares and contrasts the programming models and
 * performance results of Java parallel streams, completable futures,
 * and Java structured concurrency when applied to download,
 * transform, and store many images from a remote web server.
 */
public class ex4 {
    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv)
        throws ExecutionException, InterruptedException {
        System.out.println("Entering the program with "
                               + Runtime.getRuntime().availableProcessors()
                               + " cores available");

        // Initializes the Options singleton.
        Options.instance().parseArgs(argv);

        runTest(ParallelStreamsTest::run,
                "warmup common fork-join pool");

        // Runs the tests using the Java parallel streams framework.
        runTest(ParallelStreamsTest::run,
                "Parallel streams implementation");

        // Runs the tests using the Java completable futures framework.
        runTest(CompletableFuturesTest::run,
                "Completable futures implementation");

        // Runs the tests using the Java structured concurrency framework.
        runTest(StructuredConcurrencyTest::run,
                "Structured concurrency implementation");

        // Runs the tests using the Java structured concurrency framework
        // mixed with Java sequential streams.
        runTest(HybridStructuredConcurrencyTest::run,
                "Hybrid structured concurrency implementation");

        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Record the amount of time needed to run {@code runTest}.
     */
    private static void runTest(Consumer<String> test,
                                String testName) {
        // Let the system garbage collect first to ensure pristine conditions.
        System.gc();

        // Record how long the test takes to run runTest.
        // Run the test with the designated
        // functions.
        RunTimer.timeRun(() -> test.accept(testName),
                         testName);

        // Delete any images from the previous run.
        FileAndNetUtils
            .deleteDownloadedImages(Options.instance().getDirectoryPath());
    }
}
