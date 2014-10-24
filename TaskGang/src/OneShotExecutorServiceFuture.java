import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @class OneShotExecutorServiceFuture
 * 
 * @brief ...
 */
public class OneShotExecutorServiceFuture
       extends SearchTaskGangCommon {
    /**
     * A List of Futures that contain SearchResults.
     */
    protected List<Future<SearchResults>> mResultFutures;

    /**
     * Constructor initializes the superclass and data members.
     */
    protected OneShotExecutorServiceFuture(String[] wordsToFind,
                                           String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind, 
              stringsToSearch);

        // Initialize the Executor with a cached pool of
        // Threads.
        setExecutor (Executors.newCachedThreadPool());
    }

    /**
     * Process all the Futures containing search results.
     */
    protected void processFutureResults(List<Future<SearchResults>> resultFutures) {
        // Iterate through the List of Futures and print the
        // search results.  
        for (Future<SearchResults> resultFuture : resultFutures) {
            try {
                // The get() call may block if the results aren't
                // ready yet.
                resultFuture.get().print();
            } catch (Exception e) {
                System.out.println("get() exception");
            }
        }
    }

    /**
     * Hook method that performs work a background Task.  Returns true
     * if all goes well, else false (which will stop the background
     * Thread from continuing to run).
     */
    protected boolean processInput(final String inputData) {
        ExecutorService executorService = 
            (ExecutorService) getExecutor();

        // Iterate through each word and submit a Callable that
        // will search concurrently for this word in the
        // inputData.
        for (final String word : mWordsToFind) {
            // Create a Future to store the results.
            final Future<SearchResults> resultFuture = 
                executorService.submit(new Callable<SearchResults>() {
                        @Override
                        public SearchResults call() throws Exception {
                            return searchForWord(word,
                                                 inputData);
                        }
                    });

            // Add the Future to the List so it can be processed
            // later.
            mResultFutures.add(resultFuture);
        }
        return true;
    }

    /**
     * Initiate the TaskGang to process each word as a separate
     * task in the Thread pool.
     */
    protected void initiateTaskGang(int inputSize) {
        // Preallocate the List of Futures to hold all the
        // SearchResults.
        mResultFutures = 
            new ArrayList<Future<SearchResults>> 
            (inputSize * mWordsToFind.length);

        // Process each String of inputData via the
        // processInput() method.
        for (final String inputData : getInput())
            processInput(inputData);

        // Process all the Futures.
        processFutureResults(mResultFutures);
    }
}

