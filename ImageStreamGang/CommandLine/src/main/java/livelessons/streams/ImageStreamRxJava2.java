package livelessons.streams;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.parallel.ParallelTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import livelessons.filters.Filter;
import livelessons.utils.Image;
import livelessons.utils.RxUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * This implementation strategy customizes ImageStreamGang to use
 * RxJava parallel flowables to download, process, and store images
 * concurrently.  This implementation uses Java's common fork-join
 * pool, which has as many threads as there are processors, as
 * returned by Runtime.getRuntime().availableProcessors().  The size
 * of this common fork-join pool can be changed via Java system
 * properties.
 */
public class ImageStreamRxJava2
       extends ImageStreamGang {
    /**
     * Constructor initializes the superclass.
     */
    public ImageStreamRxJava2(Filter[] filters,
                              Iterator<List<URL>> urlListIterator) {
        super(filters, urlListIterator);
    }

    /**
     * Perform the ImageStreamGang processing, which uses RxJava
     * parallel flowables to download, process, and store images
     * concurrently.
     */
    @Override
    protected void processStream() {
        // Get the list of URLs.
        List<URL> urls = getInput();

        Flowable
            // Convert the URLs in the input list into a stream of
            // observables.
            .fromIterable(urls)

            // Run this flow of operations in parallel in the common
            // fork-join pool.
            .parallel().compose(RxUtils.commonPoolParallelFlowable())

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
            .collectInto(new ArrayList<Image>(), List::add)

            // Print statistics in a blocking manner.
            .blockingSubscribe(filteredImages ->
                               System.out.println(TAG
                                                  + ": processing of "
                                                  + filteredImages.size()
                                                  + " image(s) from "
                                                  + urls.size()
                                                  + " urls is complete"));
    }

    /**
     * Apply all the image filters concurrently to each {@code image}
     * @return A stream of filtered images
     */
    private Flowable<Image> applyFilters(Image image) {
        return Flowable
            // Convert the filters in the input list into a stream of
            // flowables.
            .fromIterable(mFilters)

            // Run this flow of operations in parallel in the common
            // fork-join pool.
            .parallel().compose(RxUtils.commonPoolParallelFlowable())

            // Use map() to create an OutputFilterDecorator for each
            // image and run it to filter each image and store it in an
            // output file.
            .map(filter ->
                 makeFilterDecoratorWithImage(filter, image).run())
               
            // Merge the parallel flow into a sequential flow and
            // return this result.
            .sequential();
    }
}
