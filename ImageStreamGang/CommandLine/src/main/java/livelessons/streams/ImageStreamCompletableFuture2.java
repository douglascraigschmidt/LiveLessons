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

        // Create a list of completable futures to a list of images.
        CompletableFuture<List<List<Image>>> allImagesDone = urls
            // Convert the URLs in the input list into a sequential
            // stream.
            .stream()

            // Use filter() to ignore URLs that are already cached locally,
            // i.e., only download non-cached images.
            .filter(StreamsUtils.not(this::urlCached))

            // Use map() to transform each URL to a completable future
            // to an image (i.e., asynchronously download each image
            // via its URL).
            .map(this::downloadImageAsync)

            // Use map() to call makeFilterDecorators(), which returns
            // a List of FilterDecoratorWithImage objects associated
            // with a completable future.
            .map(this::makeFilterDecoratorsAsync)

            // Use map() to call the applyFiltersAsync() method
            // reference, which returns a list of filtered Image
            // futures.
            .map(this::applyFiltersAsync)

            // Trigger intermediate processing and create a
            // CompletableFuture that can be used to wait for all
            // operations associated with the futures to complete.
            .collect(toFuture());

        // The call to join() is needed here to blocks the calling
        // thread until all the futures have completed.
        Integer imagesProcessed = allImagesDone
            // join() returns a list of list of images, so we convert
            // this to a stream and then sum up the size of each of
            // these lists to get the final count of images processed.
            .join()
            .stream()
            .collect(summingInt(List::size));

        System.out.println(TAG
                           + ": processing of "
                           + imagesProcessed
                           + " image(s) from "
                           + urls.size()
                           + " urls is complete");
    }

    /**
     * A factory method that makes all the image processing filters
     * for the @a imageFuture after its downloaded.
     */
    private CompletableFuture<List<FilterDecoratorWithImage>>
        makeFilterDecoratorsAsync (CompletableFuture<Image> imageFuture) {
        // Returns a new CompletionStage that, when this stage
        // completes normally, is executed with this stage's result as
        // the argument to the supplied lambda expression.
        return imageFuture
            .thenApply(image -> mFilters
                       // Convert image filters to a sequential
                       // stream.
                       .stream()

                       // Create a FilterDecoratorWithImage object for
                       // each filter/image combo.
                       .map(filter ->
                            makeFilterDecoratorWithImage(filter, image))

                       // Return a completable future to a list of
                       // FilterDecoratorWithImage objects.
                       .collect(toList()));
    }

    /**
     * Asynchronously apply all the filters to images and store them
     * in an output file on the local computer.
     */
    private CompletableFuture<List<Image>> applyFiltersAsync
        (CompletableFuture<List<FilterDecoratorWithImage>>
         decoratedFiltersWithImageFuture) {
        // Return a CompletableFuture to a list of images that will be
        // available when all the CompletableFutures in listOfFutures
        // complete.
        return decoratedFiltersWithImageFuture
            .thenCompose(list ->
                         list
                         // Converts list of FilterDecoratorWithImage
                         // objects into a sequential stream.
                         .stream()

                         // Asynchronously apply a filter to an Image.
                         .map(this::filterImageAsync)

                         // Collect the list of futures.
                         .collect(toFuture()));
    }
}
