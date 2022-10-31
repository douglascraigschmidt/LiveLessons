package tests;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.FileUtils;
import utils.Options;
import utils.RunTimer;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

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
                                  String testName,
                                  Scheduler scheduler) {
        // First let the system garbage collect.
        System.gc();

        // Delete any filtered images from the previous run.
        FileUtils.deleteDownloadedImages();

        RunTimer
            // Record how long the test takes to run.
            .timeRun(() ->
                     // Run the test with the designated function.
                     testDownloadFlatMap(downloadAndStoreImage,
                                         testName,
                                         scheduler),
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
         String testName,
         Scheduler scheduler) {
        // Get and print a list of files to the downloaded images.
        Observable
            // Convert the URLs in the input list into a stream of
            // observables.
            .fromIterable(Options.instance().getUrlList())

            // Apply the RxJava flatMap() concurrency idiom to process
            // each url in parallel.
            .flatMap(url -> Observable
                     // Emit this url.
                     .fromCallable(() -> url)

                     // Run the URL concurrently in the given
                     // scheduler.  The placement of this operation
                     // can move down in this pipeline without
                     // affecting the behavior.
                     .subscribeOn(scheduler)

                     // Transform each url to a file by downloading
                     // the image.
                     .map(downloadAndStoreImage))

            // Collect the downloaded images into a list.
            .collect(Collectors.toList())

            // Process the list.
            .doOnSuccess(imageFiles -> Options
                // Print the # of image files that were downloaded.
                .printStats(testName, imageFiles.size()))

            // Print the statistics for this test run in a blocking
            // manner.
            .blockingGet();
    }

    /**
     * This method runs the {@code downloadAndStoreImage} function
     * using the RxJava framework and its ParallelFlowable mechanism
     * for parallelizing downloads.
     */
    public static void testDownloadParallelFlowable
        (Function<URL, File> downloadAndStoreImage,
         String testName) {

        Flowable
            // Convert collection into a flowable.
            .fromIterable(Options.instance().getUrlList())

            // Create a ParallelFlowable.
            .parallel()

            // Run this flow in the common fork-join pool.
            .runOn(Schedulers.from(ForkJoinPool.commonPool()))

            // Transform each url to a file via downloadAndStoreImage,
            // which downloads each image.
            .map(downloadAndStoreImage)

            // Merge the values back into a single flowable.
            .sequential() 

            // Collect the downloaded images into a list.
            .collect(Collectors.toList())

            // Process the list.
            .doOnSuccess(imageFiles -> Options
                // Print the # of image files that were downloaded.
                .printStats(testName, imageFiles.size()))

            // Print the statistics for this test run in a blocking
            // manner.
            .blockingGet();
    }
}
