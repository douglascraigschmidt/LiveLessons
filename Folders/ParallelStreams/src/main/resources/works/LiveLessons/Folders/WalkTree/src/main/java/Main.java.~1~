import utils.RunTimer;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.*;

/**
 * This example shows how to use various Java directory traversal
 * mechanisms to count the number of files in a (large) recursive
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
        // a Java sequential stream to count the files.
        runFileCounterWalkSequentialStream();

        // Get and print the timing results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending the file counter program");
    }

    /**
     * Run a test that uses the Java Files.walkFileTree() method, Java
     * 7 features, and the Visitor pattern to count the files.
     */
    private static void runFileCounterWalkFileTree() throws URISyntaxException {
        runTest(new FileCounterWalkFileTree
                (new File(ClassLoader.getSystemResource("works").toURI())),
                "FileCounterWalkFileTree");
    }

    /**
     * Run a test that uses the Java Files.walk() method and a
     * sequential stream to count the files.
     */
    private static void runFileCounterWalkSequentialStream() throws URISyntaxException {
        runTest(new FileCounterWalkSequentialStream
                (new File(ClassLoader.getSystemResource("works").toURI())),
                "FileCounterWalkStream");
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

