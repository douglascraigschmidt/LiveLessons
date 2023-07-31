package tasks;

import utils.ExceptionUtils;
import utils.SearchResults;
import utils.TaskGang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static utils.ExceptionUtils.*;

/**
 * Customizes the {@link SearchTaskGangCommon} framework to process a
 * one-shot {@link List} of tasks via a fixed-size pool of {@link
 * Thread} objects associated with the {@link ExecutorService}, which is
 * also used as a barrier synchronizer to wait for all the threads in
 * the pool to shut down.  The unit of concurrency is {@code
 * invokeAll()}, which creates a task for each input string.  The
 * results processing model uses a {@link LinkedBlockingQueue} that
 * stores results for immediate concurrent processing per cycle.
 */
public class OneShotExecutorService
       extends SearchTaskGangCommon {
    /**
     * Controls when the framework exits.
     */
    protected CountDownLatch mExitBarrier = null;

    /**
     * Queue to store {@link SearchResults} of concurrent
     * computations.
     */
    private final BlockingQueue<SearchResults> mResultsQueue =
        new LinkedBlockingQueue<>();

    /**
     * Number of {@link Thread} objects in the pool is set
     * to the number of processor cores known to the JVM.
     */
    protected final int MAX_THREADS = Runtime
        .getRuntime().availableProcessors();

    /**
     * Constructor initializes the superclass.
     */
    public OneShotExecutorService(String[] wordsToFind,
                                  String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind, stringsToSearch);

        // Create an ExecutorService that uses a fixed-size pool
        // of Java Thread objects.
        setExecutor(Executors
            .newFixedThreadPool(MAX_THREADS));
    }

    /**
     * Hook method that initiates the gang of {@link Thread} objects
     * by using a fixed-size {@link Thread} pool managed by the {@link
     * ExecutorService}.
     */
    @Override
    protected void initiateHook(int inputSize) {
        System.out.println(">>> starting cycle "
                           + currentCycle()
                           + " with "
                           + inputSize
                           + " tasks <<<");
        // Initialize the exit barrier to inputSize, which causes
        // awaitTasksDone() to block until the cycle is finished.
        mExitBarrier = new CountDownLatch(inputSize);
    }

    /**
     * Initiate the {@link TaskGang} to run each worker in the {@link
     * Thread} pool.
     */
    protected void initiateTaskGang(int inputSize) {
        // Allow subclasses to customize their behavior before the
        // Thread objects in the gang are spawned.
        initiateHook(inputSize);

        // Create a new collection that will contain all the
        // Runnable objects.
        List<Callable<Object>> workerCollection =
            new ArrayList<>(inputSize);

        // Create a Runnable for each item in the input List and add
        // it as a Callable adapter into the collection.
        for (int i = 0; i < inputSize; ++i)
            workerCollection.add(Executors.callable(makeTask(i)));

        // Downcast to get the ExecutorService.
        if (getExecutor() instanceof ExecutorService executorService)
            // Use invokeAll() to execute all items in the collection via
            // the Executor's Thread pool.  This invocation blocks the
            // calling thread until all the processing is finished.
            rethrowSupplier(() -> executorService
                            .invokeAll(workerCollection)).get();
    }

    /**
     * Runs as a background task and searches inputData for all
     * occurrences of the words to find.
     */
    @Override
    protected boolean processInput(String inputData) {
        // Iterate through each word we're searching for
        // and try to find it in the inputData.
        for (String word : mWordsToFind)
            // Each time a match is found add the SearchResults to the
            // Queue that provides messages for a worker Thread to
            // process concurrently.
            mResultsQueue.add(searchForWord(word, inputData));

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
     * Hook method that shuts down the ExecutorService's Thread pool
     * and waits for all the tasks to exit before returning.
     */
    @Override
    protected void awaitTasksDone() {
        do {
            // Need to account for all the input data and all the words
            // that were searched for.
            int count = getInput().size() * mWordsToFind.length;

            // Start processing this cycle of results concurrently in
            // a background Thread.
            processQueuedResults(count);

            // Wait until the exit barrier has been tripped.
            rethrowRunnable(mExitBarrier::await);

            // Keep looping as long as there's another cycle's
            // worth of input.
        } while (advanceTaskToNextCycle());

        // Call up to the super class to await for ExecutorService's
        // Thread objects to shut down.
        super.awaitTasksDone();
    }

    /**
     * Process all the queued results concurrently in a background
     * Thread.
     */
    protected void processQueuedResults(final int resultCount) {
        // Process all queued results.
        for (int i = 0; i < resultCount; ++i)
            // Extract each SearchResults from the queue (blocking if
            // necessary) until we're done.
            rethrowSupplier(mResultsQueue::take).get().print();
    }

}

