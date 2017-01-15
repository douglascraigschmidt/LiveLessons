package livelessons.streams;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import livelessons.filters.Filter;
import livelessons.filters.FilterDecoratorWithImage;
import livelessons.utils.Image;
import livelessons.utils.StreamsUtils;

/**
 * Customizes ImageStreamGang to use a Java 8 parallelstream to download,
 * process, and store images concurrently.  A parallelstream uses the
 * default ForkJoinPool, which has as many threads there are processors,
 * as returned by Runtime.getRuntime().availableProcessors().  The size
 * of the pool can be changed using Java system properties.
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
        List<Image> filteredImages = getInput()
            // Concurrently process each URL in the input List.
            .parallelStream()

            // Use filter() to ignore URLs that are already cached locally,
            // i.e., only download non-cached images.
            .filter(StreamsUtils.not(this::urlCached))

            // Use map() to transform each URL to an image (e.g., download
            // each image via its URL).
            .map(ImageStreamGang::downloadImage)

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

           // Use map() to create an OutputFilterDecorator for each image.
           .map(filter -> makeFilterDecoratorWithImage(filter, image))

           // Use map() to apply the image filter to each image and store
           // it in an output file.
           .map(FilterDecoratorWithImage::run);
    }
}
