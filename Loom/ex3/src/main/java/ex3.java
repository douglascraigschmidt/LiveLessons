import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Random;

import utils.Options;
import utils.Image;
import utils.FileAndNetUtils;
import utils.RunTimer;
import utils.FuturesCollector;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * This example compares and contrasts the programming models and
 * performance results of Java parallel streams and Project Loom
 * structured concurrency when applied to download many images from a
 * remote web server.
 */
public class ex3 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex3.class.getName();

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv)
        throws ExecutionException, InterruptedException {
        // Initialize any command-line options.
        Options.instance().parseArgs(argv);

        // Run the test program.
        new ex3().run();
    }

    /**
     * Run the test program.
     */
    private void run() {
        System.out.println("Entering the download tests program with "
                           + Runtime.getRuntime().availableProcessors()
                           + " cores available");

        // Runs the tests using the Java parallel streams framework.
        runTest(this::testDownloadPS,
                this::downloadAndStoreImage,
                "testParallelStreamDownload()");

        // Runs the tests using Project Loom's structured concurrency model.
        runTest(this::testDownloadSC,
                this::downloadAndStoreImage,
                "testStructuredConcurrencyDownload()");

        // Runs the tests using the Java completable futures framework.
        runTest(this::testDownloadCF,
                this::downloadAndStoreImage,
                "testCompletableFuturesDownload()");

        // Print the results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving the download tests program");
    }

    /**
     * Run the {@code test} named {@code testName} by applying the
     * {@code downloadAndStoreImage} function.
     *
     * @param test A {@link BiFunction} that runs the test
     * @param downloadAndStoreImage A {@link Function} that downloads
     *                              and stores an image
     * @param testName The name of the test that's being run
     */
    private void runTest(BiFunction<Function<URL, File>, String, Void> test,
                         Function<URL, File> downloadAndStoreImage,
                         String testName) {
        // Let the system garbage collect to avoid perturbing results.
        System.gc();

        // Delete any images from the previous run.
        FileAndNetUtils.deleteDownloadedImages();

        // Record how long the test takes to run.
        RunTimer.timeRun(() ->
                         // Run the designated test on the
                         // downloadAndStoreImage function.
                         test.apply(downloadAndStoreImage,
                                    testName),
                         testName);
    }

    /**
     * This method uses Java parallel streams to run the tests via the
     * {@code downloadAndStoreImage} function.
     *
     * @param downloadAndStoreImage A {@link Function} that downloads
     *                              and stores an image
     * @param testName The name of the test that's being run
     */
    private Void testDownloadPS(Function<URL, File> downloadAndStoreImage,
                                String testName) {
        // Get the list of files to the downloaded images.
        List<File> imageFiles = Options.instance().getUrlList()
            // Convert the URLs in the input list into a stream and
            // process them in parallel.
            .parallelStream()

            // Transform URL to a File by downloading each image via
            // its URL.
            .map(downloadAndStoreImage)

            // Terminate the stream and collect the results into list
            // of images.
            .collect(Collectors.toList());

        // Print the statistics for this test run.
        printStats(testName, imageFiles.size());
        return null;
    }

    /**
     * This method uses Java completable futures to run the tests via
     * the {@code downloadAndStoreImage} function.
     *
     * @param downloadAndStoreImage A {@link Function} that downloads
     *                              and stores an image
     * @param testName The name of the test that's being run
     */
    private Void testDownloadCF(Function<URL, File> downloadAndStoreImage,
                                String testName) {
        // Get the list of files to the downloaded images.
        CompletableFuture<List<File>> imageFilesFuture = Options.instance()
            // Get the List of URLs.
            .getUrlList()

            // Convert the List to a stream.
            .stream()

            // Transform URL to a File by downloading each image via
            // its URL.
            .map(url -> CompletableFuture
                 // Download each image and store it in a file
                 // asychronously.
                 .supplyAsync(() ->
                              downloadAndStoreImage(url)))

            // Terminate the stream and collect the results into list
            // of images.
            .collect(FuturesCollector.toFuture());

        // Print the statistics for this test run.
        printStats(testName, imageFilesFuture.join().size());
        return null;
    }

    /**
     * This method uses Project Loom structure concurrency to run the
     * tests via the {@code downloadAndStoreImage} function.
     *
     * @param downloadAndStoreImage A {@link Function} that downloads
     *                              and stores an image
     * @param testName The name of the test that's being run
     */
    private Void testDownloadSC(Function<URL, File> downloadAndStoreImage,
                                String testName) {
        // A List of Future<File> objects that holds the results of
        // downloading images.
        List<Future<File>> imageFileFutures = new ArrayList<>();

        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            // Iterate through the List of image URLs.
            for (URL url : Options.instance().getUrlList()) 
                imageFileFutures
                    // Add each Future the Future<File> List.
                    .add(executor
                         // submit() starts a virtual thread to
                         // download each image.
                         .submit(() ->
                                 // Download each image via its URL
                                 // and store it in a File.
                                 downloadAndStoreImage(url)));

            // Scope doesn't exit until all concurrent tasks complete.
        } 

        // Print the statistics for this test run.
        printStats(testName, imageFileFutures.size());
        return null;
    }

    /**
     * Transform a {@link URL} to a {@link File} by downloading the
     * contents of the {@code url} and storing it on the local file
     * system.
     *
     * @param url The {@link URL} of the image to download
     * @return A {@link File} containing the image contents
     */
    private File downloadAndStoreImage(URL url) {
        return 
            // Perform a blocking image download.
            new Image(url,
                      FileAndNetUtils.downloadContent(url))

            // Store the image on the local device.
            .store();
    }

    /**
     * Display statistics about the given {@code testName}.
     */
    private void printStats(String testName, 
                            int imageCount) {
        System.out.println(TAG 
                           + ": "
                           + testName
                           + " downloaded and stored "
                           + imageCount
                           + " images using "
                           + (ForkJoinPool.commonPool().getPoolSize() + 1)
                           + " threads in the common fork-join pool");
    }
}
