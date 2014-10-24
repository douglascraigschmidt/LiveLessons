import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @class OneShotExecutorService
 *
 * @brief Customizes the SearchTaskGangCommon framework to process
 *        a one-shot List of tasks via a pool of Threads created
 *        by the Executor, which is also used to wait for
 *        all the Threads in the pool to shutdown.
 */
public class OneShotExecutorService
       extends SearchTaskGangCommon {
    /**
     * Controls when the framework exits.
     */
    protected CountDownLatch mExitBarrier = null;

    /**
     * Queue to store the results of concurrent computations.
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
     * fixed-size Thread pool managed by the Executor.
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
     * Initiate the TaskGang to run each worker in the Thread
     * pool.
     */
    protected void initiateTaskGang(int inputSize) {
        // Allow subclasses to customize their behavior before the
        // Threads in the gang are spawned.
        initiateHook(inputSize);

        // Enqueue each item in the input List for execution in the
        // Executor's Thread pool.
        for (int i = 0; i < inputSize; ++i) 
            getExecutor().execute(makeTask(i));
    }

    /**
     * Runs in a background Thread and searches the inputData for
     * all occurrences of the words to find.
     */
    @Override
        protected boolean processInput (String inputData) {
        // Iterate through each word we're searching for
        // and try to find it in the inputData.
        for (String word : mWordsToFind) 
            // Each time a match is found the queueResults() hook
            // method is called to pass the search results to a
            // background Thread for concurrent processing.
            queueResults(searchForWord(word, 
                                       inputData));
        return true;
    }

    /**
     * Hook method called when a worker Thread is done - it
     * decrements the CountDownLatch.
     */
    @Override
        protected void taskDone(int index) throws IndexOutOfBoundsException {
        mExitBarrier.countDown();
    }

    /**
     * Hook method that can be used by processInput() to
     * process results.
     */
    protected void queueResults(SearchResults results) {
        getQueue().add(results);
    }

    /**
     * Hook method that shuts down the Executor's Thread
     * pool and waits for all the Threads to exit before
     * returning.
     */
    @Override
        protected void awaitTasksDone() {
        do {
            // Start processing this cycle of results
            // concurrently.
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

        // Call up to the super class to await for Executor
        // Threads to shutdown.
        super.awaitTasksDone();
    }

    /**
     * Set the BlockingQueue, returning the existing queue.
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
     * Process all the queued results concurrently.
     */
    protected void processQueuedResults(final int resultCount) {
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
        // concurrently.
        Thread t = new Thread(processQueuedResultsRunnable);
        t.start();

        try {
            // Wait for the results processing Thread to exit.
            t.join();
        } catch (InterruptedException e) {
            System.out.println("processQueuedResults() interrupted");
        }
    }
}

