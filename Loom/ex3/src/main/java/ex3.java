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

        List.of(1, 2, 3, 4)
            .parallelStream() // .stream() - nothing changes
            .peek(m -> {
                    System.out.println("outer " + Thread.currentThread().getId());
                })
            .flatMap(x -> {
                Map<Long, Integer> threadMap = new ConcurrentHashMap<>();
                return Stream
                    .iterate(0, i -> i + 1)
                    .limit(x)
                    .parallel()
                    .peek(m -> {
                        var existing = threadMap
                            .put(Thread.currentThread().getId(),
                                 0);
                        if (existing == null) {
                            threadMap.put(Thread.currentThread().getId(),
                                          1);
                            System.out.println("first time in for thread "
                                               + Thread.currentThread().getId());
                        } else {
                            threadMap.put(Thread.currentThread().getId(),
                                          existing + 1);
                            System.out.println((existing + 1) + " time in for thread "
                                                   + Thread.currentThread().getId());
                        }
                    });
            })
            .collect(Collectors.toSet());

        /*
        // Runs the tests using Project Loom's structured concurrency model.
        runTest(StructuredConcurrencyTests::run,
                "Structured concurrency test");
                         */

        /*
        // Runs the tests using the Java completable futures framework.
        runTest(ex3::testDownloadCF,
        ex3::downloadImage,
        ex3::transformImageCF,
        ex3::storeImage);
        */

        // Print the results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving the download tests program");
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
