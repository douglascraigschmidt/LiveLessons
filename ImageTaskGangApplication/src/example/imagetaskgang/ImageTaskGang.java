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
     * The List of filters to apply to the downloaded images.
     */
    private List<Filter> mFilters;

    /**
     * An iterator to the List of input URLs that are used to download
     * Images.
     */
    private Iterator<List<URL>> mUrlIterator;

    /**
     * An ExecutorCompletionService that executes image filtering
     * tasks on designated URLs.  This plays the role of the
     * "Asynchronous Operation Processor" in the Proactor pattern.
     */
    private ExecutorCompletionService<InputEntity> mCompletionService;

    /**
     * Clients of ImageTaskGang supply this hook so they know when the
     * images have been downloaded, processed, and stored, at which
     * point they typically display the stored images.
     */
    private Runnable mCompletionHook;

    /**
     * The iteration barrier that's used to coordinate each cycle,
     * i.e., each Thread in the TaskGang must await on
     * mIterationBarrier for all the other Threads to complete their
     * processing before they all attempt to move to the next cycle en
     * masse.
     */
    protected CountDownLatch mIterationBarrier = null;

    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageTaskGang(Filter[] filters,
                         Iterator<List<URL>> urlIterator,
                         Runnable completionHook) {
        // Create an Iterator for the array of URLs to download.
        mUrlIterator = urlIterator;

        // Store the Filters to apply as a List.
        mFilters = Arrays.asList(filters);

        // Initialize the Executor with a cached pool of Threads,
        // which grow dynamically.
        setExecutor(Executors.newCachedThreadPool());

        // Connect the Executor with the CompletionService to process
        // result Futures concurrently.
        mCompletionService =
            new ExecutorCompletionService<InputEntity>(getExecutor());

        // Set the completion hook that's called when all the images
        // are downloaded and processed.
        mCompletionHook = completionHook;
    }

    /**
     * Factory method that returns the next List of URLs to be
     * downloaded and processed concurrently by the ImageTaskGang.
     */
    @Override
    protected List<URL> getNextInput() {
        if (mUrlIterator.hasNext()) {
            // Note that we're starting a new cycle.
            incrementCycle();

            // Return a List containing the URLs to download
            // concurrently.
            return mUrlIterator.next();
        }
        else
            // Indicate that we're done.
            return null;
    }

    /**
     * Initiate the TaskGang to run each task in a pool of Threads.
     */
    @Override
    protected void initiateTaskGang(int inputSize) {
        // Create a new iteration barrier with the appropriate size.
        mIterationBarrier = new CountDownLatch(inputSize);

        // Enqueue each item in the input List for execution in the
        // Executor's Thread pool, which will ensure there's a Thread
        // allocated for each task.
        for (int i = 0; i < inputSize; ++i)
            getExecutor().execute(makeTask(i));
    }

    /**
     * Factory method that downloads a URL and creates an ImageEntity
     * to encapsulate it.
     */
    private ImageEntity makeImageEntity(URL urlToDownload) {
        return new ImageEntity(urlToDownload,
                               downloadContent(urlToDownload));
    }

    /**
     * Run in a background Thread to download, process, and store an
     * Image via the ExecutorCompletionService.
     */
    @Override
    protected boolean processInput(URL urlToDownload) {
        // Download an image into a new ImageEntity object.
    	final ImageEntity originalImage = 
            makeImageEntity(urlToDownload);

        // For each filter in the List of Filters, submit a task to
        // the ExecutorCompletionService that filters the image
        // downloaded from the given URL, stores the results in a
        // file, and puts the results of the filtered image in the
        // completion queue.
        for (final Filter filter : mFilters) {
        	
            // The ExecutorCompletionService receives a callable and
            // invokes its call() method, which returns the filtered
            // InputEntity (that's actually an ImageEntity).
            mCompletionService.submit(new Callable<InputEntity>() {
                    @Override
                    public InputEntity call() {
                    	// Create an OutputFilterDecorator that
                        // contains the original filter and the
                        // original Image.
                        Filter decoratedFilter =
                            new OutputFilterDecorator(filter);

                        // Filter the original image and store it in a
                        // file.
                        return decoratedFilter.filter(originalImage);
                    }
                });
        }

        return true;
    }

    /**
     * Download the contents found at the given URL and return them as
     * a raw byte array.
     */
    @SuppressLint("NewApi")
    private byte[] downloadContent(URL url) {
        // The size of the image downloading buffer
        final int BUFFER_SIZE = 4096;

        // Opens a new ByteArrayOutputStream to write the downloaded
        // contents to a byte array, which is a generic form of the
        // image.
        ByteArrayOutputStream ostream = 
            new ByteArrayOutputStream();
        
        // This is the buffer in which the input data will be stored
        byte[] readBuffer = new byte[BUFFER_SIZE];
        int bytes;
        
        // Open an InputStream from the inputUrl from which to read
    	// the Image data.
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
     * Hook method that used as an exit barrier to wait for the gang
     * of tasks to exit.
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

                // Check to see if there's any input remaining to
                // process.
                if (setInput(getNextInput()) == null)
                    break; // No more input, so we're done.
                else
                    // Invoke this hook method to initialize the gang
                    // of tasks for the next iteration cycle.
                    initiateTaskGang(getInput().size());
            } 


            // Account for all the downloaded images and all the
            // filtering of these images.
            resultsCount *= mFilters.size();

            // Process all the Futures concurrently via the
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

                // Wait for all the tasks/threads in the pool to
                // complete.
                executorService.awaitTermination(Long.MAX_VALUE,
                                                 TimeUnit.NANOSECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Run the completion hook now that all the image downloading,
        // processing and storing is done.
        mCompletionHook.run();
    }

    /**
     * Block on the ExecutorCompletionService's completion queue until
     * all the processed downloads have been received and then print
     * diagnostics indicating if the downloading and processing worked
     * properly.
     */
    protected void concurrentlyProcessFilteredResults(int resultsCount) {
        // Loop for the designated number of results.
        for (int i = 0; i < resultsCount; ++i) 
            try {
                // Take the next ready Future off the
                // CompletionService's queue.
                final Future<InputEntity> resultFuture =
                    mCompletionService.take();

                // The get() call will not block since the results
                // should be ready before they are added to the
                // completion queue.
                InputEntity inputEntity = resultFuture.get();
    
                // Indicate success or failure for this URL.
                PlatformStrategy.instance().errorLog
                    ("ImageTaskGang",
                     "Operations on file " 
                     + inputEntity.getSourceURL()
                     + (inputEntity.succeeded() == true 
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
}
