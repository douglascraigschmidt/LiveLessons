package livelessons.streams;

import livelessons.filters.Filter;
import livelessons.utils.Image;
import livelessons.utils.StreamsUtils;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Stream;

import static livelessons.utils.FuturesCollectorStream.toFuture;

/**
 * This is another asynchronous implementation strategy that
 * customizes the ImageStreamCompletableFutureBase super class to
 * download, process, and store images asynchronously in the
 * designated thread pool.
 */
public class ImageStreamCompletableFuture2
       extends ImageStreamCompletableFutureBase {
    /**
     * Maximum number of threads in a fixed-size thread pool.
     */
    private final int sMAX_THREADS = 100;

    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture2(Filter[] filters,
                                         Iterator<List<URL>> urlListIterator) {
        super(filters, 
              urlListIterator);
    }

    /**
     * A hook method that's also a template method.  It sets the
     * executor to a fixed-size thread pool and calls up to the
     * superclass start the processing.
     */
    @Override
    protected void initiateStream() {
        // The thread pool size is the smaller of (1) the number of
        // filters times the number of images to download and (2)
        // sMAX_THREADS (which prevents allocating excessive threads).
        int threadPoolSize = Math.min(mFilters.size() * getInput().size(),
                                      sMAX_THREADS);

        // Initialize the Executor with appropriate pool of threads.
        setExecutor(Executors.newFixedThreadPool(threadPoolSize));

        // Call up to superclass to start the processing.
        super.initiateStream();
    }

    /**
     * This hook method uses Java 8 CompletableFutures to download,
     * process, and store images in asynchronously.
     */
    @Override
    protected void processStream() {
        // Get the input URLs.
        List<URL> urls = getInput();

        // A future to a stream of URLs.
        CompletableFuture<Stream<URL>> urlStreamFuture = urls
            // Convert the URLs in the input list into a sequential
            // stream.
            .stream()

            // Use map() to ignore URLs that are already cached
            // locally, i.e., only download non-cached images.
            .map(this::checkUrlCachedAsync)

            // Trigger intermediate processing and create a future to
            // a stream of URLs.
            .collect(toFuture());

        urlStreamFuture
            // thenAccept() is called when all the futures in the
            // stream complete their processing.
            .thenAccept(stream -> {
                    // Create a stream of completable futures to filtered images.
                    Stream<CompletableFuture<Stream<Image>>> futureStream = stream
                        // Remove all cached URLs.
                        .filter(Objects::nonNull)

                        // Use map() to call downloadImageAsync(),
                        // which transforms each URL to a completable
                        // future to an image (i.e., asynchronously
                        // download each image via its URL).
                        .map(this::downloadImageAsync)

                        // Use map() to call applyFiltersAsync(), which
                        // creates a stream of completable futures to multiple
                        // filtered/stored versions of each image.
                        .map(this::applyFiltersAsync);

                    StreamsUtils
                        // Create a future that is used to wait for
                        // all futures in futureStream to complete.
                        .joinAllStream(futureStream)

                        // thenAccept() is called when all the futures
                        // in the stream complete their processing.
                        .thenAccept(resultsStream ->
                                    // Log the results.
                                    logResults(resultsStream, urls.size()))
                        
                        // Wait until all images have been downloaded,
                        // processed, and stored.
                        .join();
                })
            
            // Wait until all images have been downloaded, processed,
            // and stored.
            .join();
    }

    /**
     * Asynchronously download an image from the {@code url} parameter.
     *
     * @param url The URL to download
     * @return A future that completes when the image finishes downloading
     */
    private CompletableFuture<Image> downloadImageAsync(URL url) {
        // Return a future that completes when the image finishes
        // downloading.
        return CompletableFuture
            // Use the executor to asynchronously download an image
            // when urlFuture completes.
            .supplyAsync(() -> downloadImage(url),
                         getExecutor());
    }

    /**
     * Asynchronously apply all the filters to images and store them
     * in an output file on the local computer.
     *
     * @param imageFuture A future to an image that's being downloaded
     @ return A completable future to a stream of images that are being filtered/stored 
    */
    private CompletableFuture<Stream<Image>> applyFiltersAsync
        (CompletableFuture<Image> imageFuture) {
        // Return a completable future to a stream of images that are
        // being processed.
        return imageFuture
            // thenCompose() is called after the image download
            // completes.  It works like flatMap(), i.e., it returns a
            // future to a stream of futures.
            .thenCompose(image -> mFilters
                         // Convert image filters to a sequential
                         // stream.
                         .stream()

                         // Use map() to asynchronously apply a filter.
                         .map(filter -> CompletableFuture
                              // Asynchronously apply filter action.
                              .supplyAsync(() -> 
                                           // Create and apply the
                                           // filter to the image.
                                           makeFilterDecoratorWithImage
                                           (filter, image).run(),
                                           // Run in designated
                                           // executor.
                                           getExecutor()))

                         // Collect a stream of futures.
                         .collect(toFuture()));
    }

    /**
     * Log the results.
     * 
     * @param resultsStream A stream of images that have been
     * downloaded, processed, and stored
     * @param urlsSize The number of URLs to download
     */
    private void logResults(Stream<Stream<Image>> resultsStream,
                            int urlsSize) {
        // Print the results to the log.
        System.out
            .println(TAG
                     + ": processing of "
                     + resultsStream
                     // Flatten the stream of streams.
                     .flatMap(Function.identity())

                     // Count the number of elements in the flattened
                     // stream.
                     .count()
                     + " image(s) from "
                     + urlsSize
                     + " urls is complete");
    }
}
