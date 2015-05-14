package example;

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

        // Sequentially process each filter in the mFilters List.
        getInput().stream()
	    	// transform URL -> ImageEntity
	    	.map(url -> makeImageEntity(url))
	    	// Check to see if the download was successful
	    	.peek(image -> 
	    		PlatformStrategy.instance().errorLog
	                         ("ImageStreamParallel",
	                          "Operations"
	                          + (image.getSucceeded() == true 
	                             ? " succeeded" 
	                             : " failed")
	                          + " on file " 
	                          + image.getSourceURL()))
	        // collect each image and apply each filter in parallel
	    	.forEach(image -> {
	    		mFilters.stream()
	    			// decorate each filter to write the images to files
	    			.map(filter -> new OutputFilterDecorator(filter))
	    			// filter the image
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
