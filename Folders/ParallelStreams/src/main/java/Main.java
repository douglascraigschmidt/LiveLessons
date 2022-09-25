import utils.RunTimer;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.*;

/**
 * This example shows how to use various Java mechanisms (including
 * the Java fork-join pool framework and sequential/parallel Streams
 * framework) to count the number of files in a (large) recursive
 * folder hierarchy, as well as calculate the cumulative sizes of all
 * the files.
 */
class Main {
    /**
     * Main entry point into the program runs the tests.
     */
    public static void main(String[] args) throws URISyntaxException {
        System.out.println("Starting the file counter program");

        warmUpThreadPool();

        // Run a test that uses the Java fork-join framework in
        // conjunction with Java parallel streams features.
        runFileCounterParallelStream();

        // Run a test that uses the Java fork-join framework in
        // conjunction with Java parallel streams features with
        // direct indexing.
        runFileCounterParallelStreamIndex();

        // Run a test that uses the Java fork-join framework in
        // conjunction with Java parallel streams features with
        // the teeing collector.
        runFileCounterParallelStreamTeeing();

        // Get and print the timing results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending the file counter program");
    }

    /**
     * Warmup the thread pool.
     */
    private static void warmUpThreadPool() throws URISyntaxException {
        runTest(new ForkJoinPool(),
                new FileCounterParallelStream
                (new File(ClassLoader.getSystemResource("works").toURI())),
                "WarmupThreadPool");
    }

    /**
     * Run a test that uses the Java fork-join framework in
     * conjunction with Java parallel streams features.
     */
    private static void runFileCounterParallelStream() throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterParallelStream
                (new File(ClassLoader.getSystemResource("works").toURI())),
                "FileCounterParallelStream");
    }

    /**
     * Run a test that uses the Java fork-join framework in
     * conjunction with Java parallel streams features and
     * direct indexing.
     */
    private static void runFileCounterParallelStreamIndex() throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterParallelStreamIndex
                        (new File(ClassLoader.getSystemResource("works").toURI())),
                "FileCounterParallelStreamIndex");
    }

    /**
     * Run a test that uses the Java fork-join framework in
     * conjunction with Java parallel streams features and
     * the teeing collector.
     */
    private static void runFileCounterParallelStreamTeeing() throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterParallelStreamTeeing
                        (new File(ClassLoader.getSystemResource("works").toURI())),
                "FileCounterParallelStreamTeeing");
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

        // Run the task on the root of a large directory hierarchy.
        long size = RunTimer.timeRun(testTask::compute,
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

