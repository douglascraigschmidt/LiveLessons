package tests;

import reactor.core.publisher.Flux;
import utils.FileUtils;
import utils.Options;
import utils.ReactorUtils;
import utils.RunTimer;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * This Java utility class contains static methods show how to
 * download many images from a remote web server via the Reactor
 * framework.
 */
public final class ReactorTests {
    /**
     * Utility class constructors should be private.
     */
    private ReactorTests() {}

    /**
     * Run the test named {@code testName} by appying the {@code
     * downloadAndStoreImage} function with the Reactor driver that
     * uses its flatMap() mechanism to download files in parallel.
     */
    public static void runFlatMap(Function<URL, File> downloadAndStoreImage,
                                  String testName) {
        // First let the system garbage collect.
        System.gc();

        // Delete any filtered images from the previous run.
        FileUtils.deleteDownloadedImages();

        RunTimer
            // Record how long the test takes to run.
            .timeRun(() ->
                     // Run the test with the designated function.
                     testDownloadFlatMap(downloadAndStoreImage,
                                         testName),
                     testName);
    }

    /**
     * Run the test named {@code testName} by appying the {@code
     * downloadAndStoreImage} function with the Reactor driver that
     * uses its ParallelFlowable mechanism to download files in
     * parallel.
     */
    public static void runParallelFlux
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
                     testDownloadParallelFlux(downloadAndStoreImage,
                                              testName),
                     testName);
    }

    /**
     * This method runs the {@code downloadAndStoreImage} function
     * using the Reactor framework and its flatMap() idiom for
     * parallelizing downloads.
     */
    private static void testDownloadFlatMap
        (Function<URL, File> downloadAndStoreImage,
         String testName) {
        Function<URL, Flux<File>> downloadAndStore = url -> ReactorUtils
            // Just omit this url and run it concurrently in the
            // common fork-join pool.
            .justConcurrentIf(url, true)

            // Transform each url to a file by downloading the image.
            .map(downloadAndStoreImage);

        Flux
            // Convert the URLs in the input list into a stream.
            .fromIterable(Options.instance().getUrlList())

            // Apply the RxJava flatMap() idiom to process each url
            // concurrently.
            .flatMap(downloadAndStore)

            // Collect the downloaded images into a list.
            .collectList()

            // Process the list.
            .doOnSuccess(imageFiles -> Options.instance()
                         // Print the # of image files that were downloaded.
                         .printStats(testName, imageFiles.size()))

            // Block until the processing is finished.
            .block();
    }

    /**
     * This method runs the {@code downloadAndStoreImage} function
     * using the Reactor framework and its ParallelFlux mechanism for
     * parallelizing downloads.
     */
    private static void testDownloadParallelFlux
        (Function<URL, File> downloadAndStoreImage,
         String testName) {

        ReactorUtils
            // Convert the URLs in the input list into a parallel flux
            // stream.
            .fromIterableParallel(Options.instance().getUrlList())

            // Transform each URL to a file by calling the
            // downloadAndStoreImage function, which downloads each
            // image via its URL.
            .map(downloadAndStoreImage)

            // Convert the parallel flux back to flux.
            .sequential()

            // Collect to a list.
            .collectList()

            // Process the list.
            .doOnSuccess(imageFiles -> Options.instance()
                        // Print the # of image files that were downloaded.
                        .printStats(testName, imageFiles.size()))

            // Block until the processing is finished.
            .block();
    }
}
