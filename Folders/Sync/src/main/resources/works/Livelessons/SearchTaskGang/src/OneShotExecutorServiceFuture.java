import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @class OneShotExecutorServiceFuture
 * 
 * @brief Customizes the SearchTaskGangCommon framework to process a
 *        one-shot List of tasks via a variable-sized pool of Threads
 *        created by the ExecutorService. The unit of concurrency is a
 *        "task per search word". The results processing model uses
 *        the Synchronous Future model, which defers the results
 *        processing until all words to search for have been
 *        submitted.
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

        // Initialize the Executor with a cached pool of Threads,
        // which grow dynamically.
        setExecutor (Executors.newCachedThreadPool());
    }

    /**
     * Initiate the TaskGang to process each word as a separate task
     * in the ExecutorService's Thread pool.
     */
    protected void initiateTaskGang(int inputSize) {
        // Preallocate the List of Futures to hold all the
        // SearchResults.
        mResultFutures = 
            new ArrayList<Future<SearchResults>> 
            (inputSize * mWordsToFind.length);

        // Process each String of inputData via the processInput()
        // method.  Note that input Strings aren't run concurrently,
        // just eacd word that's being searched for.
        for (final String inputData : getInput())
            processInput(inputData);

        // Process all the Futures.
        processFutureResults(mResultFutures);
    }

    /**
     * Hook method that performs work a background task.  Returns true
     * if all goes well, else false (which will stop the background
     * task from continuing to run).
     */
    protected boolean processInput(final String inputData) {
        ExecutorService executorService = 
            (ExecutorService) getExecutor();

        // Iterate through each word.
        for (final String word : mWordsToFind) {

            // Submit a Callable that will search concurrently for
            // this word in the inputData & create a Future to store
            // the results.
            final Future<SearchResults> resultFuture =
                executorService.submit(new Callable<SearchResults>() {
                        @Override
                        // call() runs in a background task.
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
     * Process all the Futures containing search results.
     */
    protected void processFutureResults(List<Future<SearchResults>> resultFutures) {

        // Iterate through the List of Futures and print the search
        // results.
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
}

