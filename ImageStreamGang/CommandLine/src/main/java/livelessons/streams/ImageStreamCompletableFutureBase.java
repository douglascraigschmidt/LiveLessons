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
     * Used to represent a null future.
     */
    protected static CompletableFuture<URL> mNullFuture =
        CompletableFuture.completedFuture(null);

    /**
     * Constructor initializes the superclass.
     */
    public ImageStreamCompletableFutureBase(Filter[] filters,
                                            Iterator<List<URL>> urlListIterator) {
        super(filters, urlListIterator);
    }

    /**
     * Asynchronously check if {@code url} is already cached.
     *
     * @param url The URL to check
     * @return A completable future to null if already cached, else a
     * completable future to a non-null value if {@code url} is not
     * already cached
     */
    protected CompletableFuture<URL> checkUrlCachedAsync(URL url) {
        return CompletableFuture
            // Asynchronously check if the URL is cached.
            .supplyAsync(() -> urlCached(url) ? null : url)

            // Return a completable future to null if url was already
            // cached, else return a completable future to a non-null
            // value if url is not already cached.
            .thenCompose(u -> u == null
                         ? mNullFuture
                         : CompletableFuture.completedFuture(u));
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
