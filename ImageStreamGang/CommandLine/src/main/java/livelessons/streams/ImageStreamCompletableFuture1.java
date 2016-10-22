package livelessons.streams;

import livelessons.filters.Filter;

import static java.util.stream.Collectors.toList;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import livelessons.utils.Image;
import livelessons.utils.StreamsUtils;
import livelessons.filters.FilterDecoratorWithImage;

/**
 * Customizes ImageStream to use Java 8 CompletableFutures to
 * download, process, and store images concurrently.
 */
public class ImageStreamCompletableFuture1
       extends ImageStreamCompletableFutureBase {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture1(Filter[] filters,
                                         Iterator<List<URL>> urlListIterator,
                                         Runnable completionHook) {
        super(filters, urlListIterator, completionHook);
    }

    /**
     * Perform the ImageStream processing, which uses Java 8 CompletableFutures
     * to download, process, and store images concurrently.
     */
    @Override
    protected void processStream() {
        // Create a list of futures.
        List<CompletableFuture<Image>> listOfFutures = getInput()
            // Concurrently process each URL in the input List.
            .stream()

            // Only include URLs that have not been already cached.
            .filter(StreamsUtils.not(this::urlCached))

            // Submit the URLs for asynchronous downloading.
            .map(this::downloadImageAsync)

            // Map each image to a stream containing the filtered
            // versions of the image.
            .flatMap(this::applyFiltersAsync)

            // Terminate the stream.
            .collect(toList());

        // Create a CompletableFuture that can be used to wait for all
        // operations associated with the futures to complete.
        CompletableFuture<List<Image>> allImagesDone =
                StreamsUtils.joinAll(listOfFutures);

        // Print the results.
        System.out.println(TAG 
                           + ": processing of "
                           + allImagesDone.join().size()
                           + " image(s) is complete");
    }

    /**
     * Apply filters concurrently to each @a image and store the
     * results in output files.
     */
    private Stream<CompletableFuture<Image>> applyFiltersAsync(CompletableFuture<Image> imageFuture) {
        return mFilters.stream()
            // Create a FilterDecoratorWithImage for each filter/image
            // combo.
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
                 // Returns a new CompletionStage that, when this stage
                 // completes normally, is executed with this stage's result as
                 // the argument to the supplied lambda expression.
                 filterFuture.thenCompose(filter ->
                                          CompletableFuture.supplyAsync(filter::run,
                                                                        getExecutor())));
    }
}
