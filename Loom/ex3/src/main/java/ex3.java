import tests.ParallelStreamsTests;
import tests.StructuredConcurrencyTests;
import transforms.Transform;
import utils.*;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This example compares and contrasts the programming models and
 * performance results of Java parallel streams, completable futures,
 * and Project Loom structured concurrency when applied to download
 * many images from a remote web server.
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

        /*

        // Runs the tests using the Java parallel streams framework.
        runTest(ParallelStreamsTests::runFlatMap,
                "warmup");

        // Runs the tests using the Java parallel streams framework.
        runTest(ParallelStreamsTests::runReduceConcat,
                "Parallel streams runReduceConcat() test");

        // Runs the tests using the Java parallel streams framework.
        runTest(ParallelStreamsTests::runFlatMap,
                "Parallel streams runFlatMap() test");

         */
    }

    /**
     * Record the amount of time needed to run test.
     */
    private static void runTest(Runnable runTest,
                                String testName) {
        // Let the system garbage collect.
        System.gc();

        // Record how long the test takes to run.
        // Run the test with the designated
        // functions.
        RunTimer.timeRun(runTest,
                         testName);

        // Delete any images from the previous run.
        FileAndNetUtils
            .deleteDownloadedImages(Options.instance().getDirectoryPath());
    }
}
