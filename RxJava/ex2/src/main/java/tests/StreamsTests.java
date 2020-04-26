package tests;

import utils.FileUtils;
import utils.Options;
import utils.RunTimer;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * This Java utility class contains static methods show how to
 * download many images from a remote web server via the Java parallel
 * streams framework.  It also compares the performance of the Java
 * parallel streams framework with and without the {@code
 * ForkJoinPool.ManagedBlocker} interface and the Java common
 * fork-join pool.
 */
public class StreamsTests {
    /**
     * Utility class constructors should be private.
     */
    private StreamsTests() {}

    /**
     * Run the test named {@code testName} by appying the {@code
     * downloadAndStoreImage} function using the parallel streams
     * driver.
     */
    public static void runParallelStreams
        (Function<URL, File> downloadAndStoreImage,
         String testName) {
        // First let the system garbage collect.
        System.gc();

        // Delete any filtered images from the previous run.
        FileUtils.deleteDownloadedImages();

        RunTimer
            // Record how long the test takes to run.
            .timeRun(() ->
                     // Run the test with the designated function.
                     testDownloadStreams(downloadAndStoreImage,
                                                 testName),
                     testName);
    }

    /**
     * This method runs the {@code downloadAndStoreImage} function
     * using the parallel streams framework.
     */
    public static void testDownloadStreams
        (Function<URL, File> downloadAndStoreImage,
         String testName) {
        // Get a list of files to the downloaded images.
        List<File> imageFiles = Options.instance().getUrlList()
            // Convert the URLs in the input list into a stream and
            // process them in parallel.
            .parallelStream()

            // Transform each URL to a File by calling the
            // downloadAndStoreImage function, which downloads each
            // image via its URL.
            .map(downloadAndStoreImage)

            // Terminate the stream and collect the results into list
            // of images.
            .collect(toList());

        // Print the statistics for this test run.
        Options.instance().printStats(testName, imageFiles.size());
    }
}
