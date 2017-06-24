package livelessons.streams;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import livelessons.filters.Filter;
import livelessons.filters.FilterDecoratorWithImage;
import livelessons.utils.Image;
import livelessons.utils.BlockingTask;
import livelessons.utils.StreamsUtils;

/**
 * This implementation strategy customizes ImageStreamGang to use a
 * Java 8 parallelstream to download, process, and store images
 * concurrently.  A parallelstream uses the global Java ForkJoinPool,
 * which has as many threads as there are processors, as returned by
 * Runtime.getRuntime().availableProcessors().  The size of this
 * global thread pool can be changed via Java system properties.
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
     * Perform the ImageStreamGang processing, which uses a Java 8 parallel
     * stream to download, process, and store images concurrently.
     */
    @Override
    protected void processStream() {
        // Get the list of URLs.
        List<URL> urls = getInput();

        List<Image> filteredImages = urls
            // Convert the URLs in the input list into a stream and
            // process them concurrently.
            .parallelStream()

            // Use filter() to ignore URLs that are already cached locally,
            // i.e., only download non-cached images.
            .filter(StreamsUtils.not(this::urlCached))

            // Use map() to transform each URL to an image (i.e.,
            // synchronously download each image via its URL).
            .map(url ->
                 // This call ensures the common fork/join thread pool
                 // is expanded to handle the blocking image download.
                 BlockingTask.callInManagedBlock(() 
                                                 -> ImageStreamGang.downloadImage(url)))

            // Use flatMap() to create a stream containing multiple filtered
            // versions of each image.
            .flatMap(this::applyFilters)

            // Terminate the stream and collect the results into
            // list of images.
            .collect(Collectors.toList());

        System.out.println(TAG
                           + ": processing of "
                           + filteredImages.size()
                           + " image(s) is complete");
    }

    /**
     * Apply all the image filters concurrently to each @a image.
     */
    private Stream<Image> applyFilters(Image image) {
        return mFilters
           // Iterate through the list of image filters concurrently and
           // apply each one to the image.
           .parallelStream()

           // Use map() to create an OutputFilterDecorator for each
           // image and run it to filter each image and store it in an
           // output file.
           .map(filter
                -> makeFilterDecoratorWithImage(filter, image).run());
    }
}
