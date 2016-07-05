package livelessons.imagestreamgang.streams;

import android.util.Log;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import livelessons.imagestreamgang.filters.Filter;
import livelessons.imagestreamgang.filters.FilterDecoratorWithImage;
import livelessons.imagestreamgang.utils.Image;

/**
 * Customizes ImageStream to use a Java 8 parallelstream to
 * download, process, and store images concurrently.  A
 * parallelstream uses the default ForkJoinPool, which has as
 * many threads there are processors, as returned by
 * Runtime.getRuntime().availableProcessors().  The size of the
 * pool can be changed using system properties.
 */
public class ImageStreamParallel 
       extends ImageStream {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamParallel(Filter[] filters,
                               Iterator<List<URL>> urlListIterator,
                               Runnable completionHook) {
        super(filters, urlListIterator, completionHook);
    }

    /**
     * Perform the ImageStream processing, which uses a Java 8
     * parallelstream to download, process, and store images
     * concurrently.
     */
    @Override
    protected void processStream() {
        getInput()
            // Concurrently process each URL in the input List.
            .parallelStream()

            // Filter out URLs that are already cached.
            .filter(this::urlNotCached)

            // Transform URL -> Image (download each image via
            // its URL).
            .map(this::makeImage)

            // Map each image to a stream containing the filtered
            // versions of the image.
            .flatMap(this::applyFilters)

            // Terminate the stream.
            .collect(Collectors.toList());
    }

    /**
     * Apply all the filters in parallel to each @a image.
     */
    private Stream<Image> applyFilters(Image image) {
        return mFilters
            // Apply each filter concurrently.
            .parallelStream()

            // Create an OutputDecoratedFilter for each image.
            .map(filter -> makeFilterDecoratorWithImage(filter, image))

            // Filter the image and store it in an output file.
            .map(FilterDecoratorWithImage::run);
    }
}
