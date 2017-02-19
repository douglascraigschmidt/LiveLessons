import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @class OneShotExecutorService
 *
 * @brief Customizes the SearchTaskGangCommon framework to process a
 *        one-shot List of tasks via a fixed-size pool of Threads
 *        created by the ExecutorService, which is also used as a
 *        barrier synchronizer to wait for all the Threads in the pool
 *        to shutdown.  The unit of concurrency is invokeAll(), which
 *        creates a task for each input string.  The results
 *        processing model uses a LinkedBlockingQueue that stores
 *        results for immediate concurrent processing per cycle.
 */
public class OneShotExecutorService
       extends SearchTaskGangCommon {
    /**
     * Controls when the framework exits.
     */
    protected CountDownLatch mExitBarrier = null;

    /**
     * Queue to store SearchResults of concurrent computations.
     */
    private BlockingQueue<SearchResults> mResultsQueue = 
        new LinkedBlockingQueue<SearchResults>();

    /**
     * Number of Threads in the pool.
     */ 
    protected final int MAX_THREADS = 4;

    /**
     * Constructor initializes the superclass.
     */
    public OneShotExecutorService(String[] wordsToFind,
                                  String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Hook method that initiates the gang of Threads by using a
     * fixed-size Thread pool managed by the ExecutorService.
     */
    @Override
    protected void initiateHook(int inputSize) {
        System.out.println("@@@ starting cycle "
        		           + currentCycle()
                           + " with "
                           + inputSize
                           + " tasks@@@");
        // Initialize the exit barrier to inputSize, which causes
        // awaitTasksDone() to block until the cycle is finished.
        mExitBarrier = new CountDownLatch(inputSize);

        // Create a fixed-size Thread pool.
        if (getExecutor() == null) 
            setExecutor (Executors.newFixedThreadPool(MAX_THREADS));
    }

    /**
     * Initiate the TaskGang to run each worker in the Thread pool.
     */
    protected void initiateTaskGang(int inputSize) {
        // Allow subclasses to customize their behavior before the
        // Threads in the gang are spawned.
        initiateHook(inputSize);

        // Create a new collection that will contain all the
        // Worker Runnables.
        List<Callable<Object>> workerCollection =
            new ArrayList<Callable<Object>>(inputSize);

        // Create a Runnable for each item in the input List and add
        // it as a Callable adapter into the collection.
        for (int i = 0; i < inputSize; ++i) 
            workerCollection.add(Executors.callable(makeTask(i)));

        try {
            // Downcast to get the ExecutorService.
            ExecutorService executorService = 
                (ExecutorService) getExecutor();

            // Use invokeAll() to execute all items in the collection
            // via the Executor's Thread pool.  Note that this
            // invocation blocks the calling thread until all the
            // processing is finished.
            executorService.invokeAll(workerCollection);
        } catch (InterruptedException e) {
            System.out.println("invokeAll() interrupted");
        }
    }

    /**
     * Runs as a background task and searches inputData for all
     * occurrences of the words to find.
     */
    @Override
    protected boolean processInput (String inputData) {
        // Iterate through each word we're searching for
        // and try to find it in the inputData.
        for (String word : mWordsToFind) 

            // Each time a match is found the queueResults() method is
            // called to pass the search results to a background
            // Thread for concurrent processing.
            queueResults(searchForWord(word, 
                                       inputData));
        return true;
    }

    /**
     * Hook method called when a worker task is done.
     */
    @Override
    protected void taskDone(int index) throws IndexOutOfBoundsException {

        // Decrements the CountDownLatch, which releases the main
        // Thread when count drops to 0.
        mExitBarrier.countDown();
    }

    /**
     * Used by processInput() to queue results for processing in a
     * background Thread.
     */
    protected void queueResults(SearchResults results) {
        getQueue().add(results);
    }

    /**
     * Hook method that shuts down the ExecutorService's Thread pool
     * and waits for all the tasks to exit before returning.
     */
    @Override
    protected void awaitTasksDone() {
        do {
            // Start processing this cycle of results concurrently in
            // a background Thread.
            processQueuedResults(getInput().size() 
                                 * mWordsToFind.length);

            try {
                // Wait until the exit barrier has been tripped.
                mExitBarrier.await();
            } catch (InterruptedException e) {
                System.out.println("await() interrupted");
            }

            // Keep looping as long as there's another cycle's
            // worth of input.
        } while (advanceTaskToNextCycle());

        // Call up to the super class to await for ExecutorService's
        // Threads to shutdown.
        super.awaitTasksDone();
    }

    /**
     * Set the BlockingQueue and return the existing queue.
     */
    protected BlockingQueue<SearchResults> setQueue(BlockingQueue<SearchResults> q) {
        BlockingQueue<SearchResults> old = mResultsQueue;
        mResultsQueue = q;
        return old;
    }

    /**
     * Get the BlockingQueue. 
     */
    protected BlockingQueue<SearchResults> getQueue() {
        return mResultsQueue;
    }

    /**
     * Process all the queued results concurrently in a background
     * Thread.
     */
    protected void processQueuedResults(final int resultCount) {
        // This runnable processes all queued results.
        Runnable processQueuedResultsRunnable =
            new Runnable() {
                public void run() {
                    try {
                        for (int i = 0; i < resultCount; ++i)
                            // Extract each SearchResults from the
                            // queue (blocking if necessary) until
                            // we're done.
                            getQueue().take().print();

                    } catch (InterruptedException e) {
                        System.out.println("run() interrupted");
                    }
                }
            };

        // Create a new Thread that will process the results
        // concurrently in the background.
        Thread t = new Thread(processQueuedResultsRunnable);
        t.start();

        try {
            // Simple barrier that waits for the Thread processing
            // SearchResults to exit.
            t.join();
        } catch (InterruptedException e) {
            System.out.println("processQueuedResults() interrupted");
        }
    }
}

