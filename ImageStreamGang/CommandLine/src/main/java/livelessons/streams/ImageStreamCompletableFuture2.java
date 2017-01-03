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

/**
 * Customizes the ImageStreamCompletableFutureBase super class to
 * download, process, and store images concurrently and
 * asynchronously.
 */
public class ImageStreamCompletableFuture2
       extends ImageStreamCompletableFutureBase {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture2(Filter[] filters,
                                        Iterator<List<URL>> urlListIterator,
                                        Runnable completionHook) {
        super(filters, urlListIterator, completionHook);
    }

    /**
     * Use Java 8 CompletableFutures to download, process, and store
     * images concurrently.
     */
    @Override
    protected void processStream() {
        final List<CompletableFuture<List<Image>>> listOfFutures = getInput()
            // Process each URL in the input List.
            .stream()

            // Only include URLs that have not been already cached.
            .filter(StreamsUtils.not(this::urlCached))

            // Submit non-cached URLs for asynchronous downloading,
            // which returns a stream of unfiltered Image futures.
            .map(this::downloadImageAsync)

            // After each future completes then call the
            // makeFilterDecorators() method reference, which returns a
            // List of FilterDecoratorWithImage objects stored in a
            // future.
            .map(this::makeFilterDecorators)

            // After each future completes then call the applyFiltersAsync()
            // method reference, which returns a list of filtered Image futures.
            .map(this::applyFiltersAsync)
            
            // Terminate the stream, which returns a List of futures
            // to filtered Image futures.
            .collect(toList());

        // Create a CompletableFuture that can be used to wait for all
        // operations associated with the futures to complete.
        CompletableFuture<List<List<Image>>> allImagesDone =
                StreamsUtils.joinAll(listOfFutures);
        // The call to join() is needed here to blocks the calling
        // thread until all the futures have been completed.
        Integer imagesProcessed =
            allImagesDone.join()
                         .stream()
                         .collect(summingInt(List::size));

        System.out.println(TAG
                           + ": processing of "
                           + imagesProcessed
                           + " image(s) is complete");
    }


    /**
     * Apply the filters in parallel to each @a image.
     */
    private CompletableFuture<List<FilterDecoratorWithImage>> makeFilterDecorators
                (CompletableFuture<Image> imageFuture) {
        // Returns a new CompletionStage that, when this stage
        // completes normally, is executed with this stage's result as
        // the argument to the supplied lambda expression.
        return imageFuture.thenApply(image -> mFilters
            // Iterate through all the configured filters.
            .stream()

            // Create an OutputDecoratedFilter for each image.
            .map(filter -> makeFilterDecoratorWithImage(filter, image))

            // Return a list of FilterDecoratorWithImage objects.
            .collect(toList()));
    }

    /**
     * Asynchronously apply all the filters to an image and store it
     * in an output file.
     */
    private CompletableFuture<List<Image>> applyFiltersAsync
      (CompletableFuture<List<FilterDecoratorWithImage>> decoratedFiltersWithImageFuture) {
        // Returns a new CompletionStage that, when this stage
        // completes normally, is executed with this stage's result as
        // the argument to the supplied lambda expression.
        return decoratedFiltersWithImageFuture.thenCompose(list -> {
                List<CompletableFuture<Image>> listOfFutures = list
                    // Iterate through all the configured filters.
                    .stream()

                    // Asynchronously apply a filter to an Image.
                    .map(this::filterImageAsync)

                    // Collect the list of futures.
                    .collect(toList());

                // Return a CompletableFuture to a list of Image
                // objects that will be available when all the
                // CompletableFutures in listOfFutures complete.
                return StreamsUtils.joinAll(listOfFutures);
            });
    }
}
