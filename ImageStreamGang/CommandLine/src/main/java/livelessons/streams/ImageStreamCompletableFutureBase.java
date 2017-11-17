package livelessons.streams;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import livelessons.filters.Filter;
import livelessons.filters.FilterDecoratorWithImage;
import livelessons.utils.Image;

import static livelessons.utils.FuturesCollectorStream.toFuture;

/**
 * Super class that factors out common code so that implementation
 * strategies can use the Java 8 CompletableFutures framework to
 * download, process, and store images asynchronously.
 */
abstract class ImageStreamCompletableFutureBase
         extends ImageStreamGang {
    /**
     * Constructor initializes the superclass.
     */
    ImageStreamCompletableFutureBase(Filter[] filters,
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
    CompletableFuture<URL> checkUrlCachedAsync(URL url) {
        return CompletableFuture
            // Asynchronously check if the URL is cached.
            .supplyAsync(() -> urlCached(url) ? null : url,
                         getExecutor());
            
    }


    /**
     * Log the results.
     * 
     * @param resultsStream A stream of images that have been
     * downloaded, processed, and stored
     * @param urlsSize The number of URLs to download
     */
    protected void logResults(Stream<Image> resultsStream,
                              int urlsSize) {
        // Print the results to the log.
        System.out
            .println(TAG
                     + ": processing of "
                     + resultsStream
                     // Count the number of elements in the flattened
                     // stream.
                     .count()
                     + " image(s) from "
                     + urlsSize
                     + " urls is complete");
    }
}
