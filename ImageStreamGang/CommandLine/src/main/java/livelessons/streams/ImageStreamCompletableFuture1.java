package livelessons.streams;

import livelessons.filters.Filter;
import livelessons.utils.Image;
import livelessons.utils.StreamsUtils;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * This asynchronous implementation strategy customizes the
 * ImageStreamCompletableFutureBase super class to download, process,
 * and store images asynchronously in a designated executor's thread
 * pool.
 */
public class ImageStreamCompletableFuture1
       extends ImageStreamCompletableFutureBase {
    /**
     * Maximum number of threads in a fixed-size thread pool.
     */
    private final int sMAX_THREADS = 100;

    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture1(Filter[] filters,
                                         Iterator<List<URL>> urlListIterator) {
        super(filters, 
              urlListIterator);
    }

    /**
     * A hook method that's also a template method.  It sets the
     * executor to a fixed-size thread pool and calls up to the
     * superclass start the processing.
     */
    @Override
    protected void initiateStream() {
        // The thread pool size is the smaller of (1) the number of
        // filters times the number of images to download and (2)
        // sMAX_THREADS (which prevents allocating excessive threads).
        int threadPoolSize = Math.min(mFilters.size() * getInput().size(),
                sMAX_THREADS);

        // Initialize the Executor with appropriate pool of threads.
        setExecutor(Executors.newFixedThreadPool(threadPoolSize));

        // Call up to superclass to start the processing.
        super.initiateStream();
    }

    /**
     * This hook method uses Java 8 CompletableFutures to download,
     * process, and store images in asynchronously.
     */
    @Override
    protected void processStream() {
        // Get the input URLs.
        List<URL> urls = getInput();

        // Create a stream of completable futures to filtered images.
        Stream<CompletableFuture<Image>> futureStream = urls
            // Convert the URLs in the input list into a sequential
            // stream.
            .stream()

            // Use map() to ignore URLs that are already cached
            // locally, i.e., only download non-cached images.
            .map(this::checkUrlCachedAsync)

            // Use map() to ignore URLs that are already cached
            // locally, i.e., only download non-cached images.
            .filter(this::nonNull)

            // Use map() to call downloadImageAsync(), which
            // transforms each URL to a completable future to an image
            // (i.e., asynchronously download each image via its URL).
            .map(this::downloadImageAsync)

            // Use flatMap() to call applyFiltersAsync(), which
            // creates a stream of completable futures to multiple
            // filtered/stored versions of each image.
            .flatMap(this::applyFiltersAsync);

        StreamsUtils
            // Create a CompletableFuture that can be used to wait for
            // all futures associated with futureStream to complete.
            .joinAllStream(futureStream)

            // This completion stage method is called after the future
            // from the previous stage completes, which occurs when
            // all the futures in the stream complete.
            .thenAccept(resultsStream -> System.out
                        // Print the results.
                        .println(TAG
                                + ": processing of "
                                + resultsStream
                                // Count the number of elements in the
                                // results stream.
                                .count()
                                + " image(s) from "
                                + urls.size() 
                                + " urls is complete"))

            // Wait until all the images have been downloaded,
            // processed, and stored.
            .join();
    }

    /**
     * Apply filters asynchronously to the {@code a imageFuture} after
     * it finishes downloading and store the results in output files
     * on the local computer.
     *
     * @param imageFuture A future to an image that's being downloaded
     @ return A stream of completable futures to images that are being filtered/stored
     */
    private Stream<CompletableFuture<Image>> applyFiltersAsync
                                               (CompletableFuture<Image> imageFuture) {
        return mFilters
            // Convert the list of filters to a sequential stream.
            .stream()

            // Use map() to filter each image asynchronously.
            .map(filter -> imageFuture
                 // Asynchronously create a FilterDecoratorWithImage
                 // object for each filter/image combo and apply this
                 // filter to an image in the designated executor.
                 .thenApplyAsync(image ->
                                 makeFilterDecoratorWithImage(filter,
                                                              image).run(),
                                 // Run in the designated executor.
                                 getExecutor()));
    }
}
