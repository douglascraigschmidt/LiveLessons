import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import utils.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This example downloads multiple images from a remote web server via
 * several different mechanisms, including Java parallel streams and
 * RxJava.  It also compares the performance of Java parallel streams
 * and RxJava with and without the ForkJoinPool.ManagedBlocker
 * interface and the Java common fork-join pool.
 */
public class ex2 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex2.class.getName();

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Run the test program.
        new ex2().run();
    }

    /**
     * Run the test program.
     */
    private void run() {
        System.out.println("Entering the download tests program with "
                           + Runtime.getRuntime().availableProcessors()
                           + " cores available");

        // Warm up the common fork-join pool.
        warmUpThreadPool();

        // Runs the tests using the using the Java fork-join
        // framework's default behavior, which does not add any new
        // worker threads to the pool when blocking on I/O occurs.
        runTest(this::downloadAndStoreImage,
                "testDefaultDownloadBehavior()");

        // Run the tests using the using the Java fork-join
        // framework's ManagedBlocker mechanism, which adds new worker
        // threads to the pool adaptively when blocking on I/O occurs.
        runTest(this::downloadAndStoreImageMB,
                "testAdaptiveMBDownloadBehavior()");

        // Run the tests using the using the BlockingTask wrapper for
        // the Java fork-join framework's ManagedBlocker mechanism,
        // which adds new worker threads to the pool adaptively when
        // blocking on I/O occurs.
        runTest(this::downloadAndStoreImageBT,
                "testAdaptiveBTDownloadBehavior()");

        // Run the tests using the RxJava along with the BlockingTask
        // wrapper for the Java fork-join framework's ManagedBlocker
        // mechanism, which adds new worker threads to the pool
        // adaptively when blocking on I/O occurs.
        runTestRx(this::downloadAndStoreImageBT,
                  "testAdaptiveBTDownloadBehaviorBTRx()");

        // Print the results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving the download tests program");
    }

    /**
     * Run the test named {@code testName} by appying the {@code
     * downloadAndStoreImage} function using the parallel streams
     * driver.
     */
    private void runTest(Function<URL, File> downloadAndStoreImage,
                         String testName) {
        // First let the system garbage collect.
        System.gc();

        // Record how long the test takes to run.
        RunTimer.timeRun(() ->
                         // Run the test with the designated function.
                         testDownloadBehavior(downloadAndStoreImage,
                                              testName),
                         testName);
    }

    /**
     * Run the test named {@code testName} by appying the {@code
     * downloadAndStoreImage} function with the RxJava driver.
     */
    private void runTestRx(Function<URL, File> downloadAndStoreImage,
                           String testName) {
        // First let the system garbage collect.
        System.gc();

        // Record how long the test takes to run.
        RunTimer.timeRun(() ->
                         // Run the test with the designated function.
                         testDownloadBehaviorRx(downloadAndStoreImage,
                                                testName),
                         testName);
    }

    /**
     * This method runs the tests via the {@code
     * downloadAndStoreImage} function using the parallel streams
     * framework.
     */
    private void testDownloadBehavior(Function<URL, File> downloadAndStoreImage,
                                      String testName) {
        // Delete any filtered images from the previous run.
        FileUtils.deleteDownloadedImages();

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
            .collect(Collectors.toList());

        // Print the statistics for this test run.
        printStats(testName, imageFiles.size());
    }

    /**
     * This method runs the tests via the {@code
     * downloadAndStoreImage} function using the RxJava framework.
     */
    private void testDownloadBehaviorRx(Function<URL, File> downloadAndStoreImage,
                                        String testName) {
        // Delete any filtered images from the previous run.
        FileUtils.deleteDownloadedImages();

        // Get and print a list of files to the downloaded images.
        Observable
            // Convert the URLs in the input list into a stream of
            // observables.
            .fromIterable(Options.instance().getUrlList())

            // Run these operations in the common fork-join thread
            // pool.
            .subscribeOn(Schedulers.from(ForkJoinPool.commonPool()))

            // Transform each URL to a File by calling the
            // downloadAndStoreImage function, which downloads each
            // image via its URL.
            .map(downloadAndStoreImage)

            // Collect the results into list of images.
            .collectInto(new ArrayList<>(), List::add)

            // Print the statistics for this test run.
            .blockingSubscribe(imageFiles ->
                               printStats(testName, imageFiles.size()));
    }

    /**
     * Transform the {@code url} to a {@code File} by downloading each
     * image via its URL and storing it to the local file system.
     * This method does not use the {@code ManagedBlocker} mechanism
     * in the Java fork-join framework, i.e., the pool of worker
     * threads will not be expanded when blocking on I/O occurs.
     */
    private File downloadAndStoreImage(URL url) {
        return 
            // Perform a blocking image download.
            downloadImage(url)

            // Store the image on the local device.
            .store();
    }

    /**
     * Transform the {@code url} to a {@code File} by downloading each
     * image via its URL and storing it to the local file system.
     * This method uses the {@code ManagedBlocker} mechanism in the
     * Java fork-join framework, which adds new worker threads to the
     * pool adaptively when blocking on I/O occurs.
     */
    private File downloadAndStoreImageMB(URL url) {
        // Create a one element array so we can update it in the
        // anonymous inner class instance below.
        final Image[] image = new Image[1];

        try {
            ForkJoinPool
                // Submit an anonymous managedBlock implementation to
                // the common fork-join thread pool.  This call
                // ensures the common fork-join thread pool is
                // expanded to handle the blocking image download.
                .managedBlock(new ForkJoinPool.ManagedBlocker() {
                        /**
                         * Download the image, which will block the
                         * calling thread.
                         */
                        @Override public boolean block() {
                            image[0] = downloadImage(url);
                            return true;
                        }
                        
                        /**
                         * Always return false.
                         */
                        @Override public boolean isReleasable() {
                            return false;
                        }
                    });
        } catch (InterruptedException e) {
            throw new Error(e);
        }

        // Store and return the image on the local device.
        return image[0].store();
    }

    /**
     * Transform the {@code url} to a {@code File} by downloading each
     * image via its URL and storing it on the local file system.
     * This method uses the {@code BlockingTask} wrapper around the
     * {@code ManagedBlocker} mechanism in the Java fork-join
     * framework, which adds new worker threads to the pool adaptively
     * when blocking on I/O occurs.
     */
    private File downloadAndStoreImageBT(URL url) {
        return BlockingTask
                // This call ensures the common fork-join thread pool
                // is expanded to handle the blocking image download.
                .callInManagedBlock(() -> downloadImage(url))

                // Store the image on the local device.
                .store();
    }

    /**
     * Factory method that retrieves the image associated with the
     * {@code url} and creates an {@code Image} to encapsulate it.
     * This method blocks until I/O has completed.
     */
    private Image downloadImage(URL url) {
        return new Image(url,
                         // Download the content from the url.
                         NetUtils.downloadContent(url));
    }

    /**
     * Display the statistics about the test.
     */
    private void printStats(String testName, 
                            int imageCount) {
        if (!testName.equals("warmup"))
            System.out.println(TAG 
                               + ": "
                               + testName
                               + " downloaded and stored "
                               + imageCount
                               + " images using "
                               + (ForkJoinPool.commonPool().getPoolSize() + 1)
                               + " threads in the pool");
    }

    /**
     * This method warms up the default thread pool.
     */
    private void warmUpThreadPool() {
        testDownloadBehavior(this::downloadAndStoreImage,
                             "warmup");
    }
}
