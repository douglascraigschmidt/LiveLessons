package livelessons.streams;

import livelessons.filters.Filter;
import livelessons.utils.Image;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Stream;

import static livelessons.utils.FuturesCollectorStream.toFuture;

/**
 * This is another asynchronous implementation strategy that
 * customizes the ImageStreamCompletableFutureBase super class to
 * download, process, and store images asynchronously the common
 * fork-join pool.
 */
public class ImageStreamCompletableFuture2
       extends ImageStreamCompletableFutureBase {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture2(Filter[] filters,
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

            // Use map() to call applyFiltersAsync(), which returns a
            // future to a stream of filtered images.
            .map(this::applyFiltersAsync)

            // Trigger intermediate processing and create a future
            // that can be used to wait for all operations associated
            // with the stream of futures to complete.
            .collect(toFuture())

            // This completion stage method is called after the future
            // from the previous stage completes, which occurs when
            // all the futures in the stream of streams complete.
            .thenAccept(resultsStream -> System.out
                        .println(TAG
                                 + ": processing of "
                                + resultsStream
                                // Flatten the stream of streams.
                                .flatMap(Function.identity())

                                // Count the number of elements in the
                                // flattened stream.
                                .count()
                                 + " image(s) from "
                                 + urls.size()
                                 + " urls is complete"))

            // Wait until all the images have been downloaded,
            // processed, and stored.
            .join();
    }

    /**
     * Asynchronously apply all the filters to images and store them
     * in an output file on the local computer.
     *
     * @param imageFuture A future to an image that's being downloaded
     @ return A completable future to a stream of images that are being filtered/stored 
     */
    private CompletableFuture<Stream<Image>> applyFiltersAsync
                                               (CompletableFuture<Image> imageFuture) {
        // Return a completable future to a stream of images that are
        // being processed.
        return imageFuture
            // thenCompose() is called after the image download
            // completes.  It works like flatMap(), i.e., it returns a
            // future to a stream of futures.
            .thenCompose(image -> mFilters
                         // Convert image filters to a sequential
                         // stream.
                         .stream()

                         // Use map() to asynchronously create a
                         // FilterDecoratorWithImage object for each
                         // filter/image combo and apply this filter
                         // to an image.
                         .map(filter -> CompletableFuture
                              .supplyAsync(() -> makeFilterDecoratorWithImage
                                           (filter, image).run(),
                                           // Run in the common fork-join pool.
                                           getExecutor()))

                         // Collect a stream of futures.
                         .collect(toFuture()));
    }
}
