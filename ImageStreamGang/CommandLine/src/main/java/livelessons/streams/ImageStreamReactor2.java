package livelessons.streams;

import livelessons.filters.Filter;
import livelessons.utils.Image;
import livelessons.utils.ReactorUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * This implementation strategy customizes ImageStreamGang to use
 * Project Reactor parallel flowables to download, process, and store
 * images concurrently.  This implementation uses Java's common
 * fork-join pool, which has as many threads as there are processors,
 * as returned by Runtime.getRuntime().availableProcessors().  The
 * size of this common fork-join pool can be changed dynamically via
 * Java's ManagedBlocker mechanism.
 */
public class ImageStreamReactor2
       extends ImageStreamGang {
    /**
     * Constructor initializes the superclass.
     */
    public ImageStreamReactor2(Filter[] filters,
                              Iterator<List<URL>> urlListIterator) {
        super(filters, urlListIterator);
    }

    /**
     * Perform the ImageStreamGang processing, which uses Project
     * Reactor ParallelFlux capability to download, process, and store
     * images concurrently.
     */
    @Override
    protected void processStream() {
        // Get the list of URLs.
        List<URL> urls = getInput();

        List<Image> filteredImages = Flux
            // Convert collection into a flux.
            .fromIterable(urls)

            // Create a parallel flux.
            .parallel()

            // Run this flow of operations in the elastic thread pool.
            .runOn(Schedulers.boundedElastic())

            // Use filter() to ignore URLs that are already cached
            // locally, i.e., only download non-cached images.
            .filter(url -> !urlCached(url))

            // Transform each URL to an image by downloading it via
            // blockingDownload(), which expands the common fork/join
            // thread pool to handle the blocking image download.
            .map(this::blockingDownload)

            // Use flatMap() to create a stream containing multiple
            // filtered versions of each image.
            .flatMap(this::applyFilters)

            // Merge the parallel flow into a sequential flow and
            // return this result.
            .sequential()

            // Collect the downloaded and filtered images into a list.
            .collectList()

            // Get statistics in a blocking manner.
            .block();

        assert filteredImages != null;
        // Print the statistics.
        System.out.println(TAG
                           + ": processing of "
                           + filteredImages.size()
                           + " image(s) from "
                           + urls.size()
                           + " urls is complete");
    }

    /**
     * Apply all the image filters concurrently to each {@code image}
     * @return A stream of filtered images
     */
    private Flux<Image> applyFilters(Image image) {
        return ReactorUtils
            // Convert the filters in the input list into a parallel flux stream.
            .fromIterableParallel(mFilters)

            // Use map() to create an OutputFilterDecorator for each
            // image and run it to filter each image and store it in
            // an output file.
            .map(filter ->
                 makeFilterDecoratorWithImage(filter, image).run())
               
            // Merge the parallel flow into a sequential flow and
            // return this result.
            .sequential();
    }
}
