package livelessons.imagestreamgang.streams;

import android.util.Log;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import livelessons.imagestreamgang.filters.Filter;
import livelessons.imagestreamgang.filters.OutputFilterDecorator;

/**
 * Customizes ImageStream to use Java 8 CompletableFutures to
 * download, process, and store images concurrently.
 */
public class ImageStreamCompletableFuture 
       extends ImageStream {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamCompletableFuture(Filter[] filters,
                                        Iterator<List<URL>> urlListIterator,
                                        Runnable completionHook) {
        super(filters, urlListIterator, completionHook);
    }

    /**
     * Initiate the ImageStream processing, which uses Java 8
     * CompletableFutures to download, process, and store images
     * concurrently.
     */
    @Override
    protected void initiateStream() {
        // Create a new barrier for this iteration cycle.
        mIterationBarrier = new CountDownLatch(1);

        // Concurrently process each URL in the input List.
        getInput().parallelStream()
            // Submit the URL for asynchronous downloading.
            .map(url -> CompletableFuture.supplyAsync
                     (() -> makeImage(url),
                      getExecutor()).join())
            // Map each image to a parallel stream of the filtered
            // versions of the entity.
            .flatMap(image ->
                     mFilters.parallelStream()
                     // Decorate each filter to write the images to
                     // files.
                     .map(filter -> makeFilter(filter))
                     // Submit the imageEntity for asynchronous
                     // filtering.
                     .map(decoratedFilter -> 
                          CompletableFuture.supplyAsync
                              (() -> decoratedFilter.filter(image),
                               getExecutor()).join())
                     .collect(Collectors.toList()).parallelStream())
            // Report the success of the pipeline for each filtered
            // entity.
            .forEach(image -> Log.e
                     ("CompletableFuture",
                      "Operations"
                      + (image.getSucceeded() == true
                         ? " succeeded" 
                         : " failed")
                      + " on file " 
                      + image.getSourceURL())
                     );


        // Indicate all computations in this iteration are done.
        try {
            mIterationBarrier.countDown();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } 
    }
}
