import filters.*;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This example compares and contrasts the programming models and
 * performance results of Java parallel streams, completable futures,
 * and Project Loom structured concurrency when applied to download
 * many images from a remote web server.
 */
public class ex3 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex3.class.getName();

    /**
     * The List of filters to apply to the images.
     */
    protected List<Filter> mFilters = List
        .of(new NullFilter(), new GrayScaleFilter());


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
     * Constructor initializes the cache.
     */
    public ex3() {
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
                this::downloadImage,
                "testParallelStreamDownload()");

        /*
        // Runs the tests using the Java completable futures framework.
        runTest(this::testDownloadCF,
                this::downloadImage,
                "testCompletableFuturesDownload()");

        // Runs the tests using Project Loom's structured concurrency model.
        runTest(this::testDownloadSC,
                this::downloadImage,
                "testStructuredConcurrencyDownload()");
        */

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
    private void runTest(BiFunction<Function<URL, Image>, String, Void> test,
                         Function<URL, Image> downloadAndStoreImage,
                         String testName) {
        // Let the system garbage collect to avoid perturbing results.
        System.gc();

        // Record how long the test takes to run.
        RunTimer.timeRun(() ->
                         // Run the designated test on the
                         // downloadAndStoreImage function.
                         test.apply(downloadAndStoreImage,
                                    testName),
                         testName);

        // Delete any images from the previous run.
        FileAndNetUtils
            .deleteDownloadedImages(Options.instance().getDirectoryPath());
    }

    /**
     * This method uses Java parallel streams to run the tests via the
     * {@code downloadAndStoreImage} function.
     *
     * @param downloadImage A {@link Function} that downloads an image
     * @param testName The name of the test that's being run
     */
    private Void testDownloadPS(Function<URL, Image> downloadImage,
                                String testName) {
        // Get the List downloaded Image objects.
        List<Image> imageFiles = Options.instance()
            // Get the List of URLs.
            .getUrlList()

            // Convert List into a parallel stream.
            .parallelStream()
                                        
            // Use filter() to ignore URLs that are already cached
            // locally, i.e., only download non-cached images.
            .filter(Predicate.not(this::urlCached))

            // Transform URL to a File by downloading each image via
            // its URL.
            .map(this::downloadImage)

            // Apply filters to all images and store them locally.
            .flatMap(this::applyFiltersPS)

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
     * @param downloadImage A {@link Function} that downloads an image
     * @param testName The name of the test that's being run
     */
    private Void testDownloadCF(Function<URL, Image> downloadImage,
                                String testName) {
        // Get the list of files to the downloaded images.
        CompletableFuture<List<Image>> imageFilesFuture = Options.instance()
            // Get the List of URLs.
            .getUrlList()

            // Convert the List to a stream.
            .stream()

            // Transform URL to a File by downloading each image via
            // its URL.
            .map(url -> CompletableFuture
                 // Download each image and store it in a file
                 // asynchronously.
                 .supplyAsync(() ->
                              downloadImage(url)))

            .flatMap(this::applyFiltersCF)

            // Terminate the stream and collect the results into list
            // of images.
            .collect(FuturesCollector.toFuture());

        // Print the statistics for this test run.
        printStats(testName, imageFilesFuture.join().size());
        return null;
    }

    /**
     * This method uses Project Loom structure concurrency to run the
     * tests via the {@code downloadImage} function.
     *
     * @param downloadImage A {@link Function} that downloads an image
     * @param testName The name of the test that's being run
     */
    private Void testDownloadSC(Function<URL, Image> downloadImage,
                                String testName) {
        // A List of Future<Image> objects that holds the results of
        // downloading images.
        List<Future<File>> imageFutures = new ArrayList<>();

        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        /*
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
        */
        // Print the statistics for this test run.
        printStats(testName, imageFutures.size());
        return null;
    }

    /**
     * Transform a {@link URL} to a {@link File} by downloading the
     * contents of the {@code url}.
     *
     * @param url The {@link URL} of the image to download
     * @return An {@link Image} containing the image contents
     */
    private Image downloadImage(URL url) {
        return 
            // Perform a blocking image download.
            new Image(url,
                      FileAndNetUtils.downloadContent(url));
    }

    /**
     * Use parallel streams to apply all the image filters concurrently to
     * each {@Link Image}.
     *
     * @param image The {@link Image} to filter
     * @return A stream of filtered {@link Image} objects
     */
    private Stream<Image> applyFiltersPS(Image image) {
        return mFilters
            // Iterate through the list of image filters concurrently
            // and apply each one to the image.
            .parallelStream()

            // Use map() to create an OutputFilterDecorator for each
            // image and run it to filter each image and store it in
            // an output file.
            .map(filter ->
                 makeFilterDecoratorWithImage(filter, image).run());
    }

    /**
     * Apply completable future sto asynchronously apply filters to
     * the {@code imageFuture} after it finishes downloading and store
     * the results in output files on the local computer.
     *
     * @param imageFuture A future to an {@link Image} that's being
     *                    downloaded
     * @return A stream of futures to {@link Image} objects that are
     *         being filtered/stored
     */
    private Stream<CompletableFuture<Image>> applyFiltersCF
              (CompletableFuture<Image> imageFuture) {
        return mFilters
            // Convert the list of filters to a sequential stream.
            .stream()

            // Use map() to filter each image asynchronously.
            .map(filter -> imageFuture
                 // Asynchronously apply a filter action after the
                 // previous stage completes.
                 .thenApplyAsync(image ->
                                 // Create and apply the filter to the
                                 // image.
                                 makeFilterDecoratorWithImage(filter,
                                                              image).run()));
    }

    /**
     * Factory method that makes a new {@link FilterDecoratorWithImage}.
     */
    protected FilterDecoratorWithImage makeFilterDecoratorWithImage(Filter filter,
                                                                    Image image) {
        return new FilterDecoratorWithImage(new OutputFilterDecorator(filter),
                                            image);
    }

    /**
     * Checks to see if the {@code url} is already exists in the file
     * system.  If not, it atomically creates a new file based on
     * combining the {@code url} with the {@code filterName} and
     * returns false, else true.

     * @return true if the {@code url} already exists in file system,
     * else false.
     */
    protected boolean urlCached(URL url,
                                String filterName) {
        File imageFile = null;
        try {
            // Construct the subdirectory for the filter.
            File externalFile =
                new File(Options.instance().getDirectoryPath(),
                         filterName);

            // Construct a new file based on the filename for the URL.
            imageFile =
                new File(externalFile,
                         FileAndNetUtils.getFileNameForUrl(url));
            
            // The URL is already cached if imageFile exists so we
            // negate the return value from createNewFile().
            return !imageFile.createNewFile();
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("file " + imageFile.toString() + e);
            return true;
        }
    }

    /**
     * @return true if the {@code url} is in the cache, else false.
     */
    private boolean urlCached(URL url) {
        // Iterate through the list of filters and check to see which
        // images already exist in the cache.
        return mFilters
            // Convert list of filters into a stream.
            .stream()

            // Return true if any file already exists, else false.
            .anyMatch(filter ->
                      urlCached(url, filter.getName()));
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
