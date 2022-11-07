import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.directory.SearchResult;

/**
 * @class SearchTaskGangCommon
 * 
 * @brief This helper class factors out the common code used by all
 *        the implementations of TaskGang below.  It customizes the
 *        TaskGang framework to concurrently search an array of
 *        Strings for an array of words to find.
 */
public abstract class SearchTaskGangCommon
                extends TaskGang<String> {
    /**
     * The array of words to find.
     */
    protected final String[] mWordsToFind;

    /**
     * An Iterator for the array of Strings to search.
     */
    private final Iterator<String[]> mInputIterator;

    /**
     * Constructor initializes the data members.
     */
    protected SearchTaskGangCommon(String[] wordsToFind,
                                   String[][] stringsToSearch) {
        // Store the words to search for.
        mWordsToFind = wordsToFind;

        // Create an Iterator for the array of Strings to search.
        mInputIterator = Arrays.asList(stringsToSearch).iterator();
    }

    /**
     * Factory method that returns the next List of Strings to be
     * searched concurrently by the TaskGang.
     */
    @Override
    protected List<String> getNextInput() {
        if (mInputIterator.hasNext()) {
            // Note that we're starting a new cycle.
            incrementCycle();

            // Return a List containing the Strings to search
            // concurrently.
            return Arrays.asList(mInputIterator.next());
        }
        else 
            // Indicate that we're done.
            return null;
    }

    /**
     * Search for all instances of @code word in @code inputData
     * and return a list of all the @code SearchData results (if
     * any).
     */
    protected SearchResults searchForWord(String word, 
                                          String inputData) {
        SearchResults results =
            new SearchResults(Thread.currentThread().getId(),
                              currentCycle(),
                              word,
                              inputData);

        // Check to see how many times (if any) the word appears
        // in the input data.
        for (int i = inputData.indexOf(word, 0);
             i != -1;
             i = inputData.indexOf(word, i + word.length())) {
            // Each time a match is found it's added to the list
            // of search results.
            results.add(i);
        }
        return results;
    }

    /**
     * Hook method that can be used as an exit barrier to wait for the
     * gang of tasks to exit.
     */
    protected void awaitTasksDone() {
        // Only call the shutdown() and awaitTermination() methods if
        // we've actually got an ExecutorService (as opposed to just
        // an Executor).
        if (getExecutor() instanceof ExecutorService) {
            ExecutorService executorService = 
                (ExecutorService) getExecutor();

            // Tell the ExecutorService to initiate a graceful
            // shutdown.
            executorService.shutdown();
            try {
                // Wait for all the tasks/threads in the pool to
                // complete.
                executorService.awaitTermination(Long.MAX_VALUE,
                                                 TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
            }
        }
    }
}

