import tests.CompletableFuturesTests;
import tests.ParallelStreamsTests;
import tests.StructuredConcurrencyTests;
import utils.*;

import java.util.concurrent.*;

/**
 * This example compares and contrasts the programming models and
 * performance results of Java parallel streams, completable futures,
 * and Project Loom structured concurrency when applied to download,
 * transform, and store many images from a remote web server.
 */
public class ex3 {
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

        // Runs the tests using the Java parallel streams framework.
        runTest(ParallelStreamsTests::run,
                "Parallel streams run() test");

        // Runs the tests using the Java completable futures framework.
        runTest(CompletableFuturesTests::run,
                "Completable futures runFineGrained() test");

        // Runs the tests using the Java framework.
        runTest(StructuredConcurrencyTests::run,
                "Structured concurrency runFineGrained() test");

        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Record the amount of time needed to runFineGrained test.
     */
    private static void runTest(Runnable runTest,
                                String testName) {
        // Let the system garbage collect.
        System.gc();

        // Record how long the test takes to runFineGrained.
        // Run the test with the designated
        // functions.
        RunTimer.timeRun(runTest,
                         testName);

        // Delete any images from the previous runFineGrained.
        FileAndNetUtils
            .deleteDownloadedImages(Options.instance().getDirectoryPath());
    }
}
