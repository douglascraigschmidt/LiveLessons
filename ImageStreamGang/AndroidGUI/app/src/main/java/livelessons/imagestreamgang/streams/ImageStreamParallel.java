package livelessons.imagestreamgang.streams;

import android.util.Log;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import livelessons.imagestreamgang.filters.Filter;
import livelessons.imagestreamgang.filters.FilterDecoratorWithImage;
import livelessons.imagestreamgang.utils.BlockingTask;
import livelessons.imagestreamgang.utils.Image;
import livelessons.imagestreamgang.utils.StreamsUtils;

/**
 * Customizes ImageStream to use a Java 8 parallelstream to
 * download, process, and store images concurrently.  A
 * parallelstream uses the default ForkJoinPool, which has as
 * many threads there are processors, as returned by
 * Runtime.getRuntime().availableProcessors().  The size of the
 * pool can be changed using system properties.
 */
public class ImageStreamParallel 
       extends ImageStreamGang {
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
     * parallel stream to download, process, and store images
     * concurrently.
     */
    @Override
    protected void processStream() {
        // Get the list of URLs.
        List<URL> urls = getInput();

        List<Image> filteredImages = urls
                // Concurrently process each URL in the input List.
                .parallelStream()

                // Only include URLs that have not been already cached.
                .filter(StreamsUtils.not(this::urlCached))

                // Transform URL -> Image (download each image via
                // its URL).
                .map(url ->
                     // This call ensures the common fork/join thread pool
                     // is expanded to handle the blocking image download.
                     BlockingTask.callInManagedBlock(()
                                                     -> ImageStreamGang.downloadImage(url)))

                // Map each image to a stream containing the filtered
                // versions of the image.
                .flatMap(this::applyFilters)

                // Terminate the stream.
                .collect(Collectors.toList());

        Log.d(TAG, "processing of "
                + filteredImages.size()
                + " image(s) is complete");
    }

    /**
     * Apply all the filters in parallel to each @a image.
     */
    private Stream<Image> applyFilters(Image image) {
        return mFilters
            // Apply each filter concurrently.
            .parallelStream()

           // Use map() to create an OutputFilterDecorator for each
           // image and run it to filter each image and store it in an
           // output file.
           .map(filter
                -> makeFilterDecoratorWithImage(filter, image).run())
    }
}
