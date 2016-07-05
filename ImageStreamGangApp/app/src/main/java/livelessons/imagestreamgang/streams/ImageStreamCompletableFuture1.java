package livelessons.imagestreamgang.streams;

import android.util.Log;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import livelessons.imagestreamgang.filters.Filter;
import livelessons.imagestreamgang.utils.Image;
import livelessons.imagestreamgang.utils.NetUtils;

/**
 * Customizes ImageStream to use Java 8 CompletableFutures to
 * download, process, and store images concurrently.
 */
public class ImageStreamCompletableFuture1
       extends ImageStream {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture1(Filter[] filters,
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
        getInput()
            // Concurrently process each URL in the input List.
            .parallelStream()

            // Filter out URLs that are already cached.
            .filter(this::urlNotCached)

            // Submit the URLs for asynchronous downloading.
            .map(url ->
                 CompletableFuture.supplyAsync(() ->
                                               makeImage(url),
                                               getExecutor()))
            // Wait for all async operations to finish.
            .map(CompletableFuture::join)

            // Map each image to a stream containing the filtered
            // versions of the image.
            .flatMap(this::applyFilters)

            // Terminate the stream.
            .collect(Collectors.toList());
    }

    /**
     * Apply the filters in parallel to each @a image.
     */
    private Stream<Image> applyFilters(Image image) {
        return mFilters.parallelStream()
            // Create a FilterDecoratorWithImage for each filter/image
            // combo.
            .map(filter -> makeFilterDecoratorWithImage(filter, image))

            // Asynchronously filter the image and store it in an
            // output file.
            .map(decoratedFilterWithImage -> 
                 CompletableFuture.supplyAsync(decoratedFilterWithImage::run,
                                               getExecutor()))
            // Wait for all async operations to finish.
            .map(CompletableFuture::join);
    }
}
