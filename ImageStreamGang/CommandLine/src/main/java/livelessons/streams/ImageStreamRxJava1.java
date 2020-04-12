package livelessons.streams;

import io.reactivex.rxjava3.core.Observable;
import livelessons.filters.Filter;
import livelessons.utils.Image;
import livelessons.utils.RxUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This implementation strategy customizes ImageStreamGang to use
 * RxJava to download, process, and store images concurrently.  This
 * implementation uses Java's common fork-join pool, which has as many
 * threads as there are processors, as returned by
 * Runtime.getRuntime().availableProcessors().  The size of this
 * common fork-join pool can be changed via Java system properties.
 */
public class ImageStreamRxJava1
       extends ImageStreamGang {
    /**
     * Constructor initializes the superclass.
     */
    public ImageStreamRxJava1(Filter[] filters,
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

            // Transform the stream of urls by downloading and
            // filtering them in parallel.
            .flatMap(url ->
                     Observable
                     // Just omit this one object.
                     .just(url)

                     // Run this flow of operations in the common
                     // fork-join pool.
                     .compose(RxUtils.commonPoolObservable())

                     // Ignore URLs that are cached locally, i.e.,
                     // only download non-cached images.
                     .filter(lUrl -> !urlCached(lUrl))

                     // Transform each URL to an image by downloading
                     // it via blockingDownload(), which expands the
                     // common fork/join thread pool to handle the
                     // blocking image download.
                     .map(this::blockingDownload)

                     // Use flatMap() to create a stream containing
                     // multiple filtered versions of each image.
                     .flatMap(this::applyFilters))

            // Reduce the downloaded and filtered images into a list.
            .reduceWith(ArrayList<Image>::new,
                        this::append)

            // Print the statistics in a blocking manner.
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
                     .compose(RxUtils.commonPoolObservable())

                     // Use map() to create an OutputFilterDecorator
                     // for each image and run it to filter each image
                     // and store it in an output file.
                     .map(lFilter ->
                          makeFilterDecoratorWithImage(lFilter, image).run()));
    }

    /**
     * Append {@code newItem} to the end of {@code list} and return the updated list.
     */
    private <T> ArrayList<T> append(ArrayList<T> list, T newItem) {
        // Add newItem to the end of the list.
        list.add(newItem);

        // Return the updated list.
        return list;
    }


}
