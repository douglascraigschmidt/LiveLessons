package livelessons.streams;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import livelessons.filters.Filter;
import livelessons.utils.Image;
import livelessons.utils.StreamsUtils;
import org.reactivestreams.Publisher;

/**
 * This implementation strategy customizes ImageStreamGang to use
 * RxJava to download, process, and store images concurrently.  This
 * implementation uses Java's common fork-join pool, which has as many
 * threads as there are processors, as returned by
 * Runtime.getRuntime().availableProcessors().  The size of this
 * common fork-join pool can be changed via Java system properties.
 */
public class ImageStreamRxJava
       extends ImageStreamGang {
    /**
     * Constructor initializes the superclass.
     */
    public ImageStreamRxJava(Filter[] filters,
                               Iterator<List<URL>> urlListIterator) {
        super(filters, urlListIterator);
    }

    /**
     * Perform the ImageStreamGang processing, which uses RxJava to
     * download, process, and store images concurrently.
     */
    @Override
    protected void processStream() {
        // Get the list of URLs.
        List<URL> urls = getInput();

        Observable
            // Convert the URLs in the input list into a stream of
            // observables.
            .fromIterable(urls)

            // Transforms an observable by applying a set of
            // operations to each item emitted by the source.
            .flatMap(url -> {
                    return Observable
                        // Just omit this one object.
                        .just(url)

                        // Run this flow of operations in the common fork-join
                        // pool.
                        .compose(applySchedulers())

                        // Use filter() to ignore URLs that are already cached
                        // locally, i.e., only download non-cached images.
                        .filter(lUrl -> !urlCached(lUrl))

                        // Transform URL to an Image by downloading each image via
                        // its URL.  This call ensures the common fork/join thread
                        // pool is expanded to handle the blocking image download.
                        .map(this::blockingDownload)

                        // Use flatMap() to create a stream containing multiple
                        // filtered versions of each image.
                        .<Image>flatMap(this::applyFilters);
                })

            // Collect the downloaded images into a list.
            .collectInto(new ArrayList<Image>(), List::add)

            // Print the statistics for this test run in a blocking
            // manner.
            .blockingSubscribe(filteredImages -> System.out.println(TAG
                               + ": processing of "
                               + filteredImages.size()
                               + " image(s) from "
                               + urls.size()
                               + " urls is complete"));
    }

    /**
     * Apply all the image filters concurrently to each {@ image}
     * @return A stream of filtered images
     * @return
     */
    private Observable<Image> applyFilters(Image image) {
        return Observable
            // Convert the filters in the input list into a stream of
            // observables.
            .fromIterable(mFilters)

            // Transforms an observable by applying a set of
            // operations to each item emitted by the source.
            .flatMap(filter -> Observable
                     // Just omit this one object.
                     .just(filter)

                     // Run this flow of operations in the common
                     // fork-join pool.
                     .compose(applySchedulers())

                     // Use map() to create an OutputFilterDecorator
                     // for each image and run it to filter each image
                     // and store it in an output file.
                     .map(lFilter ->
                          makeFilterDecoratorWithImage(lFilter, image).run()));
    }

    /**
     * @return Schedule an observable to run on the common fork-join
     * pool.
     */
    <T> ObservableTransformer<T, T> applySchedulers() {
        return observable -> observable
            .subscribeOn(Schedulers.from(ForkJoinPool.commonPool()));
    }
}
