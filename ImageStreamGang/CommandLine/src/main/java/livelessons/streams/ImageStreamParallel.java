package livelessons.streams;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
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

            // Use map() to create a stream containing multiple
            // filtered versions of each image.
            .map(this::applyFilters)

            // Convert the stream of streams of images into a stream
            // of images without using flatMap().
            .reduce(Stream::concat).orElse(Stream.empty())

            // Terminate the stream and collect the results into list
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
     * Apply all the image filters concurrently to each {@code image}
     * @return a stream of filtered images
     */
    private Stream<Image> applyFilters(Image image) {
        return mFilters
           // Iterate through the list of image filters concurrently
           // and apply each one to the image.
           .parallelStream()

           // Use map() to create an OutputFilterDecorator for each
           // image and run it to filter each image and store it in an
           // output file.
           .map(filter ->
                makeFilterDecoratorWithImage(filter, image).run());
    }
}
