import common.Options;
import tests.*;
import utils.*;

import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * This example compares and contrasts the programming models and
 * performance results of Java parallel streams, completable futures,
 * Project Reactor, and Java structured concurrency when applied to
 * download, transform, and store many images from a remote web
 * server.
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

        // Warmup the "carrier" threads.
        runTest(StructuredConcurrencyTest::run,
                "warmup 'carrier' threads");

        // Runs the tests using the Java structured concurrency framework.
        runTest(StructuredConcurrencyTest::run,
                "Structured concurrency implementation");

        // Warmup the "carrier" threads.
        runTest(HybridStructuredConcurrencyTest::run,
                "warmup 'carrier' threads");

        // Runs the tests using the Java structured concurrency framework
        // mixed with Java sequential streams.
        runTest(HybridStructuredConcurrencyTest::run,
                "Hybrid structured concurrency implementation");

        // Warmup the common fork-join pool.
        runTest(ParallelStreamsTest::run,
                "warmup common fork-join pool");

        // Runs the tests using the Java parallel streams framework.
        runTest(ParallelStreamsTest::run,
                "Parallel streams implementation");

        // Runs the tests using the Java completable futures framework.
        runTest(CompletableFuturesTest::run,
                "Completable futures implementation");

        // Warmup the boundedElastic pool in the Project Reactor framework.
        runTest(ProjectReactorTest::run,
                "warmup Project Reactor boundedElastic poo");

        // Runs the tests using the Project Reactor framework.
        runTest(ProjectReactorTest::run,
                "Project Reactor implementation");

        // Warmup the io pool in the RxJava framework.
        runTest(RxJavaTest::run,
                "warmup RxJava io pool");

        // Runs the tests using the RxJava framework.
        runTest(RxJavaTest::run,
                "RxJava implementation");

        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Record the amount of time needed to run {@code runTest}.
     */
    private static void runTest(Consumer<String> test,
                                String testName) {
        // Let the system garbage collect first to ensure pristine conditions.
        System.gc();

        if (!testName.contains("warmup"))
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
