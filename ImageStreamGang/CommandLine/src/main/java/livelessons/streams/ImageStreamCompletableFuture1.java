package livelessons.streams;

import livelessons.filters.Filter;
import livelessons.utils.Image;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static livelessons.utils.StreamOfFuturesCollector.toFuture;

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
            // stream complete their asynchronous processing.
            .thenAccept(stream ->
                        // Log the results.
                        log(stream
                            // Remove any empty optionals.
                            .flatMap(Optional::stream),
                            // For JDK 8 you'll need to use
                            // .filter(Optional::isPresent)
                            // .map(Optional::get),
                            urls.size()))

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
    private CompletableFuture<Optional<Image>> downloadImageAsync
              (CompletableFuture<Optional<URL>> urlFuture) {
        // Return a future that completes when the image finishes
        // downloading.
        return urlFuture
            // Use the executor to asynchronously download an image
            // when urlFuture completes.
            .thenApplyAsync(urlOpt ->
                            urlOpt
                            // Download non-null URLs.
                            .map(this::blockingDownload),
                            // Use the common fork-join pool.
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
    private Stream<CompletableFuture<Optional<Image>>> applyFiltersAsync
              (CompletableFuture<Optional<Image>> imageFuture) {
        return mFilters
            // Convert the list of filters to a sequential stream.
            .stream()

            // Use map() to filter each image asynchronously.
            .map(filter -> imageFuture
                 // Asynchronously apply a filter action after the
                 // previous stage completes.
                 .thenApplyAsync(imageOpt ->
                                 imageOpt
                                 // Create and apply the filter to the
                                 // image.
                                 .map(image ->
                                      makeFilterDecoratorWithImage(filter,
                                                                   image).run()),
                                 // Run in the common fork-join pool.
                                 getExecutor()));
    }
}
