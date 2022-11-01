import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @class OneShotExecutorCompletionService
 *
 * @brief Customizes the SearchTaskGangCommon framework to process a
 *        one-shot List of tasks via a variable-sized pool of Threads
 *        created by the ExecutorService. The units of concurrency are
 *        a "task per search word" *and* the input Strings. The
 *        results processing model uses an Asynchronous Future model,
 *        which starts processing results immediately.
 */
public class OneShotExecutorCompletionService
       extends SearchTaskGangCommon {
    /**
     * Processes the results of Futures returned from the
     * Executor.submit() method.
     */
    protected ExecutorCompletionService<SearchResults> mCompletionService;

    /**
     * Constructor initializes the superclass and data members.
     */
    protected OneShotExecutorCompletionService(String[] wordsToFind,
                                               String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind, 
              stringsToSearch);

        // Initialize the Executor with a cached pool of Threads,
        // which grow dynamically.
        setExecutor (Executors.newCachedThreadPool());

        // Connect the Executor with the CompletionService
        // to process SearchResults concurrently. 
        mCompletionService =
            new ExecutorCompletionService<SearchResults>(getExecutor());
    }

    /**
     * Initiate the TaskGang to process each input String as a
     * separate task in the Thread pool.
     */
    protected void initiateTaskGang(int inputSize) {
        // Enqueue each item in the input List for execution in the
        // Executor's Thread pool.
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
        for (final String word : mWordsToFind) {

            // This submit() call stores the Future result in the
            // ExecutorCompletionService for concurrent results
            // processing.
            mCompletionService
                    .submit (() ->
                             // call() runs in a background task.
                             searchForWord(word,
                                           inputData));
        }
        return true;
    }

    /**
     * Uses the ExecutorCompletionService to concurrently process all
     * the queued Futures.
     */
    protected void concurrentlyProcessQueuedFutures() {
        // Need to account for all the input data and all the words
        // that were searched for.
        final int count = 
            getInput().size() * mWordsToFind.length;

        // Loop for the designated number of results.
        for (int i = 0; i < count; ++i) 
            try {
                // Take the next ready Future off the
                // CompletionService's queue.
                final Future<SearchResults> resultFuture =
                    mCompletionService.take();

                // The get() call will not block since the results
                // should be ready before they are added to the
                // completion queue.
                resultFuture.get().print();
            } catch (Exception e) {
                System.out.println("get() exception");
            }
    }
}

