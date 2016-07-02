package imagestream;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import filters.Filter;
import filters.OutputFilterDecorator;

/**
 * @class ImageStreamSequential
 *
 * @brief Customizes ImageStream to use a Java 8 stream to download,
 *        process, and store images sequentially.
 */
public class ImageStreamSequential extends ImageStream {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamSequential(Filter[] filters,
                                 Iterator<List<URL>> urlListIterator,
                                 Runnable completionHook) {
        super(filters, urlListIterator, completionHook);
    }

    /**
     * Initiate the ImageStream processing, which uses a Java 8 stream
     * to download, process, and store images sequentially.
     */
    @Override
    protected void initiateStream() {
        // Create a new barrier for this iteration cycle.
        mIterationBarrier = new CountDownLatch(1);

        // Sequentially process each URL in the input List.
        getInput().stream()
            // Transform URL -> Image (download each image via
            // its URL).
            .map(url -> makeImage(url))
            // Collect each image and apply each filter sequentially
            // (similar to nested for loops).
            .forEach(image -> {
                    // Apply each filter to each image.
                    mFilters.stream()
                        // Decorate each filter to write the image to
                        // a file.
                        .map(filter -> new OutputFilterDecorator(filter))
                        // Filter image and store in output file.
                        .forEach(decoratedFilter -> 
                                 decoratedFilter.filter(image));
	    	});

        // Indicate all computations in this iteration are done.
        try {
            mIterationBarrier.countDown();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } 
    }
}
