package tasks;

import utils.SearchResults;

import java.util.List;
import java.util.concurrent.*;

import static utils.ExceptionUtils.rethrowSupplier;

/**
 * Customizes the {@link SearchTaskGangCommon} framework to process a
 * one-shot {@link List} of tasks via a pool of "work-stealing" {@link
 * Thread} objects created by the {@link ExecutorService}. The units
 * of concurrency are a "task per search word" *and* the input
 * strings. An asynchronous future results processing model is
 * applied, which starts processing results immediately.
 */
public class OneShotExecutorCompletionService
    extends SearchTaskGangCommon {
    /**
     * Processes the results of Futures returned from the {@code
     * Executor.submit()} method.
     */
    protected ExecutorCompletionService<SearchResults> mCompletionService;

    /**
     * Constructor initializes the superclass and data members.
     */
    public OneShotExecutorCompletionService(String[] wordsToFind,
                                            String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind, stringsToSearch);

        // Initialize the Executor with a cached pool of "work-stealing"
        // Thread objects.
        setExecutor(Executors.newWorkStealingPool());

        // Connect the Executor with the CompletionService to process
        // SearchResults concurrently.
        mCompletionService =
            new ExecutorCompletionService<>(getExecutor());
    }

    /**
     * Initiate the TaskGang to process each input String as a
     * separate task in the Thread pool.
     */
    protected void initiateTaskGang(int inputSize) {
        // Enqueue each item in the input List for execution in the
        // Executor's cached thread pool.
        for (int i = 0; i < inputSize; ++i)
            getExecutor().execute(makeTask(i));

        // Process all the Futures concurrently via the
        // ExecutorCompletionService's completion queue.
        concurrentlyProcessQueuedFutures();
    }

    /**
     * Hook method that performs work a background task.  Returns true
     * if all goes well, else false (which will stop the background
     * task from continuing to run).
     */
    protected boolean processInput(final String inputData) {
        // Iterate through each word and submit a Callable that will
        // search concurrently for this word in the inputData.
        for (var word : mWordsToFind)
            // This submit() call stores the Future result in the
            // ExecutorCompletionService for concurrent results
            // processing.
            mCompletionService
                .submit(() ->
                    // This callable lambda runs in a background task.
                    searchForWord(word, inputData));

        return true;
    }

    /**
     * Uses the {@link ExecutorCompletionService} to concurrently process all
     * the queued {@link Future} objects.
     */
    protected void concurrentlyProcessQueuedFutures() {
        // Need to account for all the input data and all the words
        // that were searched for.
        int count = getInput().size() * mWordsToFind.length;

        // Loop for the designated number of results.
        for (int i = 0; i < count; ++i) {
            // Take the next ready Future off the
            // CompletionService's queue.
            var resultFuture =
                rethrowSupplier(mCompletionService::take).get();

            // The get() call will not block since the results
            // should be ready before they are added to the
            // completion queue.
            rethrowSupplier(resultFuture::get).get().print();
        }
    }
}


