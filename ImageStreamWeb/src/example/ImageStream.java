package example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import filters.Filter;
import filters.OutputFilterDecorator;

/**
 * @class ImageStream
 *
 * @brief This abstract class customizes the StreamGang framework to
 *        use Java 8 functional programming features to concurrently
 *        download a List of images from web servers, apply image
 *        processing filters to each image, and store the results in
 *        files that can be displayed to users via various means
 *        defined by the context in which this class is used.
 *        Subclasses of ImageStream must override the initiateStream()
 *        method to download and process the images concurrently.
 *
 *        This class implements the roles of the "Proactive Initiator"
 *        and "Completion Handler" in the Proactor pattern and also
 *        plays the role of the "Concrete Class" in the Template
 *        Method pattern.
 */
public abstract class ImageStream extends StreamGang<URL> {
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
     * Method that runs in the background to download, process, and
     * store an image at @a urlToDownload.  It returns the ImageEntity
     * representing the image.
     */
    protected ImageEntity processInput(URL urlToDownload,
                                       Filter filter) {
        // Download an image into a new ImageEntity object.
    	final ImageEntity downloadedImage = 
            makeImageEntity(urlToDownload);

        // Create an OutputFilterDecorator that encapsulates the
        // original filter.
        Filter decoratedFilter =
            new OutputFilterDecorator(filter);

        // Process the downloaded image, store it
        // into a file, return the result.
        return decoratedFilter.filter(downloadedImage);
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
     * Factory method that retrieves the image associated with the @a
     * urlToDownload and creates an ImageEntity to encapsulate it.
     */
    protected ImageEntity makeImageEntity(URL urlToDownload) {
        return new ImageEntity(urlToDownload,
                               downloadContent(urlToDownload));
    }

    /**
     * Download the contents found at the given URL and return them as
     * a raw byte array.
     */
    private byte[] downloadContent(URL url) {
        // The size of the image downloading buffer.
        final int BUFFER_SIZE = 4096;

        // Creates a new ByteArrayOutputStream to write the downloaded
        // contents to a byte array, which is a generic form of the
        // image.
        ByteArrayOutputStream ostream = 
            new ByteArrayOutputStream();
        
        // This is the buffer in which the input data will be stored
        byte[] readBuffer = new byte[BUFFER_SIZE];
        int bytes;
        
        // Creates an InputStream from the inputUrl from which to read
    	// the image data.
        try (InputStream istream = (InputStream) url.openStream()) {
            // While there is unread data from the inputStream,
            // continue writing data to the byte array.
            while ((bytes = istream.read(readBuffer)) > 0) 
                ostream.write(readBuffer, 0, bytes);

            return ostream.toByteArray();
        } catch (IOException e) {
            // "Try-with-resources" will clean up the istream
            // automatically.
            e.printStackTrace();
            return null;
        }
    }
}
