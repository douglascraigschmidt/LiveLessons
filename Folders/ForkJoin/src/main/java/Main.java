import utils.RunTimer;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.*;

/**
 * This example shows how to use various Java mechanisms (including
 * the Java fork-join pool framework and sequential/parallel streams
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

        // Run a test that uses the Java Files.walkFileTree() method,
        // Java 7 features, and the Visitor pattern to count the
        // files.
        runFileCounterWalkFileTree();

        // Run a test that uses the Java Files.walk() method and
        // a sequential stream to count the files.
        runFileCounterWalkStream();

        // Run a test that uses the Java fork-join framework in
        // conjunction with Java 7 features.
        runFileCounterTask();

        // Run a test that uses the Java fork-join framework in
        // conjunction with Java sequential streams features.
        runFileCounterStream();

        // Run a test that uses the Java fork-join framework in
        // conjunction with Java parallel streams features.
        runFileCounterParallelStream();

        // Get and print the timing results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending the file counter program");
    }

    /**
     * Run a test that uses the Java Files.walkFileTree() method, Java
     * 7 features, and the Visitor pattern to count the files.
     */
    private static void runFileCounterWalkFileTree() throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterWalkFileTree
                        (new File(ClassLoader.getSystemResource("works").toURI())),
                "FileCounterWalkFileTree",
                false);
    }

    /**
     * Run a test that uses the Java Files.walk() method and a
     * sequential stream to count the files.
     */
    private static void runFileCounterWalkStream() throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterWalkStream
                        (new File(ClassLoader.getSystemResource("works").toURI())),
                "FileCounterWalkStream",
                false);
    }

    /**
     * Run a test that uses the Java fork-join framework in
     * conjunction with Java 7 features.
     */
    private static void runFileCounterTask() throws URISyntaxException {
        runTest(new ForkJoinPool(),
                new FileCounterTask
                (new File(ClassLoader.getSystemResource("works").toURI())),
                "FileCounterTask",
                true);
    }

    /**
     * Run a test that uses the Java fork-join framework in
     * conjunction with Java sequential streams features.
     */
    private static void runFileCounterStream() throws URISyntaxException {
        runTest(new ForkJoinPool(),
                new FileCounterStream
                (new File(ClassLoader.getSystemResource("works").toURI())),
                "FileCounterStream",
                true);
    }

    /**
     * Run a test that uses the Java fork-join framework in
     * conjunction with Java parallel streams features.
     */
    private static void runFileCounterParallelStream() throws URISyntaxException {
        runTest(ForkJoinPool.commonPool(),
                new FileCounterParallelStream
                (new File(ClassLoader.getSystemResource("works").toURI())),
                "FileCounterParallelStream",
                true);
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
                                String testName,
                                boolean printStats) {
        // Run the GC first to avoid perturbing the tests.
        System.gc();

        // Run the task on the root of a large directory hierarchy.
        long size = RunTimer.timeRun(() -> fJPool.invoke(testTask),
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

        // Only print these results for certain tests.
        if (printStats)
            System.out.println("pool size = "
                               + fJPool.getPoolSize()
                               + ", steal count = "
                               + fJPool.getStealCount()
                               + ", running thread count = "
                               + fJPool.getRunningThreadCount());
    }
}

