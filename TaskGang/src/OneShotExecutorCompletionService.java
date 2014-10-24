import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @class OneShotExecutorCompletionService
 *
 * @brief ...
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

        // Initialize the Executor with a cached pool of
        // Threads.
        setExecutor (Executors.newCachedThreadPool());

        // Connect the Executor with the CompletionService
        // to process SearchResults concurrently. 
        mCompletionService =
            new ExecutorCompletionService<SearchResults>(getExecutor());
    }

    /**
     * Uses the ExecutorCompletionService to concurrently process
     * all the queued Futures.
     */
    protected void concurrentlyProcessQueuedFutures() {
        // Need to account for all the input data and all the
        // words that were searched for.
        final int count = 
            getInput().size() * mWordsToFind.length;

        for (int i = 0; i < count; ++i) 
            try {
                // Take the next ready Future off the
                // CompletionService's queue and print the search
                // results.
                final Future<SearchResults> resultFuture =
                    mCompletionService.take();

                // The get() call will not block since the results
                // should be ready.
                resultFuture.get().print();
            } catch (Exception e) {
                System.out.println("get() exception");
            }
    }

    /**
     * Hook method that performs work a background Task.  Returns true
     * if all goes well, else false (which will stop the background
     * Thread from continuing to run).
     */
    protected boolean processInput(final String inputData) {

        // Iterate through each word and submit a Callable that
        // will search concurrently for this word in the
        // inputData.
        for (final String word : mWordsToFind) {
            // This submit() call stores the Future result in the
            // ExecutorCompletionService for concurrent
            // processing.
            mCompletionService.submit (new Callable<SearchResults>() {
                    @Override
                        public SearchResults call() throws Exception {
                        return searchForWord(word,
                                             inputData);
                    }
                });
        }
        return true;
    }

    /**
     * Initiate the TaskGang to process each word as a separate
     * task in the Thread pool.
     */
    protected void initiateTaskGang(int inputSize) {
        // Enqueue each item in the input List for execution in
        // the Executor's Thread pool.
        for (int i = 0; i < inputSize; ++i) 
            getExecutor().execute(makeTask(i));

        // Process all the Futures concurrently via the
        // ExecutorCompletionService
        concurrentlyProcessQueuedFutures();
    }
}

