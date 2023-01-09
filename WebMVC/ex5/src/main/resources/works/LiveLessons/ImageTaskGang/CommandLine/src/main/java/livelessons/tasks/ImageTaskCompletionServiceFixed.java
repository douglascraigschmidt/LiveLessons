package livelessons.tasks;

import livelessons.filters.Filter;
import livelessons.filters.FilterDecoratorWithImage;
import livelessons.filters.OutputFilterDecorator;
import livelessons.utils.Image;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Customizes the ImageTaskGang to use the ExecutorCompletionService
 * in conjunction with a fixed-sized thread pool to concurrently
 * download a list of images from web servers, apply image processing
 * filters to each image, and store the results in files that can be
 * displayed to users via various means defined by the context in
 * which this class is used.
 *
 * This class implements the roles of the "Proactive Initiator" and
 * "Completion Handler" in the Proactor pattern and also plays the
 * role of the "Concrete Class" in the Template Method pattern.
 */
public class ImageTaskCompletionServiceFixed
       extends ImageTaskGang {
    /**
     * An ExecutorCompletionService used to concurrently download and
     * apply image processing tasks on designated URLs.  This plays
     * the role of the "Asynchronous Operation Processor" in the
     * Proactor pattern.
     */
    private ExecutorCompletionService<Image> mCompletionService;

    /**
     * Maximum number of threads in a fixed-size thread pool.
     */
    private final int sMAX_THREADS = 100;

    /**
     * Constructor initializes the superclass and fields.
     */
    public ImageTaskCompletionServiceFixed(Filter[] filters,
                                           List<List<URL>> urlLists) {
        // Initialize the super class.
        super(filters, 
              urlLists);

        // The thread pool size is the smaller of (1) the number of
        // filters times the number of images to download and (2)
        // sMAX_THREADS (which prevents allocating excessive threads).
        int threadPoolSize = Math.min(filters.length * urlLists.get(0).size(),
                                      sMAX_THREADS);

        // Initialize the Executor with a cached pool of threads,
        // which grow and shrink dynamically as new tasks are
        // executed.
        setExecutor(Executors.newFixedThreadPool(threadPoolSize));

        // Prestart all the core threads.
        ((ThreadPoolExecutor) getExecutor()).prestartAllCoreThreads();

        // Connect the Executor with the CompletionService to process
        // result futures concurrently.
        mCompletionService =
            new ExecutorCompletionService<Image>(getExecutor());
    }

    /**
     * Hook method that runs in a background thread to download,
     * process, and store an image via the ExecutorCompletionService.
     */
    @Override
    protected boolean processInput(URL urlToDownload) {
        // Download an image into a new Image object.
    	final Image downloadedImage =
            downloadImage(urlToDownload);

        // For each filter in the List of filters, submit a task to
        // the ExecutorCompletionService that filters the image
        // downloaded from the given URL, stores the results in a
        // file, and puts the results of the filtered image in the
        // completion queue.
        for (Filter filter : mFilters) {
        	
            // The ExecutorCompletionService receives a Callable and
            // invokes its call() method, which returns the filtered
            // ImageEntity.
            mCompletionService.submit(() -> {
                    // Create an OutputFilterDecorator that
                    // encapsulates the original filter.
                    Filter decoratedFilter =
                        new OutputFilterDecorator(filter);

                    // Process the downloaded image, store it
                    // into a file, return the result.
                    return decoratedFilter.filter(downloadedImage);
                });
        }

        return true;
    }

    /**
     * Initializes the ImageTaskGang to run each task in the
     * FixedThreadPool.
     */
    @Override
    protected void initiateTaskGang(int initialNumberOfURLs) {
        // Start the timing for this test.
        startTiming();

        // Enqueue each item in the input list for execution in the
        // Executor's thread pool, which ensures there's a thread
        // available to run each task concurrently.
        for (int i = 0; i < initialNumberOfURLs; ++i)
            getExecutor().execute(makeTask(i));
    }

    /**
     * Hook method that waits for the gang of tasks to complete all
     * their processing.
     */
    @Override
    protected void awaitTasksDone() {
        try {
            // Loop for each iteration cycle of input URLs.
            for (;;) {
                // Keeps track of the number of result Futures to
                // process.  Accounts for all the downloaded images
                // and all the filters applied to these images.
                int resultsCount = getInput().size() * mFilters.size();

                // Process all the result Futures asynchronously via the
                // ExecutorCompletionService's completion queue.
                concurrentlyProcessFilteredResults(resultsCount);

                // Stop the timer for this test.
                stopTiming();

                // Check to see if there's another List of URLs
                // available to process.
                if (setInput(getNextInput()) == null)
                    break; // No more input, so we're done.
                else
                    // Invoke this hook method to initialize the gang
                    // of tasks for the next iteration cycle.
                    initiateTaskGang(getInput().size());
            } 

            // Only call the shutdown() and awaitTermination() methods if
            // we've actually got an ExecutorService (as opposed to just
            // an Executor).
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
     * Removes result Futures from the ExecutorCompletionService's
     * completion queue until all the processed downloads have been
     * received and prints diagnostics indicating if the image
     * downloading, processing, and storing worked properly.
     */
    protected void concurrentlyProcessFilteredResults(int resultsCount)
            throws InterruptedException{
        Thread t = new Thread(() -> {
            int succeeded = 0;
            int failed = 0;

            // Loop for the designated number of results.
            for (int i = 0; i < resultsCount; ++i)
                try {
                    // Take the next available Future off the
                    // ExecutorCompletionService's completion queue.
                    final Future<Image> resultFuture =
                            mCompletionService.take();

                    // The get() call will not block since the results
                    // should be ready before they are added to the
                    // completion queue.
                    Image image = resultFuture.get();

                    // Indicate success or failure for this URL.
                    if (false)
                        System.out.println
                                ("ImageTaskCompletionServiceFixed: Operations on URL "
                                        + image.getSourceURL()
                                        + "\n       in file "
                                        + image.getFileName()
                                        + (image.getSucceeded() == true
                                        ? " succeeded"
                                        : " failed"));
                    if (image.getSucceeded())
                        succeeded++;
                    else
                        failed++;

                } catch (ExecutionException e) {
                    System.out.println("ImageTaskCompletionServiceFixed: get() ExecutionException");
                } catch (InterruptedException e) {
                    System.out.println("ImageTaskCompletionServiceFixed: get() InterruptedException");
                }

            System.out.println("ImageTaskCompletionServiceFixed: "
                    + succeeded
                    + " operations succeeded and "
                    + failed
                    + " operations failed.");
        });
        t.start();
        t.join();
    }
}    
