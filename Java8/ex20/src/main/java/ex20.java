import utils.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This example shows how to combine Java parallel streams with and
 * without the ForkJoinPool.ManagedBlocker interface and the Java
 * fork-join framework to download multiple images from a remote
 * server.
 */
public class ex20 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex20.class.getName();

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Run the test program.
        new ex20().run();
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
        // framework's default behavior, which does not add new worker
        // threads to the pool when blocking occurs.
        runTest(this::downloadAndStoreImage,
                "testDefaultDownloadBehavior()");

        // Run the tests using the using the BlockingTask wrapper
        // around the Java fork-join framework's ManagedBlocker
        // mechanism, which adds new worker threads to the pool
        // adaptively when blocking occurs.
        runTest(this::downloadAndStoreImageMB,
                "testAdaptiveMBDownloadBehavior()");

        // Run the tests using the using the BlockingTask wrapper for
        // the Java fork-join framework's ManagedBlocker mechanism,
        // which adds new worker threads to the pool adaptively when
        // blocking occurs.
        runTest(this::downloadAndStoreImageBT,
                "testAdaptiveBTDownloadBehavior()");

        // Print the results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving the download tests program");
    }

    /**
     * Run the test named {@code testName} by appying the {@code
     * downloadAndStoreImage} function.
     */
    private void runTest(Function<URL, File> downloadAndStoreImage,
                         String testName) {
        // Let the system garbage collect.
        System.gc();

        // Record how long the test takes to run.
        RunTimer.timeRun(() ->
                         // Run the test with the designated function.
                         testDownloadBehavior(downloadAndStoreImage,
                                              testName),
                         testName);
    }

    /**
     * This method runs the tests via the {@code
     * downloadAndStoreImage} function.
     */
    private void testDownloadBehavior(Function<URL, File> downloadAndStoreImage,
                                      String testName) {
        // Delete any the filtered images from the previous run.
        deleteDownloadedImages();

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
    }

    /**
     * Transform URL to a File by downloading each image via its URL
     * and storing it *without* using the Java fork-join framework's
     * ManagedBlocker mechanism, i.e., the pool of worker threads will
     * not be expanded.
     */
    private File downloadAndStoreImage(URL url) {
        return 
            // Perform a blocking image download.
            downloadImage(url)

            // Store the image on the local device.
            .store();
    }

    /**
     * Transform URL to a File by downloading each image via its URL
     * and storing it using the Java fork-join framework's
     * ManagedBlocker mechanism, which adds new worker threads to the
     * pool adaptively when blocking occurs.
     */
    private File downloadAndStoreImageMB(URL url) {
        // Create a one element array so we can update it in the
        // anonymous inner class instance below.
        final Image[] image = new Image[1];

        try {
            ForkJoinPool
                // Submit an anonymous managedBlock implementation to
                // the common ForkJoin thread pool.  This call ensures
                // the common fork/join thread pool is expanded to
                // handle the blocking image download.
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
     * Transform URL to a File by downloading each image via its URL
     * and storing it using the BlockingTask wrapper around the Java
     * fork-join framework's ManagedBlocker mechanism, which adds new
     * worker threads to the pool adaptively when blocking occurs.
     */
    private File downloadAndStoreImageBT(URL url) {
        return BlockingTask
            // This call ensures the common fork/join thread pool
            // is expanded to handle the blocking image download.
            .callInManagedBlock(() -> downloadImage(url))

            // Store the image on the local device.
            .store();
    }

    /**
     * Factory method that blocks while retrieving the image
     * associated with the {@code url} and creating an Image to
     * encapsulate it.
     */
    private Image downloadImage(URL url) {
        return new Image(url,
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
     * Clears the filter directories.
     */
    private void deleteDownloadedImages() {
        int deletedFiles =
            deleteSubFolders(Options.instance().getDirectoryPath());

        if (Options.instance().diagnosticsEnabled())
            System.out.println(TAG
                               + ": "
                               + deletedFiles
                               + " previously downloaded file(s) deleted");
    }

    /**
     * Recursively delete files in a specified directory.
     */
    private int deleteSubFolders(String path) {
        int deletedFiles = 0;
        File currentFolder = new File(path);        
        File[] files = currentFolder.listFiles();

        if (files == null) 
            return 0;

        // Java doesn't delete a directory with child files, so we
        // need to write code that handles this recursively.
        for (File f : files) {          
            if (f.isDirectory()) 
                deletedFiles += deleteSubFolders(f.toString());
            f.delete();
            deletedFiles++;
        }

        // Don't delete the current folder.
        // currentFolder.delete();
        return deletedFiles;
    }

    /**
     * This method warms up the default thread pool.
     */
    private void warmUpThreadPool() {
        testDownloadBehavior(this::downloadAndStoreImage,
                             "warmup");
    }
}
