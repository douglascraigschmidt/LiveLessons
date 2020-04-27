package tests;

import io.reactivex.rxjava3.core.Observable;
import utils.FileUtils;
import utils.Options;
import utils.RunTimer;
import utils.RxUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This Java utility class contains static methods show how to
 * download many images from a remote web server via the RxJava
 * framework.
 */
public final class RxJavaTests {
    /**
     * Utility class constructors should be private.
     */
    private RxJavaTests() {}

    /**
     * Run the test named {@code testName} by appying the {@code
     * downloadAndStoreImage} function with the RxJava driver that
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
     * downloadAndStoreImage} function with the RxJava driver that
     * uses its ParallelFlowable mechanism to download files in
     * parallel.
     */
    public static void runParallelFlowable
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
                     testDownloadParallelFlowable(downloadAndStoreImage,
                                                  testName),
                     testName);
    }

    /**
     * This method runs the {@code downloadAndStoreImage} function
     * using the RxJava framework and its flatMap() method for
     * parallelizing downloads.
     */
    public static void testDownloadFlatMap
        (Function<URL, File> downloadAndStoreImage,
         String testName) {
        Function<URL, Observable<File>> downloadAndStore = url -> RxUtils
            // Emit this url and run it concurrently in the common
            // fork-join pool.
            .justConcurrentIf(url, true)

            // Transform each URL to a file by downloading each image.
            .map(downloadAndStoreImage::apply);

        // Get and print a list of files to the downloaded images.
        Observable
            // Convert the URLs in the input list into a stream of
            // observables.
            .fromIterable(Options.instance().getUrlList())

            // Apply the RxJava flatMap() idiom to process each url
            // concurrently.
            .flatMap(downloadAndStore::apply)

            // Collect the downloaded images into a list.
            .collectInto(new ArrayList<>(), List::add)

            // Print the statistics for this test run in a blocking
            // manner.
            .blockingSubscribe(imageFiles -> Options.instance()
                               .printStats(testName, imageFiles.size()));
    }

    /**
     * This method runs the {@code downloadAndStoreImage} function
     * using the RxJava framework and its ParallelFlowable mechanism
     * for parallelizing downloads.
     */
    public static void testDownloadParallelFlowable
        (Function<URL, File> downloadAndStoreImage,
         String testName) {

        RxUtils
            // Convert the URLs in the input list into a parallel
            // flowable stream.
            .fromIterableParallel(Options.instance().getUrlList())

            // Transform each url to a file via downloadAndStoreImage,
            // which downloads each image.
            .map(downloadAndStoreImage::apply)

            // Merge the values back into a single flowable.
            .sequential() 

            // Collect the downloaded images into a list.
            .collectInto(new ArrayList<>(), List::add)

            // Print the statistics for this test run in a blocking
            // manner.
            .blockingSubscribe(imageFiles -> Options.instance()
                               .printStats(testName, imageFiles.size()));
    }
}
