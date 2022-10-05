import filecounters.AbstractFileCounter;
import filecounters.FileCountStream;
import filecounters.FileCountStreamIndexing;
import filecounters.FileCountStreamTeeing;
import utils.RunTimer;

import java.io.File;
import java.net.URISyntaxException;

/**
 * This example shows how to use various Java parallel Streams
 * framework features to count the number of files in a (large)
 * recursive folder hierarchy, as well as calculate the cumulative
 * sizes of all the files.
 */
class Main {
    /**
     * Main entry point into the program runs the tests.
     */
    public static void main(String[] args) throws URISyntaxException {
        System.out.println("Starting the file counter program");

        warmUpThreadPool();

        // Run a test that uses Java sequential streams features as a
        // baseline for the parallel streams tests.
        runFileCounterStream(false);

        // Run a test that uses Java sequential streams features with
        // direct indexing.
        runFileCounterStreamIndex(false);

        // Run a test that uses Java sequential streams features with
        // the teeing collector.
        runFileCounterStreamTeeing(false);

        // Run a test that uses Java parallel streams features.
        runFileCounterStream(true);

        // Run a test that uses Java parallel streams features with
        // direct indexing.
        runFileCounterStreamIndex(true);

        // Run a test that uses Java parallel streams features with
        // the teeing collector.
        runFileCounterStreamTeeing(true);

        // Get and print the timing results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending the file counter program");
    }

    /**
     * Warmup the thread pool.
     */
    private static void warmUpThreadPool() throws URISyntaxException {
        runTest(new FileCountStream
                (new File(ClassLoader.getSystemResource("works").toURI()),
                 true),
                "warmup");
    }

    /**
     * Run a test that uses either Java sequential or parallel streams
     * and the Java ternary operator.
     */
    private static void runFileCounterStream(boolean parallel)
        throws URISyntaxException {
        runTest(new FileCountStream
                (new File(ClassLoader.getSystemResource("works")
                          .toURI()),
                 parallel),
                "FileCounterStream"
                + (parallel ? " (parallel)" : " (sequential)"));
    }

    /**
     * Run a test that uses either Java sequential and parallel
     * streams features and direct indexing.
     */
    private static void runFileCounterStreamIndex(boolean parallel)
        throws URISyntaxException {
        runTest(new FileCountStreamIndexing
                (new File(ClassLoader.getSystemResource("works")
                          .toURI()),
                 parallel),
                "FileCounterStreamIndex"
                + (parallel ? " (parallel)" : " (sequential)"));
    }

    /**
     * Run a test that uses either Java sequential or parallel streams
     * features and the teeing collector.
     */
    private static void runFileCounterStreamTeeing(boolean parallel)
        throws URISyntaxException {
        runTest(new FileCountStreamTeeing
                (new File(ClassLoader.getSystemResource("works")
                          .toURI()),
                 parallel),
                "FileCounterStreamTeeing"
                + (parallel ? " (parallel)" : " (sequential)"));
    }

    /**
     * Run all the tests and collect/print the results.
     *
     * @param testTask The file counter task to run
     * @param testName The name of the test
     */
    private static void runTest(AbstractFileCounter testTask,
                                String testName) {
        // Run the GC first to avoid perturbing the tests.
        System.gc();

        if (testName.equals("warmup")) {
            testTask.compute();
            return;
        }

        // Run the task on the root of a large directory hierarchy.
        long size = RunTimer.timeRun(testTask::compute,
                                     testName);

        // Print the results.
        System.out.println(testName
                           + ": "
                           + (AbstractFileCounter.documentCount()
                              + AbstractFileCounter.folderCount())
                           + " files ("
                           + AbstractFileCounter.documentCount()
                           + " documents and " 
                           + AbstractFileCounter.folderCount()
                           + " folders) contained "
                           + size / 1_000_000
                           + " megabytes");
    }
}

