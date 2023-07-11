package tasks;

import utils.SearchResults;
import utils.TaskGang;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static utils.ExceptionUtils.rethrowSupplier;

/**
 * Customizes the {@link SearchTaskGangCommon} framework to process a
 * one-shot List of tasks via a variable-sized pool of {@link Thread}
 * objects associated with the {@link ExecutorService}. The unit of
 * concurrency is a "task per search word". The results processing
 * model uses the Synchronous Future model, which defers results
 * processing until all words to search for have been submitted.
 */
public class OneShotExecutorServiceFuture
    extends SearchTaskGangCommon {
    /**
     * A {@link List} of {@link Future} objects that contain {@link
     * SearchResults}.
     */
    protected List<Future<SearchResults>> mResultFutures;

    /**
     * Constructor initializes the superclass and data members.
     */
    public OneShotExecutorServiceFuture(String[] wordsToFind,
                                        String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind, stringsToSearch);

        // Initialize the Executor with a cached pool of Threads,
        // which grow dynamically.
        setExecutor(Executors.newCachedThreadPool());
    }

    /**
     * Initiate the {@link TaskGang} to process each word as a
     * separate task in the {@link ExecutorService}'s {@link Thread}
     * pool.
     */
    protected void initiateTaskGang(int inputSize) {
        // Preallocate the List of Futures to hold all the
        // SearchResults.
        mResultFutures =
            new ArrayList<>(inputSize * mWordsToFind.length);

        // Process each String of inputData via the processInput()
        // method.  The input Strings aren't run concurrently, just
        // each word that's being searched for.
        for (var inputData : getInput())
            processInput(inputData);

        // Process all the Futures.
        processFutureResults(mResultFutures);
    }

    /**
     * Hook method that performs work a background task.  Returns true
     * if all goes well, else false (which will stop the background
     * task from continuing to run).
     */
    protected boolean processInput(String inputData) {
        if (getExecutor() instanceof ExecutorService executorService)
            // Iterate through each word.
            for (var word : mWordsToFind) {
                // Submit a Callable that will search concurrently for
                // this word in the inputData & create a Future to
                // store the results.
                var resultFuture = executorService
                    .submit(() -> searchForWord(word, inputData));

                // Add the Future to the List, so it can be processed
                // later.
                mResultFutures.add(resultFuture);
            }

        return true;
    }

    /**
     * Process all the {@link Future} objects containing {@link
     * SearchResults}.
     */
    protected void processFutureResults
    (List<Future<SearchResults>> resultFutures) {
        // Iterate through the List of Future objects and print the
        // SearchResults.
        for (var resultFuture : resultFutures) {
            // The get() call may block if the SearchResults aren't
            // ready yet.
            rethrowSupplier(resultFuture::get).get().print();
        }
    }
}

