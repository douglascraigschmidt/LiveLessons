package livelessons.streams;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import livelessons.filters.Filter;
import livelessons.filters.FilterDecoratorWithImage;
import livelessons.utils.Image;

import static java.util.stream.Collectors.toList;

/**
 * Base class that factors out common code and customizes ImageStream
 * to use Java 8 CompletableFutures to download, process, and store
 * images concurrently.
 */
public abstract class ImageStreamCompletableFutureBase
       extends ImageStreamGang {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFutureBase(Filter[] filters,
                                            Iterator<List<URL>> urlListIterator,
                                            Runnable completionHook) {
        super(filters, urlListIterator, completionHook);
    }

    /**
     * Asynchronously download an Image from the @a url parameter.
     */
    protected CompletableFuture<Image> makeImageAsync(URL url) {
        // Asynchronously download an Image from the url parameter.
        return CompletableFuture.supplyAsync(() -> makeImage(url),
                                             getExecutor());
    }

    /**
     * Asynchronously filter the image and store it in an output file.
     */
    protected CompletableFuture<Image> filterImageAsync
        (FilterDecoratorWithImage filterDecoratorWithImage) {
        // Asynchronously filter the image and store it in an output
        // file.
        return CompletableFuture.supplyAsync(filterDecoratorWithImage::run,
                                             getExecutor());
    }
}
