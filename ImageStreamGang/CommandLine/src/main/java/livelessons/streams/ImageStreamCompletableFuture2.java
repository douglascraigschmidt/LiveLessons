package livelessons.streams;

import livelessons.filters.Filter;
import java.net.URL;
import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import livelessons.utils.Image;
import livelessons.utils.StreamsUtils;
import livelessons.filters.FilterDecoratorWithImage;
import static java.util.stream.Collectors.summingInt;
import static livelessons.utils.FuturesCollector.toFuture;

/**
 * This is another asynchronous implementation strategy that
 * customizes the ImageStreamCompletableFutureBase super class to
 * download, process, and store images asynchronously and concurrently
 * in a thread in the executor's thread pool.
 */
public class ImageStreamCompletableFuture2
       extends ImageStreamCompletableFutureBase {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture2(Filter[] filters,
                                         Iterator<List<URL>> urlListIterator) {
        super(filters, urlListIterator);
    }

    /**
     * Use Java 8 CompletableFutures to download, process, and store
     * images concurrently.
     */
    @Override
    protected void processStream() {
        // Get the input URLs.
        List<URL> urls = getInput();

        // A completable futures to the number of images downloaded and
        // filtered.
        CompletableFuture<Integer> imagesProcessed = urls
            // Convert the URLs in the input list into a sequential
            // stream.
            .stream()

            // Use map() to ignore URLs that are already cached
            // locally, i.e., only download non-cached images.
            .map(this::checkUrlCachedAsync)

            // Use filter() to eliminate any future that's null (i.e.,
            // url already cached).
            .filter(future -> future.join() != null)

            // Use map() to transform each URL to a completable future
            // to an image (i.e., asynchronously download each image
            // via its URL).
            .map(this::downloadImageAsync)

            // Use map() to call the applyFiltersAsync() method
            // reference, which returns a list of filtered Image
            // futures.
            .map(this::applyFiltersAsync)

            // Trigger intermediate processing and create a
            // CompletableFuture that can be used to wait for all
            // operations associated with the futures to complete.
            .collect(toFuture())

            // When all image processing is done return a count
            // of the number of images downloaded/filtered.
            .thenApply(list -> list
                       // Convert list to stream.
                       .stream()

                       // Sum up the counts of all the processed images.
                       .collect(summingInt(List::size)));

        System.out.println(TAG
                           + ": processing of "
                           + imagesProcessed.join()
                           + " image(s) from "
                           + urls.size()
                           + " urls is complete");
    }

    /**
     * Asynchronously apply all the filters to images and store them
     * in an output file on the local computer.
     */
    private CompletableFuture<List<Image>> applyFiltersAsync
        (CompletableFuture<Image> imageFuture) {
        // Return a completable future to an array of images that are
        // being processed.
        return imageFuture
            .thenCompose(image -> mFilters
                         // Convert image filters to a sequential
                         // stream.
                         .stream()

                         // Create a FilterDecoratorWithImage object
                         // for each filter/image combo and apply this
                         // filter to an image.
                         .map(filter ->
                              filterImageAsync(makeFilterDecoratorWithImage(filter,
                                                                            image)))

                         // Collect the list of futures.
                         .collect(toFuture()));
    }

    /**
     * Asynchronously download an image from the @a url parameter and
     * return a CompletableFuture that completes when the image
     * finishes downloading.
     */
    private CompletableFuture<Image> downloadImageAsync
            (CompletableFuture<URL> urlFuture) {
        // Asynchronously download an Image from the url parameter.
        return urlFuture
            .thenApplyAsync(this::blockingDownload);
    }
}
