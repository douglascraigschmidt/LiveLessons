package search;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of how many times a phrase appears in an input string.
 */
public class SearchResults {
    /**
     * Hold the index of one search result.
     */
    public static class Result {
        /**
         * The index in the search String where the phrase that was
         * found.
         */
        private int mIndex;

        /**
         * Create a Result object contains meta-data about a search
         * result.
         */
        public Result(int index) {
            mIndex = index;
        }

        /**
         * Return the index.
         */
        public int getIndex() {
            return mIndex;
        }
    }

    /**
     * Id of the Thread that found a search result.
     */
    private long mThreadId;

    /**
     * The phrase that was found.
     */
    private String mPhrase;

    /**
     * The section title this search is associated with.
     */
    private String mTitle;

    /**
     * The cycle in which the search result was found.
     */
    private long mCycle;

    /**
     * The List of Result objects that matched the @code mPhrase.
     */
    private List<Result> mList;

    /**
     * Create an empty SearchResults, which is used to shutdown
     * processing of the BlockingQueue.
     */
    public SearchResults() {
        mList = null;
    }

    /**
     * Return the list of Results.
     */
    public List<Result> getResultList() {
        return mList;
    }

    /**
     * Create a SearchResults with values for the various fields.
     */
    public SearchResults(long threadId,
                         long cycle,
                         String phrase,
                         String title) {
        mThreadId = threadId;
        mCycle = cycle;
        mPhrase = phrase;
        mTitle = title;
        mList = new ArrayList<>();
    }

    /**
     * Create a SearchResults with values for the various fields.
     * This constructor is also passed a filled in resultList.
     */
    public SearchResults(long threadId,
                         long cycle,
                         String phrase,
                         String title,
                         List<Result> resultList) {
        mThreadId = threadId;
        mCycle = cycle;
        mPhrase = phrase;
        mTitle = title;
        mList = resultList;
    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * Convert to header to String form.
     */
    public String headerToString() {
        return 
            // "["
            // + mThreadId
            // + "|"
            // + mCycle
            // + "] "
            // + mTitle
            "  \""
            + mPhrase
            + "\" at ";
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

    /**
     * Returns the number of results.
     */
    public int size() {
        return mList.size();
    }

    /**
     * Return the phrase.
     */
    public String getPhrase() {
        return mPhrase;
    }

    /**
     * Return a string version of the object.
     */
    @Override
    public String toString() {
        String output = new String("");

        if (!isEmpty()) {
            output += headerToString();

            // Iterate through the list of indices that matched the
            // search phrase and print them out.
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
        if (!isEmpty()) {
            synchronized(System.out) {
                System.out.println(toString());
            }
        }
        return this;
    }
}

