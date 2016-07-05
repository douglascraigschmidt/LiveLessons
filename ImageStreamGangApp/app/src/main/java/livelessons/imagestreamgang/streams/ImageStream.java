package livelessons.imagestreamgang.streams;

import android.util.Log;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import livelessons.imagestreamgang.filters.Filter;
import livelessons.imagestreamgang.filters.FilterDecoratorWithImage;
import livelessons.imagestreamgang.filters.OutputFilterDecorator;
import livelessons.imagestreamgang.utils.Image;
import livelessons.imagestreamgang.utils.NetUtils;
import livelessons.imagestreamgang.utils.Options;

/**
 * This abstract class customizes the StreamGang framework to use Java
 * 8 functional programming features to download a List of images from
 * web servers, apply image processing filters to each image, and
 * store the results in files that can be displayed to users via
 * various means defined by the context in which this class is used.
 * Subclasses of ImageStream must override the initiateStream() method
 * to download and process the images concurrently.
 */
public abstract class ImageStream 
       extends StreamGang<URL> {
    /**
     * An iterator to the List of input URLs that are used to download
     * Images.
     */
    private Iterator<List<URL>> mUrlListIterator;

    /**
     * The List of filters to apply to the downloaded images.
     */
    protected List<Filter> mFilters;

    /**
     * Clients of ImageStream supply this hook so they know when the
     * all the images have been downloaded, processed, and stored, at
     * which point they can display the stored images.
     */
    private Runnable mCompletionHook;

    /**
     * A barrier synchronizer that's used to coordinate each iteration
     * cycle, i.e., each call to initiateStream() must initialize and
     * wait on this barrier for the other tasks to complete their
     * processing before moving to the next iteration cycle.
     */
    protected CountDownLatch mIterationBarrier = null;

    /**
     * Number of Threads in the fixed-size thread pool.
     */
    private final int MAX_THREADS = 8;

    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStream(Filter[] filters,
                       Iterator<List<URL>> urlListIterator,
                       Runnable completionHook) {
        // Store the Filters to apply as a List.
        mFilters = Arrays.asList(filters);

        // Create an Iterator for the array of URLs to download.
        mUrlListIterator = urlListIterator;

        // Set the completion hook that's called when all the images
        // are downloaded and processed.
        mCompletionHook = completionHook;

        // Initialize the Executor with a fixed-sized pool of Threads.
        setExecutor(Executors.newFixedThreadPool(MAX_THREADS));
    }

    /**
     * Hook method that must be overridden by subclasses to perform
     * the ImageStream processing.
     */
    protected abstract void processStream();

    /**
     * Initiate the ImageStream processing, which uses a Java 8 stream
     * to download, process, and store images sequentially.
     */
    @Override
    protected void initiateStream() {
        // Create a new barrier for this iteration cycle.
        mIterationBarrier = new CountDownLatch(1);

        long start = System.nanoTime();

        // Start the Stream processing.
        processStream();

        long duration = (System.nanoTime() - start) / 1_000_000;
        Log.d(TAG,
              "Done in " 
              + duration
              + " msecs");

        // Indicate all computations in this iteration are done.
        try {
            mIterationBarrier.countDown();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } 
    }

    /**
     * Hook method that waits for concurrent processing to complete.
     */
    @Override
    protected void awaitTasksDone() {
        try {
            // Loop for each iteration cycle of input URLs.
            for (;;) {
                // Barrier synchronizer that waits until all the
                // stream processing in this iteration cycle are done.
                mIterationBarrier.await();

                // Check to see if there's another List of URLs
                // available to process.
                if (setInput(getNextInput()) == null)
                    break; // No more input, so we're done.
                else
                    // Invoke this hook method to initialize the gang
                    // of tasks for the next iteration cycle.
                    initiateStream();
            } 

            // Only call the shutdown() and awaitTermination() methods
            // if we've actually got an ExecutorService (as opposed to
            // just an Executor).
            if (getExecutor() instanceof ExecutorService) {
                ExecutorService executorService = 
                    (ExecutorService) getExecutor();

                // Tell the ExecutorService to initiate a graceful
                // shutdown.
                executorService.shutdown();

                // Wait for all the tasks in the Thread pool to
                // complete.
                executorService.awaitTermination(Long.MAX_VALUE,
                                                 TimeUnit.NANOSECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Run the completion hook now that all the image downloading,
        // processing and storing is now complete.
        mCompletionHook.run();
    }

    /**
     * Factory method that returns the next List of URLs to download
     * and process concurrently by the ImageStream.
     */
    @Override
    protected List<URL> getNextInput() {
        if (mUrlListIterator.hasNext()) {
            // Note that we're starting a new cycle.
            incrementCycle();

            // Return a List containing the URLs to download
            // concurrently.
            return mUrlListIterator.next();
        }
        else
            // Indicate that we're done.
            return null;
    }

    /**
     * Factory method that retrieves the image associated with the @a
     * urlToDownload and creates an Image to encapsulate it.
     */
    protected Image makeImage(URL urlToDownload) {
        return new Image(urlToDownload,
                         NetUtils.downloadContent(urlToDownload));
    }

    /**
     * Factory method that makes a new @a FilterDecoratorWithImage.
     */
    protected FilterDecoratorWithImage makeFilterDecoratorWithImage(Filter filter,
                                                                    Image image) {
        return new FilterDecoratorWithImage(new OutputFilterDecorator(filter),
                                            image);
    }

    /**
     * @return true if the @a url is not already in the cache, else false.
     */
    protected boolean urlNotCached(URL url, String filterName) {
        // Construct the subdirectory for the filter.
        File externalFile = new File(Options.instance().getDirectoryPath(),
                                     filterName);
        // Construct the filename for the URL.
        File imageFile = new File(externalFile,
                                  NetUtils.getFileNameForUrl(url));
        // If the image file does not already exist then the URL is
        // not cached.
        return !imageFile.exists();
    }

    /**
     * @return true if the @a url is not already in the cache, else
     * false.
     */
    protected boolean urlNotCached(URL url) {
        // Iterate through the list of filters and concurrently check
        // to see which ones are already cached.
        long count = mFilters
            .parallelStream()
            .filter(filter -> 
                    urlNotCached(url, filter.getName()))
            .count();

        // A count > 0 means the url was not already in the cache.
        return count > 0;
    }
}

