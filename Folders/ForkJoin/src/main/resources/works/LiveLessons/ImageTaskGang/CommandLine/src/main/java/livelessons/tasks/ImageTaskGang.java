package livelessons.tasks;

import livelessons.filters.Filter;
import livelessons.utils.Image;
import livelessons.utils.FileAndNetUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Customizes the TaskGang framework for images and defines methods
 * that are reused by various subclasses.
 */
public abstract class ImageTaskGang 
       extends TaskGang<URL> {
    /**
     * An iterator of input URLs that are used to download images.
     */
    protected Iterator<List<URL>> mUrlListIterator;

    /**
     * The list of filters to apply to the downloaded images.
     */
    protected List<Filter> mFilters;

    /**
     * Constructor initializes the superclass and fields.
     */
    public ImageTaskGang(Filter[] filters,
                         List<List<URL>> urlLists) {
        // Store the Filters to apply as a List.
        mFilters = Arrays.asList(filters);

        // Create an Iterator for the array of URLs to download.
        mUrlListIterator = urlLists.iterator();
    }

    /**
     * Factory method that returns the next List of URLs to download
     * and process concurrently by the ImageTaskGang.
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
    protected Image downloadImage(URL url) {
        return new Image(url,
                         FileAndNetUtils.downloadContent(url));
    }

    /**
     * Keeps track of how long a given test has run.
     */
    private long mStartTime;

    /**
     * Keeps track of all the execution times.
     */
    private final List<Long> mExecutionTimes = new ArrayList<>();

    /**
     * Return the time needed to execute the test.
     */
    public List<Long> executionTimes() {
        return mExecutionTimes;
    }

    /**
     * Start timing the test run.
     */
    public void startTiming() {
        // Note the start time.
        mStartTime = System.nanoTime();
    }

    /**
     * Stop timing the test run.
     */
    public void stopTiming() {
        mExecutionTimes.add((System.nanoTime() - mStartTime) / 1_000_000);
    }
}
