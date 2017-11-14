package livelessons.streams;

import livelessons.filters.Filter;
import livelessons.utils.Image;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static livelessons.utils.FuturesCollectorStream.toFuture;

/**
 * This is another asynchronous implementation strategy that
 * customizes the ImageStreamCompletableFutureBase super class to
 * download, process, and store images asynchronously in the common
 * fork-join pool.
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

        // A future to a stream of images that are being downloaded,
        // filtered, and stored.
        CompletableFuture<Stream<Image>> resultsFuture = urls
            // Convert the URLs in the input list into a sequential
            // stream.
            .stream()

            // Use map() to ignore URLs that are already cached
            // locally, i.e., only download non-cached images.
            .map(this::checkUrlCachedAsync)

            // Use filter() to call nonNull(), which eliminates any
            // future that's null (i.e., url already cached).
            .filter(this::nonNull)

            // Use map() to call downloadImageAsync(), which
            // transforms each URL to a completable future to an image
            // (i.e., asynchronously download each image via its URL).
            .map(this::downloadImageAsync)

            // Use flatMap() to call applyFiltersAsync(), which returns a
            // future to a stream of filtered images.
            .flatMap(this::applyFiltersAsync)

            // Trigger intermediate processing and create a future
            // that can be used to wait for all operations associated
            // with the stream of futures to complete.
            .collect(toFuture());

        resultsFuture
            // thenAccept() is called when all the futures in the
            // stream complete their processing.
            .thenAccept(resultsStream -> System.out
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
     * Asynchronously download an image from the {@code urlFuture} parameter.
     *
     * @param urlFuture A future the URL to download
     * @return A future that completes when the image finishes downloading
     */
    CompletableFuture<Image> downloadImageAsync(CompletableFuture<URL> urlFuture) {
        // Return a future that completes when the image finishes
        // downloading.
        return urlFuture
            // Use the executor to asynchronously download an image
            // when urlFuture completes.
            .thenApplyAsync(this::blockingDownload,
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
