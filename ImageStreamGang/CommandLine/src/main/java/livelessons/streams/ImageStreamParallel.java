package livelessons.streams;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import livelessons.filters.Filter;
import livelessons.utils.Image;

/**
 * This implementation strategy extends the ImageStreamGang super
 * class to use Java's parallel streams framework to download,
 * process, and store images in parallel.  This framework uses Java's
 * common fork-join pool, which has one less than the number of
 * processor cores returned by
 * Runtime.getRuntime().availableProcessors().  The size of this
 * common fork-join pool can be changed dynamically via Java's
 * ManagedBlocker mechanism.
 */
public class ImageStreamParallel 
       extends ImageStreamGang {
    /**
     * Constructor initializes the superclass.
     */
    public ImageStreamParallel(Filter[] filters,
                               Iterator<List<URL>> urlListIterator) {
        super(filters, urlListIterator);
    }

    /**
     * Perform the ImageStreamGang processing, which uses Java's
     * parallel streams framework to download, process, and store
     * images concurrently.
     */
    @Override
    protected void processStream() {
        // Get the list of URLs.
        List<URL> urls = getInput();

        List<Image> filteredImages = urls
            // Convert the URLs in the input list into a stream and
            // process them in parallel.
            .parallelStream()

            // Use filter() to ignore URLs that are already cached
            // locally, i.e., only download non-cached images.
            .filter(Predicate.not(this::urlCached))

            // Transform URL to an Image by downloading each image via
            // its URL.  This call ensures the common fork/join thread
            // pool is expanded to handle the blocking image download.
            .map(this::blockingDownload)

            // Use mapMulti() to create a stream containing multiple
            // filtered versions of each image.
            .mapMulti(this::applyFilters)

            // Terminate the stream and collect the results into List
            // of images.
            .toList();

        System.out.println(TAG
                           + ": processing of "
                           + filteredImages.size()
                           + " image(s) from "
                           + urls.size() 
                           + " urls is complete");
    }

    /**
     * Apply all the filters concurrently to each {@link Image}
     * and accept the filtered results into the {@link Consumer}.
     *
     * @param image The image to apply all the filters to.
     * @param consumer The {@link Consumer} that accepts all
     *                 the filtered {@link Image} objects
     */
    private void applyFilters(Image image,
                              Consumer<Image> consumer) {
        // Apply the Image filters concurrently to each image.
        mFilters
            // Iterate through the list of image filters concurrently
            // and apply each one to the image.
            .parallelStream()

            // Use map() to create an OutputFilterDecorator for each
            // image and run it to filter each image and store it in an
            // output file.
            .map(filter ->
                makeFilterDecoratorWithImage(filter, image).run())

            // Iterate through the list of filtered images and accept
            // each one into the consumer.
            .forEach(consumer);
    }
}
