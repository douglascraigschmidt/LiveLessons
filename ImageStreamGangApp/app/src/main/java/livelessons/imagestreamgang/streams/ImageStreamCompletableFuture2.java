package livelessons.imagestreamgang.streams;

import android.util.Log;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import livelessons.imagestreamgang.filters.Filter;
import livelessons.imagestreamgang.filters.FilterDecoratorWithImage;
import livelessons.imagestreamgang.utils.FutureUtils;
import livelessons.imagestreamgang.utils.Image;

import static java.util.stream.Collectors.toList;

/**
 * Customizes ImageStream to use Java 8 CompletableFutures to
 * download, process, and store images concurrently.
 */
public class ImageStreamCompletableFuture2
       extends ImageStream {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture2(Filter[] filters,
                                        Iterator<List<URL>> urlListIterator,
                                        Runnable completionHook) {
        super(filters, urlListIterator, completionHook);
    }

    /**
     * Perform the ImageStream processing, which uses Java 8
     * CompletableFutures to download, process, and store images
     * concurrently.
     */
    @Override
    protected void processStream() {
        final List<CompletableFuture<List<CompletableFuture<Image>>>> listOfFutures = getInput()
            // Concurrently process each URL in the input List.
            .parallelStream()

            // Only include URLs that have not been already cached.
            .filter(not(this::urlCached))

            // Submit non-cached URLs for asynchronous downloading,
            // which returns a stream of unfiltered Image futures.
            .map(this::makeImageAsync)

            // After each future completes then apply the
            // makeFilterDecoratorWithImage() method, which returns a
            // List of FilterDecoratorWithImage objects stored in a
            // future.
            .map(imageFuture ->
                 imageFuture.thenApply(this::makeFilterDecoratorsWithImage))

            // After each future completes then compose the results
            // with the applyFiltersAsync() method, which returns a
            // list of filtered Image futures.
            .map(listFilterDecoratorsFuture ->
                 listFilterDecoratorsFuture.thenCompose(this::applyFiltersAsync))

            // Terminate the stream, which returns a List of futures
            // to filtered Image futures.
            .collect(toList());

        // Wait for all operations associated with the futures to
        // complete.
        final CompletableFuture<List<List<CompletableFuture<Image>>>> allImagesDone =
                FutureUtils.joinAll(listOfFutures);
        try {
            // The call to get() is essential here since it blocks the
            // calling thread until all the futures have been
            // completed.
            long count = allImagesDone.get().stream().count();

            Log.d(TAG,
                  "processing of "
                  + count
                  + " image(s) is complete");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Asynchronously download an Image from the @a url parameter.
     */
    private CompletableFuture<Image> makeImageAsync(URL url) {
        // Asynchronously download an Image from the url parameter.
        return CompletableFuture.supplyAsync(() -> makeImage(url),
                                             getExecutor());
    }

    /**
     * Create a List of FilterDecoratorWithImage objects corresponding
     * to the @a image parameter.
     */
    private List<FilterDecoratorWithImage> makeFilterDecoratorsWithImage(Image image) {
        return mFilters
            // Iterate through all the configured filters.
            .stream()

            // Create an OutputDecoratedFilter for each image.
            .map(filter -> makeFilterDecoratorWithImage(filter, image))

            // Return a list of FilterDecoratorWithImage objects.
            .collect(toList());
    }

    /**
     * Asynchronously apply all the filters to each image.
     */
    private CompletableFuture<List<CompletableFuture<Image>>> applyFiltersAsync
                (List<FilterDecoratorWithImage> decoratedFiltersWithImage) {
        List<CompletableFuture<Image>> listOfFutures = decoratedFiltersWithImage
            // Iterate through all the configured filters.
            .stream()

            // Asynchronously apply a filter to an Image.
            .map(decoratedFilterWithImage ->
                 CompletableFuture.supplyAsync(decoratedFilterWithImage::run,
                                               getExecutor()))

            // Collect the list of futures.
            .collect(toList());

        // Create a future to hold the results.
        CompletableFuture<List<CompletableFuture<Image>>> future =
            new CompletableFuture<>();
        future.complete(listOfFutures);
        return future;
    }
}
