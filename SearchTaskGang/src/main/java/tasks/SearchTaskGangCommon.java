package tasks;

import utils.SearchResults;
import utils.TaskGang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.directory.SearchResult;

import static utils.ExceptionUtils.rethrowSupplier;

/**
 * This helper class factors out the common code used by all the
 * implementations of {@link TaskGang} below.  It customizes the
 * {@link TaskGang} framework to concurrently search an array of
 * {@link String} objects to determine if its contents match an array
 * of words.
 */
public abstract class SearchTaskGangCommon
                extends TaskGang<String> {
    /**
     * The array of words to find.
     */
    protected final String[] mWordsToFind;

    /**
     * An {@link Iterator} for the array of {@link String} objects to
     * search.
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
     * @return The next {@link List} of {@link String} objects to be
     *         searched concurrently by the {@link TaskGang}
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
     * Hook method that can be used as an exit barrier to wait for the
     * gang of tasks to exit.
     */
    @Override
    protected void awaitTasksDone() {
        // Only call the shutdown() and awaitTermination() methods if
        // we've actually got an ExecutorService (as opposed to just
        // an Executor).
        if (getExecutor() instanceof ExecutorService executorService) {
            // Tell the ExecutorService to initiate a graceful
            // shutdown.
            executorService.shutdown();

            // Wait for all the tasks/threads in the pool to complete.
            rethrowSupplier(() ->
                executorService.awaitTermination(Long.MAX_VALUE,
                                                 TimeUnit.NANOSECONDS)).get();
        }
    }

    /**
     * Search for all instances of {@code word} in {@code inputData}
     * and return a {@link List} of all the {@link SearchResults} (if
     * any).
     */
    protected SearchResults searchForWord(String word,
                                          String inputData) {
        return new SearchResults(Thread.currentThread().threadId(),
                                 currentCycle(),
                                 word,
                                 inputData,
                                 // Make a List of Result objects that
                                 // match the word to search for.
                                 makeResultsIndexOf(word, inputData));
    }

    /**
     * Use the Java {@code indexOf()} method to make a {@link List} of
     * {@link SearchResults.Result} objects that match the {@code word}
     * to search for in the {@code inputData}.
     * 
     * @param word The word to search for
     * @param inputData The input to search for {@code word}
     * @return A {@link List} of {@link SearchResults.Result} 
     *         objects
     */
    private List<SearchResults.Result> makeResultsIndexOf
        (String word,
         String inputData) {
        // Create an ArrayList to store Result objects.
        var results = new ArrayList<SearchResults.Result>();

        // Check to see how many times (if any) the word appears in
        // the input data.
        for (int i = inputData.indexOf(word);
             i != -1;
             i = inputData.indexOf(word, i + 1)) {
            // Each time a match is found, it's added to the list of
            // search results.
            results.add(new SearchResults.Result(i));
        } 

        // Return the list of results.
        return results;
    }
}

