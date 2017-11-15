package livelessons.streams;

import livelessons.filters.Filter;
import livelessons.utils.Image;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static livelessons.utils.FuturesCollectorStream.toFuture;

/**
 * This asynchronous implementation strategy customizes the
 * ImageStreamCompletableFutureBase super class to download, process,
 * and store images asynchronously in a common fork-join thread pool.
 */
public class ImageStreamCompletableFuture1
       extends ImageStreamCompletableFutureBase {
     /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture1(Filter[] filters,
                                         Iterator<List<URL>> urlListIterator) {
        super(filters, 
              urlListIterator);
    }

    /**
     * A hook method that's also a template method.  It assigns the
     * executor to the common fork-join pool and calls up to the
     * superclass start the processing.
     */
    @Override
    protected void initiateStream() {
        // Set the executor to the common fork-join pool.
        setExecutor(ForkJoinPool.commonPool());

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

        urls
            // Convert the URLs in the input list into a sequential
            // stream.
            .stream()

            // Use map() to call checkUrlCachedAsync(), which
            // asynchronously checks if a URL is aready cached.
            .map(this::checkUrlCachedAsync)

            // Use map() to call downloadImageAsync(), which
            // transforms each non-cached URL to a future to an image
            // (i.e., asynchronously download each image via its URL).
            .map(this::downloadImageAsync)

            // Use flatMap() to call applyFiltersAsync(), which
            // returns a future to a stream of filtered images that
            // are not already cached locally.
            .flatMap(this::applyFiltersAsync)

            // Trigger intermediate processing and create a future
            // that can be used to wait for all operations associated
            // with the stream of futures to complete.
            .collect(toFuture())

            // thenAccept() is called when all the futures in the
            // stream complete their processing.
            .thenAccept(stream ->
                        // Log the results.
                        logResults(stream, urls.size()))

            // Wait until all the images have been downloaded,
            // processed, and stored.
            .join();
    }

    /**
     * Asynchronously download an image from the {@code urlFuture} parameter.
     *
     * @param urlFuture A future the URL to download
     * @return A future that completes when the image finishes downloading
     */
    private CompletableFuture<Image> downloadImageAsync(CompletableFuture<URL> urlFuture) {
        // Return a future that completes when the image finishes
        // downloading.
        return urlFuture
            // Use the executor to asynchronously download an image
            // when urlFuture completes.
            .thenApplyAsync(url ->
                            url == null 
                            // Ignore null URLs.
                            ? null
                            // Download non-null URLs.
                            : downloadImage(url),
                            // Use the 
                            getExecutor());
    }

    /**
     * Asynchronously apply filters to the {@code imageFuture} after
     * it finishes downloading and store the results in output files
     * on the local computer.
     *
     * @param imageFuture A future to an image that's being downloaded
     @ return A stream of completable futures to images that are being filtered/stored
    */
    private Stream<CompletableFuture<Image>> applyFiltersAsync(CompletableFuture<Image> imageFuture) {
        return mFilters
            // Convert the list of filters to a sequential stream.
            .stream()

            // Use map() to filter each image asynchronously.
            .map(filter -> imageFuture
                 // Asynchronously apply a filter action after the
                 // previous stage completes.
                 .thenApplyAsync(image ->
                                 image == null
                                 // Ignore null images.
                                 ? null
                                 // Create and apply the filter to the
                                 // image.
                                 : makeFilterDecoratorWithImage(filter,
                                                                image).run(),
                                 // Run in the common fork-join pool.
                                 getExecutor()));
    }

    /**
     * Log the results.
     * 
     * @param resultsStream A stream of images that have been
     * downloaded, processed, and stored
     * @param urlsSize The number of URLs to download
     */
    private void logResults(Stream<Image> resultsStream,
                            int urlsSize) {
        // Print the results to the log.
        System.out
            .println(TAG
                     + ": processing of "
                     + resultsStream
                     // Remove any null objects stemming from URLs
                     // that were already cached.
                     .filter(Objects::nonNull)

                     // Count the number of elements in the results
                     // stream.
                     .count()
                     + " image(s) from "
                     + urlsSize
                     + " urls is complete");
    }
}
