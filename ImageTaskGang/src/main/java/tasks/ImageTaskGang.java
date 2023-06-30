package tasks;

import filters.Filter;
import filters.OutputFilterDecorator;
import utils.FileAndNetUtils;
import utils.Image;
import utils.Options;
import utils.TaskGang;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Customizes the {@link TaskGang} to use the Java {@link
 * ExecutorCompletionService} to concurrently download a list of
 * images from web servers, apply image processing filters to each
 * image, and store the results in local files that can be displayed to
 * users via various means defined by the context in which this class
 * is used.
 *
 * This class implements the roles of the "Proactive Initiator" and
 * "Completion Handler" in the Proactor pattern and also plays the
 * role of the "Concrete Class" in the Template Method pattern.
 */
public class ImageTaskGang
       extends TaskGang<URL> {
    /**
     * An {@link Iterator} of input {@link URL} objects that are used to
     * download images.
     */
    protected final Iterator<List<URL>> mUrlListIterator;

    /**
     * The {@link List} of {@link Filter} objects to apply to the
     * downloaded images.
     */
    protected final List<Filter> mFilters;

    /**
     * An {@link ExecutorCompletionService} used to concurrently
     * download and apply image processing tasks on designated URLs.
     * This field plays the role of the "Asynchronous Operation Processor"
     * in the Proactor pattern.
     */
    private final ExecutorCompletionService<Image> mCompletionService;

    /**
     * Name of the test (used for diagnostics).
     */
    private final String mTestName;

    /**
     * Constructor initializes the superclass and fields.
     */
    public ImageTaskGang(Filter[] filters,
                         List<List<URL>> urlLists,
                         String testName,
                         ExecutorService executor) {
        // Store the Filters to apply as a List.
        mFilters = Arrays.asList(filters);

        // Create an Iterator for the array of URLs to download.
        mUrlListIterator = urlLists.iterator();

        // Set the test name for diagnostics.
        mTestName = testName;

        // Initialize the Executor.
        setExecutor(executor);

        // Connect the Executor with the CompletionService to process
        // result futures concurrently.
        mCompletionService =
            new ExecutorCompletionService<>(executor);
    }

    /**
     * @return The next {@link List} of {@link URL} objects to
     * download and process concurrently by the {@link
     * ImageTaskGang}
     */
    @Override
    protected List<URL> getNextInput() {
        if (mUrlListIterator.hasNext()) {
            // Note that we're starting a new cycle.
            incrementCycle();

            // Return a List containing the URLs to download
            // concurrently.
            return mUrlListIterator.next();
        } else
            // Indicate that we're done.
            return null;
    }

    /**
     * Initializes the {@link ImageTaskGang} to run each task in the
     * designated {@link Executor} returned by {@code getExecutor()}.
     */
    @Override
    protected void initiateTaskGang(int initialNumberOfURLs) {
        // Enqueue each item in the input list for execution in the
        // Executor's thread pool, which ensures there's a thread
        // available to run each task concurrently.
        for (int i = 0; i < initialNumberOfURLs; ++i)
            getExecutor().execute(makeTask(i));
    }

    /**
     * Hook method that runs in a background thread to download,
     * process, and store the {@link URL} via the {@link
     * ExecutorCompletionService}.
     */
    @Override
    protected boolean processInput(URL urlToDownload) {
        // Download an image into a new Image object.
        var downloadedImage =
            getOrDownloadImage(urlToDownload);

        // For each filter in the List of filters, submit a task to
        // the ExecutorCompletionService that filters the image
        // downloaded from the given URL, stores the results in a
        // file, and puts the results of the filtered image in the
        // completion queue.
        for (var filter : mFilters) {
            // The ExecutorCompletionService receives a Callable and
            // invokes its call() method, which returns the filtered
            // ImageEntity.
            mCompletionService
                .submit(() -> {
                    // Create an OutputFilterDecorator that
                    // encapsulates the original filter.
                    var decoratedFilter =
                        new OutputFilterDecorator(filter);

                    // Process the downloaded image, store it into
                    // a file, return the result.
                    return decoratedFilter
                        .filter(downloadedImage);
                });
        }

        return true;
    }

    /**
     * Factory method that retrieves the image associated with the
     * {@link URL} and creates an {@link Image} to encapsulate it.
     *
     * @return An {@link Image} containing the contents of the {@link
     *         URL}
     */
    protected Image getOrDownloadImage(URL url) {
        return new Image(url,
            FileAndNetUtils.downloadContent(url));
    }

    /**
     * Hook method that waits for the gang of tasks to complete all
     * their processing.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void awaitTasksDone() {
        try {
            // Loop until there's no more input to process.
            for (;;) {
                // Keeps track of the number of result Futures to
                // process.  Accounts for all the downloaded images
                // and all the filters applied to these images.
                int resultsCount = getInput().size() * mFilters.size();

                // Process all the result Futures asynchronously via the
                // ExecutorCompletionService's completion queue.
                concurrentlyProcessFilteredResults(resultsCount);

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
            if (getExecutor() instanceof ExecutorService executorService) {

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
     * Removes result {@link Future} objects from the {@link
     * ExecutorCompletionService}'s completion queue until all
     * processed downloads have been received and prints diagnostics
     * indicating if the image downloading, processing, and storing
     * worked properly.
     */
    protected void concurrentlyProcessFilteredResults
    (int resultsCount)
        throws InterruptedException {
        int succeeded = 0;
        int failed = 0;

        // Loop for the designated number of results.
        for (int i = 0; i < resultsCount; ++i)
            try {
                // Take the next available Future off the
                // ExecutorCompletionService's completion queue
                // (may block).
                Future<Image> resultFuture =
                    mCompletionService.take();

                // This get() call will not block since the results
                // should be ready before they are added to the
                // completion queue.
                Image image = resultFuture.get();

                // Indicate success or failure for this URL.
                if (Options.instance().diagnosticsEnabled())
                    System.out.println
                        (mTestName
                            + ": Operations on URL "
                            + image.getSourceURL()
                            + "\n       in file "
                            + image.getFileName()
                            + (image.getSucceeded()
                            ? " succeeded"
                            : " failed"));

                // Increment the succeeded or failed counts.
                if (image.getSucceeded())
                    succeeded++;
                else
                    failed++;
            } catch (ExecutionException e) {
                System.out.println(mTestName + ": get() ExecutionException");
            } catch (InterruptedException e) {
                System.out.println(mTestName + ": get() InterruptedException");
            }

        if (!mTestName.equals(""))
            System.out.println(mTestName
                + ": "
                + succeeded
                + " operations succeeded and "
                + failed
                + " operations failed.");
    }
}    
