package imagestream;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import filters.Filter;
import filters.OutputFilterDecorator;

/**
 * @class ImageStreamParallel
 *
 * @brief Customizes ImageStream to use a Java 8 parallelstream to
 *        download, process, and store images concurrently.  A
 *        parallelstream uses the default ForkJoinPool, which has as
 *        many threads there are processors, as returned by
 *        Runtime.getRuntime().availableProcessors().  The size of the
 *        pool can be changed using system properties.
 */
public class ImageStreamParallel extends ImageStream {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamParallel(Filter[] filters,
                               Iterator<List<URL>> urlListIterator,
                               Runnable completionHook) {
        super(filters, urlListIterator, completionHook);
    }

    /**
     * Initiate the ImageStream processing, which uses a Java 8
     * parallelstream to download, process, and store images
     * concurrently.
     */
    @Override
    protected void initiateStream() {
        // Create a new exit barrier.
        mIterationBarrier = new CountDownLatch(1);

        // Concurrently process each URL in the input List.
        getInput().parallelStream()
            // Transform URL -> ImageEntity (download each image via
            // its URL).
            .map(url -> makeImageEntity(url))
            // Collect each image and apply each filter in
            // concurrently.
            .forEach(image -> {
                    mFilters.parallelStream()
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
