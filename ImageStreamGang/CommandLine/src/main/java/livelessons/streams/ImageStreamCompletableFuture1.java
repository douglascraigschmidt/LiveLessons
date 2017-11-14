package livelessons.streams;

import livelessons.filters.Filter;

import static java.util.stream.Collectors.toList;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import livelessons.utils.Image;
import livelessons.utils.StreamsUtils;
import livelessons.filters.FilterDecoratorWithImage;

/**
 * This asynchronous implementation strategy customizes the
 * ImageStreamCompletableFutureBase super class to download, process,
 * and store images asynchronously and concurrently in a thread in the
 * executor's thread pool.
 */
public class ImageStreamCompletableFuture1
       extends ImageStreamCompletableFutureBase {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture1(Filter[] filters,
                                         Iterator<List<URL>> urlListIterator) {
        super(filters, urlListIterator);
    }

    /**
     * Use Java 8 CompletableFutures to download, process, and store
     * images concurrently and asynchronously.
     */
    @Override
    protected void processStream() {
        // Get the input URLs.
        List<URL> urls = getInput();

        // Create a list of completable futures to filtered images.
        Stream<CompletableFuture<Image>> futureStream = urls
            // Convert the URLs in the input list into a sequential
            // stream.
            .stream()

            // Use map() to ignore URLs that are already cached
            // locally, i.e., only download non-cached images.
            .map(this::checkUrlCachedAsync)

            // Use filter() to eliminate any future that's null (i.e.,
            // url already cached).
            .filter(this::nonNull)

            // Use map() to transform each URL to a completable future
            // to an image (i.e., asynchronously download each image
            // via its URL).
            .map(this::downloadImageAsync)

            // Use flatMap() to create a stream containing completable
            // futures to multiple filtered/stored versions of each
            // image.
            .flatMap(this::applyFiltersAsync);

        StreamsUtils
            // Create a CompletableFuture that can be used to wait for
            // all futures associated with futureStream to complete.
            .joinAllStream(futureStream)

            // This completion stage method is called after the future
            // from the previous stage completes.
            .thenAccept(resultsStream ->
                        // Print the results.
                        System.out.println(TAG
                                           + ": processing of "
                                           + resultsStream.count()
                                           + " image(s) from "
                                           + urls.size() 
                                           + " urls is complete"))

            // Wait until all the images have been downloaded,
            // processed, and stored.
            .join();
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
            .thenApplyAsync(this::downloadImage,
                            getExecutor());
    }

    /**
     * Apply filters asynchronously and concurrently to the @a
     * imageFuture after it finishes downloading and store the results
     * in output files on the local computer.
     */
    private Stream<CompletableFuture<Image>> applyFiltersAsync
        (CompletableFuture<Image> imageFuture) {
        return mFilters
            // Convert the list of filters to a sequential stream.
            .stream()

            // Use map() to create a completable future to a
            // FilterDecoratorWithImage object for each filter/image.
            .map(filter ->
                 // Returns a new CompletionStage that, when this
                 // stage completes normally, is executed with this
                 // stage's result as the argument to the supplied
                 // lambda expression.
                 imageFuture.thenApply(image ->
                                       makeFilterDecoratorWithImage(filter,
                                                                    image)))
                                                 
            // Asynchronously filter the image and store it in an
            // output file.
            .map(filterFuture ->
                 // Returns a new CompletionStage that, when this
                 // stage completes normally, is executed with this
                 // stage's result as the argument to the supplied
                 // lambda expression.
                 filterFuture.thenApplyAsync(FilterDecoratorWithImage::run,
                                             getExecutor()));
    }
}
