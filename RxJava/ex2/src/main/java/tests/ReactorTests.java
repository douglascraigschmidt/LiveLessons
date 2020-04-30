package tests;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import utils.FileUtils;
import utils.Options;
import utils.ReactorUtils;
import utils.RunTimer;

import java.io.File;
import java.net.URL;
import java.util.function.Function;

import static utils.ReactorUtils.logIdentity;

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
                                  String testName,
                                  Scheduler scheduler,
                                  boolean log) {
        // First let the system garbage collect.
        System.gc();

        // Delete any filtered images from the previous run.
        FileUtils.deleteDownloadedImages();

        if (log)
            RunTimer
                // Record how long the logging test takes to run.
                .timeRun(() ->
                         // Run the test with the designated function.
                         testDownloadFlatMapLog(downloadAndStoreImage,
                                                testName,
                                                scheduler),
                         testName);
        else
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
     * downloadAndStoreImage} function with the Reactor driver that
     * uses its ParallelFlowable mechanism to download files in
     * parallel.
     *
     * @param downloadAndStoreImage A function that downloads and stores the image
     * @param testName Name of the test used to record the runtime stats
     * @param parallelism The parallelism level (i.e., number of "rails")
     * @param scheduler The pool used to run downloads in parallel.
     */
    public static void runParallelFlux
        (Function<URL, File> downloadAndStoreImage,
         String testName,
         int parallelism,
         Scheduler scheduler,
         boolean log) {
        // First let the system garbage collect.
        System.gc();

        // Delete any filtered images from the previous run.
        FileUtils.deleteDownloadedImages();

        if (log)
            RunTimer
                // Record how long the log test takes to run.
                .timeRun(() ->
                         // Run the test with the designated function.
                         testDownloadParallelFluxLog(downloadAndStoreImage,
                                                     testName,
                                                     parallelism,
                                                     scheduler),
                         testName);
        else
            RunTimer
                // Record how long the test takes to run.
                .timeRun(() ->
                         // Run the test with the designated function.
                         testDownloadParallelFlux(downloadAndStoreImage,
                                                  testName,
                                                  parallelism,
                                                  scheduler),
                         testName);

    }

    /**
     * This method runs the {@code downloadAndStoreImage} function
     * using the Reactor framework and its flatMap() idiom for
     * parallelizing downloads.
     */
    private static void testDownloadFlatMap
        (Function<URL, File> downloadAndStoreImage,
         String testName,
         Scheduler scheduler) {
        Flux
            // Convert the URLs in the input list into a stream.
            .fromIterable(Options.instance().getUrlList())

            // Apply the RxJava flatMap() idiom to process each url
            // concurrently.
            .flatMap(url -> Mono
                     // Omit this url
                     .just(url)

                     // Run the URL concurrently in the given scheduler.
                     // The placement of this operation can move down
                     // in this pipeline without affecting the behavior.
                     .subscribeOn(scheduler)

                     // Transform each url to a file by downloading the image.
                     .map(downloadAndStoreImage))

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
     * using the Reactor framework and its flatMap() idiom for
     * parallelizing downloads.
     */
    private static void testDownloadFlatMapLog
        (Function<URL, File> downloadAndStoreImage,
         String testName,
         Scheduler scheduler) {
        Flux
            // Convert the URLs in the input list into a stream.
            .fromIterable(Options.instance().getUrlList())

            .doOnNext(url -> logIdentity(url, "Flux.fromIterable()"))

            // Apply the RxJava flatMap() idiom to process each url
            // concurrently.
            .flatMap(url -> Mono
                     // Omit this url
                     .just(url)

                     .doOnNext(___ -> logIdentity(url, "Mono.just()"))

                     // Run URL concurrently in given scheduler.  The
                     // placement of this operation can move down in
                     // this pipeline without affecting the behavior.
                     .subscribeOn(scheduler)

                     .doOnNext(___ -> logIdentity(url, "map(downLoadAndStoreImage)"))

                     // Transform each url to a file by downloading the image.
                     .map(downloadAndStoreImage))

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
     *
     * @param downloadAndStoreImage A function that downloads and stores the image
     * @param testName Name of the test used to record the runtime stats
     * @param parallelism The parallelism level (i.e., number of "rails")
     * @param scheduler The pool used to run downloads in parallel.
     */
    private static void testDownloadParallelFlux
        (Function<URL, File> downloadAndStoreImage,
         String testName,
         int parallelism,
         Scheduler scheduler) {
        Flux
            // Convert the URLs in the input list into a parallel flux
            // stream.
            .fromIterable(Options.instance().getUrlList())

            // Set the parallelism level.
            .parallel(parallelism)

            // Set the scheduler.
            .runOn(scheduler)

            // Call downloadAndStoreImage to transform each URL to a
            // file by downloading each image via its URL.
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

    /**
     * This method runs the {@code downloadAndStoreImage} function
     * using the Reactor framework and its ParallelFlux mechanism for
     * parallelizing downloads.
     *
     * @param downloadAndStoreImage A function that downloads and stores the image
     * @param testName Name of the test used to record the runtime stats
     * @param parallelism The parallelism level (i.e., number of "rails")
     * @param scheduler The pool used to run downloads in parallel.
     */
    private static void testDownloadParallelFluxLog
        (Function<URL, File> downloadAndStoreImage,
         String testName,
         int parallelism,
         Scheduler scheduler) {
        Flux
            // Convert the URLs in the input list into a parallel flux
            // stream.
            .fromIterable(Options.instance().getUrlList())

            .doOnNext(url -> logIdentity(url, "Flux.fromIterable()"))

            // Set the parallelism level.
            .parallel(parallelism)

            // Set the scheduler.
            .runOn(scheduler)

            .doOnNext(url -> logIdentity(url, "map(downloadAndStoreImage)"))

            // Call downloadAndStoreImage to transform each URL to a
            // file by downloading each image via its URL.
            .map(downloadAndStoreImage)

            // Convert the parallel flux back to flux.
            .sequential()

            .doOnNext(url -> logIdentity(url, "collectList()"))

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
