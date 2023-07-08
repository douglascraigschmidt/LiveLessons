package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * A helper class that holds search results.
 */
public class SearchResults {
    /**
     * This nested class holds one search result.
     */
    public static class Result {
        /**
         * The index in the search String where the word that was
         * found.
         */
        public int mIndex;

        /**
         * Create a Result object contains meta-data about a search
         * result.
         */
        public Result(int index) {
            mIndex = index;
        }
    }

    /**
     * Id of the Thread that found a search result.
     */
    public long mThreadId;

    /**
     * The word that was found.
     */
    public String mWord;

    /**
     * The input String used for the search.
     */
    public String mInputData;

    /**
     * The cycle in which the search result was found.
     */
    public long mCycle;

    /**
     * The List of Result objects that matched the @code mWord.
     */
    protected List<Result> mList;

    /**
     * Create an empty {@link SearchResults}, which is used to
     * shut down processing of the {@link BlockingQueue}.
     */
    public SearchResults() {
        mList = null;
    }

    /**
     * Create a {@link SearchResults} with values for the various
     * fields.
     */
    public SearchResults(long threadId,
                         long cycle,
                         String word,
                         String inputData) {
        mThreadId = threadId;
        mCycle = cycle;
        mWord = word;
        mInputData = inputData;
        mList = new ArrayList<>();
    }

    /**
     * Create a {@link SearchResults} with values for the various
     * fields.
     */
    public SearchResults(long threadId,
                         long cycle,
                         String word,
                         String inputData,
                         List<Result> results) {
        mThreadId = threadId;
        mCycle = cycle;
        mWord = word;
        mInputData = inputData;
        mList = results;
    }

    /**
     * Convert to {@link String} form.
     */
    public String toString() {
        return 
            "["
            + mThreadId
            + "|"
            + mCycle
            + "] "
            + mWord
            + " at "
            + mInputData;
    }

    /**
     * Add a {@link Result}.
     */
    public void add(int index) {
        mList.add(new Result(index));
    }

    /**
     * Returns true if there are no search results.
     */
    public boolean isEmpty() {
        return mList.size() == 0;
    }

    /**
     * Print the results.
     */
    public void print() {
        if (!isEmpty()) {
            System.out.print(toString());

            // Iterate through the list of indices that matched the
            // search word and print them out.
            for (Result result : mList)
                System.out.print
                    ("["
                     + result.mIndex
                     + "]");
            System.out.println();
        }
    }
}

