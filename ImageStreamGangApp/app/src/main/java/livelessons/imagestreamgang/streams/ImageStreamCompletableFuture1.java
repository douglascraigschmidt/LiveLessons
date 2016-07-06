package livelessons.imagestreamgang.streams;

import android.util.Log;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import livelessons.imagestreamgang.filters.Filter;
import livelessons.imagestreamgang.utils.Image;

/**
 * Customizes ImageStream to use Java 8 CompletableFutures to download, process,
 * and store images concurrently.
 */
public class ImageStreamCompletableFuture1
        extends ImageStream {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture1(
            Filter[] filters,
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
        List<Image> collect = getInput()
                // Concurrently process each URL in the input List.
                .parallelStream()

                // Only include URLs that have not been already cached.
                .filter(not(this::urlCached))

                // Submit the URLs for asynchronous downloading.
                .map(this::makeImageAsync)

                // Wait for all async operations to finish.
                .map(CompletableFuture::join)

                // Map each image to a stream containing the filtered
                // versions of the image.
                .flatMap(this::applyFilters)

                // Terminate the stream.
                .collect(Collectors.toList());

        Log.d(TAG, "processing of "
                + (collect != null ? collect.size() : "0")
                + " image(s) is complete");
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
                             CompletableFuture.supplyAsync(
                                     decoratedFilterWithImage::run,
                                     getExecutor()))
                // Wait for all async operations to finish.
                .map(CompletableFuture::join);
    }

    /**
     * Asynchronously download an Image from the @a url parameter.
     */
    private CompletableFuture<Image> makeImageAsync(URL url) {
        // Asynchronously download an Image from the url parameter.
        return CompletableFuture.supplyAsync(() -> makeImage(url),
                                             getExecutor());
    }
}
