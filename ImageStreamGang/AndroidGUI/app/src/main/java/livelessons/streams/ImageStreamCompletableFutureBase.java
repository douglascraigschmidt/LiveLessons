package livelessons.streams;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import livelessons.filters.Filter;
import livelessons.filters.FilterDecoratorWithImage;
import livelessons.utils.Image;

/**
 * Super class that factors out common code so that implementation
 * strategies can use the Java 8 CompletableFutures framework to
 * download, process, and store images asynchronously.
 */
public abstract class ImageStreamCompletableFutureBase
       extends ImageStreamGang {
    /**
     * Constructor initializes the superclass.
     */
    public ImageStreamCompletableFutureBase(Filter[] filters,
                                            Iterator<List<URL>> urlListIterator) {
        super(filters, urlListIterator);
    }

    /**
     * Asynchronously filter the image and store it in an output file.
     * Returns a CompletableFuture that completes when the image has
     * been filtered and stored.
     * @param filterDecoratorWithImage
     */
    protected CompletableFuture<Image> filterImageAsync
        (FilterDecoratorWithImage filterDecoratorWithImage) {
        // Asynchronously filter the image and store it in an output
        // file.
        return CompletableFuture
            .supplyAsync(filterDecoratorWithImage::run,
                         getExecutor());
    }
}
