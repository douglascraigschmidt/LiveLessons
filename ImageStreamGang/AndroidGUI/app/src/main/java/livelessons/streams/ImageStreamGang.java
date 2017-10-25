package livelessons.streams;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import livelessons.utils.BlockingTask;
import livelessons.utils.Image;
import livelessons.utils.NetUtils;
import livelessons.utils.Options;
import livelessons.filters.Filter;
import livelessons.filters.FilterDecoratorWithImage;
import livelessons.filters.OutputFilterDecorator;

/**
 * This abstract class customizes the StreamGang framework to use Java
 * 8 functional programming features to download a list of images from
 * remote web servers (or open them in a local resources directory),
 * apply image processing filters to each image, and store the results
 * in files that can be displayed to the user.  ImageStreamGang
 * subclasses must override the initiateStream() hook method to
 * download (or open) and process the images concurrently. This class
 * plays the role of the "Concrete class" in the Template Method
 * pattern.
 */
public abstract class ImageStreamGang
       extends StreamGang<URL> {
    /**
     * An iterator to the List of input URLs that are used to download
     * (or open) images.
     */
    private Iterator<List<URL>> mUrlListIterator;

    /**
     * The List of filters to apply to the images.
     */
    protected List<Filter> mFilters;

    /**
     * Maximum number of threads in a fixed-size thread pool.
     */
    private final int sMAX_THREADS = 100;

    /**
     * Constructor initializes the class and fields.
     */
    public ImageStreamGang(Filter[] filters,
                           Iterator<List<URL>> urlListIterator) {
        // Store the Filters to apply as a List.
        mFilters = Arrays.asList(filters);

        // Create an Iterator for the array of URLs to download.
        mUrlListIterator = urlListIterator;
    }

    /**
     * Hook method that must be overridden by subclasses to perform
     * the particular implementation strategy.
     */
    protected abstract void processStream();

    /**
     * A hook method that's also a template method. It does some
     * bookkeeping operations and dispatches the subclass's
     * processStream() hook method to start the implementation
     * strategy processing.
     */
    @Override
    protected void initiateStream() {
        // The thread pool size is the smaller of (1) the number of
        // filters times the number of images to download and (2)
        // sMAX_THREADS (which prevents allocating excessive threads).
        int threadPoolSize = Math.min(mFilters.size() * getInput().size(),
                                      sMAX_THREADS);

        // Initialize the Executor with appropriate pool of threads.
        setExecutor(Executors.newFixedThreadPool(threadPoolSize));

        // Start timing the test run.
        startTiming();

        // Perform the stream processing.
        processStream();

        // Stop timing the test run.
        stopTiming();
    }

    /**
     * Hook method that waits for concurrent processing to complete.
     */
    @Override
    protected void awaitTasksDone() {
        try {
            // Loop for each iteration cycle of input URLs.
            for (;;) {
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
     * Transform URL to an Image by downloading each image via its
     * URL.  This call ensures the common fork/join thread pool is
     * expanded to handle the blocking image download.
     */
    protected Image blockingDownload(URL url) {
        return BlockingTask.callInManagedBlock(()
                                               -> downloadImage(url));
    }

    /**
     * Factory method that retrieves the image associated with the @a
     * url and creates an Image to encapsulate it.
     */
    protected Image downloadImage(URL url) {
        byte[] imageData = NetUtils.downloadContent(url);

        return new Image(url,
                         imageData);
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
     * Checks to see if the @a url is already exists in the file
     * system.  If not, it atomically creates a new file based on
     * combining the @a url with the @a filterName and returns false,
     * else true.

     * @return true if the @a url already exists in file system, else
     * false.
     */
    protected boolean urlCached(URL url,
                                String filterName) {
        File imageFile = null;
        try {
            // Construct the subdirectory for the filter.
            File externalFile =
                new File(Options.instance().getDirectoryPath(),
                         filterName);

            // Construct a new file based on the filename for the URL.
            imageFile =
                new File(externalFile,
                         NetUtils.getFileNameForUrl(url));
            
            // The URL is already cached if imageFile exists so we
            // negate the return value from createNewFile().
            return !imageFile.createNewFile();
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println("file " + imageFile.toString() + e);
            return true;
        }
    }

    /**
     * @return true if the @a url is in the cache, else false.
     */
    protected boolean urlCached(URL url) {
        // Iterate through the list of filters and check to see which
        // images already exist in the cache.
        long count = mFilters
            // Convert list of filters into a stream.
            .stream()

            // Find files that already exist.
            .filter(filter -> 
                    urlCached(url, filter.getName()))

            // Return a count of the number of files that already
            // exist.
            .count();

        // A count > 0 means the url has already been cached.
        return count > 0;
    }

    /**
     * Keeps track of how long a given test has run.
     */
    private long mStartTime;

    /**
     * Keeps track of all the execution times.
     */
    private List<Long> mExecutionTimes = new ArrayList<>();

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

