package edu.vandy.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds results from a search of how many times a word appears in an
 * input string.
 */
public class SearchResults {
    /**
     * @class SearchResult
     *
     * @brief Holds one search result.
     */
    public class Result {
        /**
         * The index in the search String where the word that was
         * found.
         */
        public int mIndex;

        /**
         * Create a Result object contains meta-data about a search
         * result..
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
     * The section title this search is associated with.
     */
    public String mTitle;

    /**
     * The cycle in which the search result was found.
     */
    public long mCycle;

    /**
     * The List of Result objects that matched the @code mWord.
     */
    protected List<Result> mList;

    /**
     * Create an empty SearchResults, which is used to shutdown
     * processing of the BlockingQueue.
     */
    public SearchResults() {
        mList = null;
    }

    /**
     * Create a SearchResults with values for the various fields.
     */
    public SearchResults(long threadId,
                         long cycle,
                         String word,
                         String title) {
        mThreadId = threadId;
        mCycle = cycle;
        mWord = word;
        mTitle = title;
        mList = new ArrayList<Result>();
    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * Convert to header to String form.
     */
    public String headerToString() {
        return 
            "["
            + mThreadId
            + "|"
            + mCycle
            + "] "
            + mTitle
            + ": \""
            + mWord
            + "\" at";
    }

    /**
     * Add a Result.
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

    public int size() {
        return mList.size();
    }

    @Override
    public String toString() {
        String output = new String("");

        if (!isEmpty()) {
            output += headerToString();

            // Iterate through the list of indices that matched the
            // search word and print them out.
            for (Result result : mList)
                output += 
                    "["
                    + result.mIndex
                    + "]";
        }
        
        return output;
    }

    /**
     * Print the results.
     */
    public SearchResults print() {
        if (!isEmpty()) synchronized(System.out) {
                System.out.println(toString());
        }
        return this;
    }
}

