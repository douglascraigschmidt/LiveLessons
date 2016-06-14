package example.imagetaskgang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

import example.imagetaskgang.filters.Filter;
import example.imagetaskgang.filters.OutputFilterDecorator;

import android.annotation.SuppressLint;

/**
 * @class ImageTaskGang
 *
 * @brief Customizes the TaskGang framework to use the Java
 *        ExecutorCompletionService to concurrently download a List of
 *        images from web servers, apply image processing filters to
 *        each image, and store the results in files that can be
 *        displayed to users via various means defined by the context
 *        in which this class is used.
 *
 *        This class implements the roles of the "Proactive Initiator"
 *        and "Completion Handler" in the Proactor pattern and also
 *        plays the role of the "Concrete Class" in the Template
 *        Method pattern.
 */
public class ImageTaskGang extends TaskGang<URL> {
    /**
     * An iterator to the List of input URLs that are used to download
     * Images.
     */
    private Iterator<List<URL>> mUrlListIterator;

    /**
     * The List of filters to apply to the downloaded images.
     */
    private List<Filter> mFilters;

    /**
     * An ExecutorCompletionService used to concurrently download and
     * apply image processing tasks on designated URLs.  This plays
     * the role of the "Asynchronous Operation Processor" in the
     * Proactor pattern.
     */
    private ExecutorCompletionService<ImageEntity> mCompletionService;

    /**
     * Clients of ImageTaskGang supply this hook so they know when the
     * all the images have been downloaded, processed, and stored, at
     * which point they can display the stored images.
     */
    private Runnable mCompletionHook;

    /**
     * A barrier synchronizer that's used to coordinate each iteration
     * cycle, i.e., each task in the TaskGang must wait on this
     * barrier for the other tasks to complete their processing before
     * they all attempt to move to the next cycle en masse.
     */
    protected CountDownLatch mIterationBarrier = null;

    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageTaskGang(Filter[] filters,
                         Iterator<List<URL>> urlListIterator,
                         Runnable completionHook) {
        // Store the Filters to apply as a List.
        mFilters = Arrays.asList(filters);

        // Create an Iterator for the array of URLs to download.
        mUrlListIterator = urlListIterator;

        // Set the completion hook that's called when all the images
        // are downloaded and processed.
        mCompletionHook = completionHook;

        // Initialize the Executor with a cached pool of Threads,
        // which grow and shrink dynamically as new tasks are
        // executed.
        setExecutor(Executors.newCachedThreadPool());

        // Connect the Executor with the CompletionService to process
        // result Futures concurrently.
        mCompletionService =
            new ExecutorCompletionService<ImageEntity>(getExecutor());
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
     * Initializes the ImageTaskGang to run each task in the
     * CachedThreadPool.
     */
    @Override
    protected void initiateTaskGang(int initialNumberOfURLs) {
        // Create a new iteration barrier with the appropriate size.
        mIterationBarrier = new CountDownLatch(initialNumberOfURLs);

        // Enqueue each item in the input List for execution in the
        // Executor's Thread pool, which ensures there's a Thread
        // available to run each task concurrently.
        for (int i = 0; i < initialNumberOfURLs; ++i)
            getExecutor().execute(makeTask(i));
    }

    /**
     * Hook method that runs in a background Thread to download,
     * process, and store an image via the ExecutorCompletionService.
     */
    @Override
    protected boolean processInput(URL urlToDownload) {
        // Download an image into a new ImageEntity object.
    	final ImageEntity downloadedImage = 
            makeImageEntity(urlToDownload);

        // For each filter in the List of Filters, submit a task to
        // the ExecutorCompletionService that filters the image
        // downloaded from the given URL, stores the results in a
        // file, and puts the results of the filtered image in the
        // completion queue.
        for (final Filter filter : mFilters) {
        	
            // The ExecutorCompletionService receives a Callable and
            // invokes its call() method, which returns the filtered
            // ImageEntity.
            mCompletionService.submit(new Callable<ImageEntity>() {
                    @Override
                    public ImageEntity call() {
                    	// Create an OutputFilterDecorator that
                        // encapsulates the original filter.
                        Filter decoratedFilter =
                            new OutputFilterDecorator(filter);

                        // Filter the downloaded image, store it into
                        // a file, return the result.
                        return decoratedFilter.filter(downloadedImage);
                    }
                });
        }

        return true;
    }

    /**
     * Each task in the gang uses the CountDownLatch countDown()
     * method indicate that they are done with their processing.
     */
    @Override
    protected void taskDone(int index) throws IndexOutOfBoundsException {
        try {
            // Indicate that this task is done with its computations.
            mIterationBarrier.countDown();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } 
    }

    /**
     * Hook method that waits for the gang of tasks to complete all
     * their processing.
     */
    @Override
    protected void awaitTasksDone() {
        try {
            // Keeps track of the number of result Futures to process.
            int resultsCount = 0;

            // Loop for each iteration cycle of input URLs.
            for (;;) {
                // Increment the number of URLs to download.
                resultsCount += getInput().size();

                // Barrier synchronizer that wait until all tasks in
                // this iteration cycle are done.
                mIterationBarrier.await();

                // Check to see if there's another List of URLs
                // available to process.
                if (setInput(getNextInput()) == null)
                    break; // No more input, so we're done.
                else
                    // Invoke this hook method to initialize the gang
                    // of tasks for the next iteration cycle.
                    initiateTaskGang(getInput().size());
            } 


            // Account for all the downloaded images and all the
            // filters applied to these images.
            resultsCount *= mFilters.size();

            // Process all the result Futures asynchronously via the
            // ExecutorCompletionService's completion queue.
            concurrentlyProcessFilteredResults(resultsCount);

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
     * Removes result Futures from the ExecutorCompletionService's
     * completion queue until all the processed downloads have been
     * received and prints diagnostics indicating if the image
     * downloading, processing, and storing worked properly.
     */
    protected void concurrentlyProcessFilteredResults(int resultsCount) {
        // Loop for the designated number of results.
        for (int i = 0; i < resultsCount; ++i) 
            try {
                // Take the next available Future off the
                // ExecutorCompletionService's completion queue.
                final Future<ImageEntity> resultFuture =
                    mCompletionService.take();

                // The get() call will not block since the results
                // should be ready before they are added to the
                // completion queue.
                ImageEntity imageEntity = resultFuture.get();
    
                // Indicate success or failure for this URL.
                PlatformStrategy.instance().errorLog
                    ("ImageTaskGang",
                     "Operations on file " 
                     + imageEntity.getSourceURL()
                     + (imageEntity.getSucceeded() == true 
                        ? " succeeded" 
                        : " failed"));
            } catch (ExecutionException e) {
                PlatformStrategy.instance().errorLog("ImageTaskGang",
                                                     "get() ExecutionException");
            } catch (InterruptedException e) {
                PlatformStrategy.instance().errorLog("ImageTaskGang",
                                                     "get() InterruptedException");
            }
    }

    /**
     * Factory method that retrieves the image associated with the @a
     * urlToDownload and creates an ImageEntity to encapsulate it.
     */
    private ImageEntity makeImageEntity(URL urlToDownload) {
        return new ImageEntity(urlToDownload,
                               downloadContent(urlToDownload));
    }

    /**
     * Download the contents found at the given URL and return them as
     * a raw byte array.
     */
    @SuppressLint("NewApi")
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
