import counters.AbstractFileCounter;
import counters.FileCounterParallelStream;
import counters.FileCounterSequentialStreamTask;
import counters.FileCounterForkJoinTask;
import utils.RunTimer;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.*;

/**
 * This example shows how to use various Java mechanisms (including
 * the Java fork-join framework and streams framework) to count the
 * number of files in a (large) recursive folder hierarchy, as well as
 * calculate the cumulative sizes of all the files.
 */
class Main {
    /**
     * Main entry point into the program runs the tests.
     */
    public static void main(String[] args)
        throws URISyntaxException {
        System.out.println("Starting the file counter program");

        // Warmup the thread pool.
        warmupThreadPool1();

        // Run a test that uses the Java fork-join framework in
        // conjunction with Java parallel streams features.
        runFileCounterParallelStream();

        // warmup the thread pool.
        warmupThreadPool2();

        // Run a test that uses the Java fork-join framework in
        // conjunction with Java 7 features.
        runFileCounterTask();

        // Run a test that uses the Java fork-join framework in
        // conjunction with Java sequential streams features.
        runFileCounterSequentialStreamTask();

        // Get and print the timing results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending the file counter program");
    }

    /**
     * Run a test that uses the Java fork-join framework in
     * conjunction with Java 7 features.
     */
    private static void runFileCounterTask()
        throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterForkJoinTask
                (new File(ClassLoader.getSystemResource("works")
                          .toURI())),
                "ForkJoinTask");
    }

    /**
     * Run a test that uses the Java fork-join framework in
     * conjunction with Java sequential streams features.
     */
    private static void runFileCounterSequentialStreamTask()
        throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterSequentialStreamTask
                (new File(ClassLoader.getSystemResource("works")
                          .toURI())),
                "SequentialStreamTask");
    }

    /**
     * Run a test that uses the Java fork-join framework in
     * conjunction with Java parallel streams features.
     */
    private static void runFileCounterParallelStream()
        throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterParallelStream
                (new File(ClassLoader.getSystemResource("works")
                          .toURI())),
                "ParallelStream");
    }

    /**
     * Warmup the thread pool for parallel stream test.
     */
    private static void warmupThreadPool1()
        throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterParallelStream
                (new File(ClassLoader.getSystemResource("works")
                          .toURI())),
                "warmup");
    }

    /**
     * Warmup the thread pool for the fork-join test.
     */
    private static void warmupThreadPool2()
        throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterForkJoinTask
                (new File(ClassLoader.getSystemResource("works")
                          .toURI())),
                "warmup");
    }

    /**
     * Run all the tests and collect/print the results.
     *
     * @param fJPool The fork-join pool to use for the test
     * @param testTask The file counter task to run
     * @param testName The name of the test
     */
    private static void runTest(ForkJoinPool fJPool,
                                AbstractFileCounter testTask,
                                String testName) {
        // Run the GC first to avoid perturbing the tests.
        System.gc();

        if (testName.equals("warmup")) {
            fJPool.invoke(testTask);
            return;
        }

        // Run the task on the root of a large directory hierarchy.
        long size = RunTimer
            .timeRun(() -> fJPool.invoke(testTask),
                     testName);

        // Print the results.
        System.out.println(testName
                           + ": "
                           + (testTask.documentCount()
                              + testTask.folderCount())
                           + " files ("
                           + testTask.documentCount()
                           + " documents and " 
                           + testTask.folderCount()
                           + " folders) contained "
                           + size // / 1_000_000)
                           + " bytes");
    }
}

